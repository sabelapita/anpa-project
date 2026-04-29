package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.entities.RoleType;
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
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ActivityService {

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path uploadDir = Paths.get("uploads");

    public ActivityDto createActivity(ActivityDto dto, Long userId, MultipartFile image) 
            throws InstanceNotFoundException,  PermissionException,  IOException{
        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
        
        if (creator.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }

        Activity activity = ActivityConverter.toActivityEntity(dto);
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

    @Transactional(readOnly = true)
    public Block<Activity> findActivities(int page, int size) {
        Slice<Activity> slice = activityRepository.findByOrderByTitleAsc(PageRequest.of(page, size));
        return new Block<>(slice.getContent(), slice.hasNext());
    }
    
    @Transactional(readOnly = true)
    public ActivityDto findById(Long id) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));
        return ActivityConverter.toActivityDto(activity);
    }

    
    public ActivityDto updateActivity(Long id, ActivityDto dto, Long userId) 
        throws InstanceNotFoundException, PermissionException{
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.activity", id));
        
                User user = userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
        
                if (user.getRole() != RoleType.ADMIN) {
                    throw new PermissionException();
        }

        ActivityConverter.merge(activity, dto);
        Activity updated = activityRepository.save(activity);
        return ActivityConverter.toActivityDto(updated);
    }

    public void deleteActivity(Long id, Long userId)  
        throws InstanceNotFoundException,PermissionException{
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.activity", id));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));

        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }

        activityRepository.delete(activity);
    }

    public static List<ActivityDto> toActivityDtos(List<Activity> activities) {
        return activities.stream()
                .map(ActivityConverter::toActivityDto)
                .toList();
    }
}
