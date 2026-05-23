package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.ActivityVisibility;
import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.ActivityDeletionException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.ActivityConverter;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.Files;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityServiceImpl implements ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private final Path uploadDir = Paths.get("uploads");

    @Override
    public ActivityDto createActivity(ActivityDto dto, Long userId, MultipartFile image)
            throws InstanceNotFoundException, PermissionException, IOException {

        User creator = getUser(userId);
        checkAdmin(creator);

        Activity activity = ActivityConverter.toActivityEntity(dto);
        if (activity.getVisibility() == null) {
            activity.setVisibility(ActivityVisibility.PUBLIC);
        }
        activity.setCreatedBy(creator);

        if (image != null && !image.isEmpty()) {

            if (!Files.exists(uploadDir)) {
                Files.createDirectories(uploadDir);
            }

            String originalFilename = image.getOriginalFilename();
            String fileName = UUID.randomUUID() + "_" + originalFilename;
            Path filePath = uploadDir.resolve(fileName);

            Files.copy(image.getInputStream(), filePath);

            activity.setImageUrl("/uploads/" + fileName);
        }

        Activity saved = activityRepository.save(activity);

        return ActivityConverter.toActivityDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Block<Activity> findActivities(int page, int size) {

        Slice<Activity> slice = activityRepository
                .findByOrderByTitleAsc(PageRequest.of(page, size));

        return new Block<>(
                slice.getContent(),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Block<Activity> findVisibleActivities(Long userId, String keywords, int page, int size)
            throws InstanceNotFoundException {

        User user = userId != null ? getUser(userId) : null;
        List<ActivityVisibility> visibilities = getAllowedVisibilities(user);
        PageRequest pageRequest = PageRequest.of(page, size);
        Slice<Activity> slice;

        if (keywords == null || keywords.trim().isEmpty()) {
            slice = activityRepository.findByVisibilityInOrderByTitleAsc(
                    visibilities,
                    pageRequest
            );
        } else {
            slice = activityRepository.findByTitleContainingIgnoreCaseAndVisibilityInOrderByTitleAsc(
                    keywords.trim(),
                    visibilities,
                    pageRequest
            );
        }

        return new Block<>(
                slice.getContent(),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityDto findById(Long id) throws InstanceNotFoundException {

        Activity activity = getActivity(id);

        return ActivityConverter.toActivityDto(activity);
    }

    @Override
    @Transactional(readOnly = true)
    public ActivityDto findVisibleById(Long id, Long userId)
            throws InstanceNotFoundException, PermissionException {

        Activity activity = getActivity(id);
        User user = userId != null ? getUser(userId) : null;

        if (!canView(activity, user)) {
            throw new PermissionException();
        }

        return ActivityConverter.toActivityDto(activity);
    }

    @Override
    public ActivityDto updateActivity(Long id, ActivityDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        Activity activity = getActivity(id);

        ActivityConverter.merge(activity, dto);
        if (activity.getVisibility() == null) {
            activity.setVisibility(ActivityVisibility.PUBLIC);
        }

        Activity updated = activityRepository.save(activity);

        return ActivityConverter.toActivityDto(updated);
    }

    @Override
    public void deleteActivity(Long id, Long userId)
            throws InstanceNotFoundException, PermissionException, ActivityDeletionException {

        User user = getUser(userId);
        checkAdmin(user);

        Activity activity = getActivity(id);

        if (enrollmentRepository.existsByActivityAndPaidTrue(activity)) {
            throw new ActivityDeletionException("project.exceptions.activity.cannotDeleteWithPaidEnrollments");
        }

        activityRepository.delete(activity);
    }

    public static List<ActivityDto> toActivityDtos(List<Activity> activities) {
        return activities.stream()
                .map(ActivityConverter::toActivityDto)
                .toList();
    }

    private User getUser(Long userId) throws InstanceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.user",
                                userId
                        )
                );
    }

    private Activity getActivity(Long activityId) throws InstanceNotFoundException {
        return activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.activity",
                                activityId
                        )
                );
    }

    private void checkAdmin(User user) throws PermissionException {
        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }
    }

    private List<ActivityVisibility> getAllowedVisibilities(User user) {
        List<ActivityVisibility> visibilities = new ArrayList<>();
        visibilities.add(ActivityVisibility.PUBLIC);

        if (user != null) {
            visibilities.add(ActivityVisibility.REGISTERED);
        }

        if (user != null &&
                (user.getRole() == RoleType.ADMIN || user.getMembershipStatus() == MembershipStatus.MEMBER)) {
            visibilities.add(ActivityVisibility.MEMBERS_ONLY);
        }

        return visibilities;
    }

    private boolean canView(Activity activity, User user) {
        if (activity.getVisibility() == ActivityVisibility.PUBLIC) {
            return true;
        }

        if (user == null) {
            return false;
        }

        if (activity.getVisibility() == ActivityVisibility.REGISTERED) {
            return true;
        }

        return user.getRole() == RoleType.ADMIN || user.getMembershipStatus() == MembershipStatus.MEMBER;
    }
}
