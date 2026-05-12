package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.ActivityType;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.DuplicateEnrollmentException;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.EnrollmentNotAllowedException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.ActivityService;
import es.udc.anpaproject.backend.model.services.EnrollmentService;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.dtos.ActivityDto;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private ActivityService activityService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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

    private ActivityDto createActivity(String title, ActivityType type)
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto admin = createAdmin("admin" + title.replace(" ", ""));

        return activityService.createActivity(
                createActivityDto(title, type),
                admin.getId(),
                null
        );
    }

    @Test
    public void testEnrollUserInActivity()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException,
            EnrollmentNotAllowedException, DuplicateEnrollmentException {

        UserDto user = createUser("enrollUser");
        ActivityDto activity = createActivity("Futbol", ActivityType.ACTIVITY);

        EnrollmentDto enrollment = enrollmentService.enroll(
                user.getId(),
                activity.getId()
        );

        assertNotNull(enrollment.getId());
        assertEquals(user.getId(), enrollment.getUserId());
        assertEquals(activity.getId(), enrollment.getActivityId());
        assertFalse(enrollment.isPaid());
        assertNull(enrollment.getPaymentDate());
    }

    @Test
    public void testDuplicateEnrollmentThrowsDuplicateEnrollmentException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException,
            EnrollmentNotAllowedException, DuplicateEnrollmentException {

        UserDto user = createUser("duplicateEnrollUser");
        ActivityDto activity = createActivity("Musica", ActivityType.ACTIVITY);

        enrollmentService.enroll(user.getId(), activity.getId());

        assertThrows(DuplicateEnrollmentException.class, () -> {
            enrollmentService.enroll(user.getId(), activity.getId());
        });
    }

    @Test
    public void testCannotEnrollInEvent()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto user = createUser("eventEnrollUser");
        ActivityDto event = createActivity("ReunionInformativa", ActivityType.EVENT);

        assertThrows(EnrollmentNotAllowedException.class, () -> {
            enrollmentService.enroll(user.getId(), event.getId());
        });
    }

    @Test
    public void testFindUserEnrollments()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException,
            EnrollmentNotAllowedException, DuplicateEnrollmentException {

        UserDto user = createUser("findEnrollmentsUser");
        ActivityDto activity = createActivity("Robotica", ActivityType.ACTIVITY);

        EnrollmentDto enrollment = enrollmentService.enroll(
                user.getId(),
                activity.getId()
        );

        List<EnrollmentDto> enrollments = enrollmentService.findUserEnrollments(
                user.getId()
        );

        assertFalse(enrollments.isEmpty());

        assertTrue(
                enrollments.stream()
                        .anyMatch(e -> e.getId().equals(enrollment.getId()))
        );
    }

    @Test
    public void testFindEnrollmentsByActivity()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException,
            EnrollmentNotAllowedException, DuplicateEnrollmentException {

        UserDto user = createUser("activityEnrollmentsUser");
        ActivityDto activity = createActivity("Dibujo", ActivityType.ACTIVITY);

        EnrollmentDto enrollment = enrollmentService.enroll(
                user.getId(),
                activity.getId()
        );

        List<EnrollmentDto> enrollments = enrollmentService.findEnrollmentsByActivity(
                activity.getId()
        );

        assertFalse(enrollments.isEmpty());

        assertTrue(
                enrollments.stream()
                        .anyMatch(e -> e.getId().equals(enrollment.getId()))
        );
    }

    @Test
    public void testUnsubscribeFromActivity()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException,
            EnrollmentNotAllowedException, DuplicateEnrollmentException {

        UserDto user = createUser("unsubscribeUser");
        ActivityDto activity = createActivity("Patinaje", ActivityType.ACTIVITY);

        enrollmentService.enroll(user.getId(), activity.getId());

        enrollmentService.unsubscribeFromActivity(
                user.getId(),
                activity.getId()
        );

        List<EnrollmentDto> enrollments = enrollmentService.findUserEnrollments(
                user.getId()
        );

        assertTrue(
                enrollments.stream()
                        .noneMatch(e -> e.getActivityId().equals(activity.getId()))
        );
    }

    @Test
    public void testUnsubscribeFromNonExistingEnrollmentThrowsInstanceNotFoundException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException {

        UserDto user = createUser("unsubscribeNonExistingUser");
        ActivityDto activity = createActivity("Manualidades", ActivityType.ACTIVITY);

        assertThrows(InstanceNotFoundException.class, () -> {
            enrollmentService.unsubscribeFromActivity(
                    user.getId(),
                    activity.getId()
            );
        });
    }

    @Test
    public void testFindUserEnrollmentsWithNonExistentUserThrowsInstanceNotFoundException() {
        assertThrows(InstanceNotFoundException.class, () -> {
            enrollmentService.findUserEnrollments(-1L);
        });
    }

    @Test
    public void testFindEnrollmentsByNonExistentActivityThrowsInstanceNotFoundException() {
        assertThrows(InstanceNotFoundException.class, () -> {
            enrollmentService.findEnrollmentsByActivity(-1L);
        });
    }
}