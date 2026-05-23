package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.ActivityType;
import es.udc.anpaproject.backend.model.entities.ActivityVisibility;
import es.udc.anpaproject.backend.model.entities.Enrollment;
import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.ActivityDeletionException;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.ActivityService;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class ActivityServiceTest {

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private UserDto createUser(String userName) throws DuplicateInstanceException {
        UserDto userDto = new UserDto(
                null,
                userName,
                "Test",
                "User",
                userName + "@udc.es",
                "USER"
        );

        userDto.setPassword("password");

        return userService.signUp(userDto);
    }

    private UserDto createAdmin(String userName)
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto userDto = createUser(userName);

        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.user",
                                userDto.getId()
                        )
                );

        user.setRole(RoleType.ADMIN);
        userRepository.save(user);

        return userDto;
    }

    private UserDto createMember(String userName)
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto userDto = createUser(userName);

        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.user",
                                userDto.getId()
                        )
                );

        user.setMembershipStatus(MembershipStatus.MEMBER);
        userRepository.save(user);

        return userDto;
    }

    private ActivityDto createActivityDto(String title, ActivityType type) {
        ActivityDto dto = new ActivityDto();

        dto.setTitle(title);
        dto.setDescription("Descripción de prueba");
        dto.setStartDate(LocalDate.now().plusDays(7));
        dto.setEndDate(LocalDate.now().plusDays(30));
        dto.setPrice(25.0);
        dto.setType(type);

        return dto;
    }

    private MockMultipartFile createMockImage() {
        return new MockMultipartFile(
                "image",
                "actividad.png",
                "image/png",
                "contenido imagen".getBytes()
        );
    }

    @Test
    public void testCreateActivityAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminCreateActivity");

        ActivityDto activity = activityService.createActivity(
                createActivityDto("Baloncesto", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        assertNotNull(activity.getId());
        assertEquals("Baloncesto", activity.getTitle());
        assertEquals("Descripción de prueba", activity.getDescription());
        assertEquals(25.0, activity.getPrice());
        assertEquals(ActivityType.ACTIVITY, activity.getType());
        assertEquals(ActivityVisibility.PUBLIC, activity.getVisibility());
        assertNotNull(activity.getStartDate());
        assertNotNull(activity.getEndDate());
    }

    @Test
    public void testCreateActivityAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException {

        UserDto user = createUser("normalCreateActivity");

        assertThrows(PermissionException.class, () -> {
            activityService.createActivity(
                    createActivityDto("Teatro", ActivityType.ACTIVITY),
                    user.getId(),
                    null
            );
        });
    }

    @Test
    public void testCreateActivityWithImageAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminCreateActivityImage");

        ActivityDto activity = activityService.createActivity(
                createActivityDto("Pintura", ActivityType.ACTIVITY),
                admin.getId(),
                createMockImage()
        );

        assertNotNull(activity.getId());
        assertEquals("Pintura", activity.getTitle());
        assertNotNull(activity.getImageUrl());
        assertTrue(activity.getImageUrl().contains("/uploads/"));
    }

    @Test
    public void testFindActivities()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminFindActivities");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Ajedrez", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        Block<Activity> activities = activityService.findActivities(0, 10);

        assertFalse(activities.getItems().isEmpty());

        assertTrue(
                activities.getItems().stream()
                        .anyMatch(activity ->
                                activity.getId().equals(savedActivity.getId())
                        )
        );
    }

    @Test
    public void testFindVisibleActivitiesForAnonymousOnlyReturnsPublicActivities()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminVisibleAnonymous");
        ActivityDto publicActivity = createActivityDto("Publica", ActivityType.ACTIVITY);
        publicActivity.setVisibility(ActivityVisibility.PUBLIC);

        ActivityDto registeredActivity = createActivityDto("Registrados", ActivityType.ACTIVITY);
        registeredActivity.setVisibility(ActivityVisibility.REGISTERED);

        ActivityDto savedPublic = activityService.createActivity(publicActivity, admin.getId(), null);
        ActivityDto savedRegistered = activityService.createActivity(registeredActivity, admin.getId(), null);

        Block<Activity> activities = activityService.findVisibleActivities(null, null, 0, 10);

        assertTrue(activities.getItems().stream()
                .anyMatch(activity -> activity.getId().equals(savedPublic.getId())));
        assertFalse(activities.getItems().stream()
                .anyMatch(activity -> activity.getId().equals(savedRegistered.getId())));
    }

    @Test
    public void testFindVisibleActivitiesByTitle()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminSearchActivities");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Robotica educativa", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        activityService.createActivity(
                createActivityDto("Baloncesto", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        Block<Activity> activities = activityService.findVisibleActivities(null, "robotica", 0, 10);

        assertTrue(activities.getItems().stream()
                .anyMatch(activity -> activity.getId().equals(savedActivity.getId())));
        assertFalse(activities.getItems().stream()
                .anyMatch(activity -> activity.getTitle().equals("Baloncesto")));
    }

    @Test
    public void testFindVisibleActivityForMemberOnlyAllowsMembers()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminMemberVisibility");
        UserDto user = createUser("normalMemberVisibility");
        UserDto member = createMember("memberVisibility");

        ActivityDto memberActivity = createActivityDto("Solo socios", ActivityType.ACTIVITY);
        memberActivity.setVisibility(ActivityVisibility.MEMBERS_ONLY);

        ActivityDto savedActivity = activityService.createActivity(memberActivity, admin.getId(), null);

        assertThrows(PermissionException.class, () ->
                activityService.findVisibleById(savedActivity.getId(), user.getId()));

        ActivityDto foundActivity = activityService.findVisibleById(savedActivity.getId(), member.getId());

        assertEquals(savedActivity.getId(), foundActivity.getId());
        assertEquals(ActivityVisibility.MEMBERS_ONLY, foundActivity.getVisibility());
    }

    @Test
    public void testFindById()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminFindActivityById");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Excursión", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        ActivityDto foundActivity = activityService.findById(savedActivity.getId());

        assertEquals(savedActivity.getId(), foundActivity.getId());
        assertEquals("Excursión", foundActivity.getTitle());
        assertEquals(ActivityType.ACTIVITY, foundActivity.getType());
    }

    @Test
    public void testUpdateActivityAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminUpdateActivity");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Actividad antigua", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        ActivityDto updateDto = createActivityDto(
                "Actividad actualizada",
                ActivityType.ACTIVITY
        );

        updateDto.setDescription("Descripción actualizada");
        updateDto.setPrice(30.0);

        ActivityDto updatedActivity = activityService.updateActivity(
                savedActivity.getId(),
                updateDto,
                admin.getId()
        );

        assertEquals(savedActivity.getId(), updatedActivity.getId());
        assertEquals("Actividad actualizada", updatedActivity.getTitle());
        assertEquals("Descripción actualizada", updatedActivity.getDescription());
        assertEquals(30.0, updatedActivity.getPrice());
    }

    @Test
    public void testUpdateActivityAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminUpdatePermission");
        UserDto user = createUser("normalUpdatePermission");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Actividad protegida", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        assertThrows(PermissionException.class, () -> {
            activityService.updateActivity(
                    savedActivity.getId(),
                    createActivityDto("Intento actualizar", ActivityType.ACTIVITY),
                    user.getId()
            );
        });
    }

    @Test
    public void testDeleteActivityAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, ActivityDeletionException, IOException {

        UserDto admin = createAdmin("adminDeleteActivity");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Actividad a borrar", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        activityService.deleteActivity(savedActivity.getId(), admin.getId());

        assertThrows(InstanceNotFoundException.class, () -> {
            activityService.findById(savedActivity.getId());
        });
    }

    @Test
    public void testDeleteActivityAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("adminDeletePermission");
        UserDto user = createUser("normalDeletePermission");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Actividad no borrable", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        assertThrows(PermissionException.class, () -> {
            activityService.deleteActivity(savedActivity.getId(), user.getId());
        });
    }

    @Test
    public void testDeleteActivityWithPaidEnrollmentsThrowsActivityDeletionException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, ActivityDeletionException, IOException {

        UserDto admin = createAdmin("adminDeletePaidActivity");
        UserDto userDto = createUser("paidActivityUser");

        ActivityDto savedActivity = activityService.createActivity(
                createActivityDto("Actividad pagada protegida", ActivityType.ACTIVITY),
                admin.getId(),
                null
        );

        User user = userRepository.findById(userDto.getId()).get();
        Activity activity = activityRepository.findById(savedActivity.getId()).get();
        Enrollment enrollment = new Enrollment(user, activity);
        enrollment.setPaid(true);
        enrollmentRepository.save(enrollment);

        assertThrows(ActivityDeletionException.class, () -> {
            activityService.deleteActivity(savedActivity.getId(), admin.getId());
        });
    }

    @Test
    public void testFindNonExistentActivityThrowsInstanceNotFoundException() {
        assertThrows(InstanceNotFoundException.class, () -> {
            activityService.findById(-1L);
        });
    }
}
