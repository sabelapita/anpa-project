package es.udc.anpaproject.backend.test.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.*;
import es.udc.anpaproject.backend.model.entities.ActivityType;
import es.udc.anpaproject.backend.model.services.EnrollmentService;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
public class EnrollmentServiceTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @MockBean
    private EnrollmentRepository enrollmentRepository;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private ActivityRepository activityRepository;


    /* =========================== HELPERS =========================== */

    private User user(Long id) {
        User u = new User();
        u.setId(id);
        return u;
    }

    private Activity activity(Long id, ActivityType type) {
        Activity a = new Activity();
        ReflectionTestUtils.setField(a, "id", id);
        a.setType(type);
        return a;
    }


    /* =========================== ENROLL =========================== */

    @Test
    public void testEnrollCorrect() {
        User u = user(1L);
        Activity a = activity(10L, ActivityType.ACTIVITY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(a));
        when(enrollmentRepository.existsByUserAndActivity(u, a)).thenReturn(false);

        when(enrollmentRepository.save(any(Enrollment.class)))
            .thenAnswer(invocation -> {
                Enrollment e = invocation.getArgument(0);
                ReflectionTestUtils.setField(e, "id", 100L);
                return e;
            });

        EnrollmentDto dto = enrollmentService.enroll(1L, 10L);

        assertEquals(100L, dto.getId());
        assertEquals(1L, dto.getUserId());
        assertEquals(10L, dto.getActivityId());
    }


    @Test
    public void testEnrollEventForbidden() {
        User u = user(1L);
        Activity event = activity(10L, ActivityType.EVENT);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(event));

        assertThrows(RuntimeException.class,
                () -> enrollmentService.enroll(1L, 10L));
    }


    @Test
    public void testEnrollAlreadyExists() {
        User u = user(1L);
        Activity a = activity(10L, ActivityType.ACTIVITY);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(activityRepository.findById(10L)).thenReturn(Optional.of(a));
        when(enrollmentRepository.existsByUserAndActivity(u, a)).thenReturn(true);

        assertThrows(RuntimeException.class,
                () -> enrollmentService.enroll(1L, 10L));
    }


    @Test
    public void testEnrollUserNotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> enrollmentService.enroll(999L, 10L));
    }


    @Test
    public void testEnrollActivityNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user(1L)));
        when(activityRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> enrollmentService.enroll(1L, 10L));
    }


    /* =========================== FIND USER ENROLLMENTS =========================== */

    @Test
    public void testFindUserEnrollments() {

        User u = user(1L);
        Enrollment e = new Enrollment(u, activity(10L, ActivityType.ACTIVITY));
        ReflectionTestUtils.setField(e, "id", 200L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        when(enrollmentRepository.findByUser(u)).thenReturn(List.of(e));

        List<EnrollmentDto> list = enrollmentService.findUserEnrollments(1L);

        assertEquals(1, list.size());
        assertEquals(200L, list.get(0).getId());
    }


    /* =========================== FIND BY ACTIVITY =========================== */

    @Test
    public void testFindEnrollmentsByActivity() {

        Activity a = activity(10L, ActivityType.ACTIVITY);
        User u = user(1L);

        Enrollment e = new Enrollment(u, a);
        ReflectionTestUtils.setField(e, "id", 300L);

        when(activityRepository.findById(10L)).thenReturn(Optional.of(a));
        when(enrollmentRepository.findByActivity(a)).thenReturn(List.of(e));

        List<EnrollmentDto> list = enrollmentService.findEnrollmentsByActivity(10L);

        assertEquals(1, list.size());
        assertEquals(300L, list.get(0).getId());
    }


    /* =========================== UNSUBSCRIBE =========================== */

    @Test
    public void testUnsubscribeCorrect() {

        Activity a = activity(10L, ActivityType.ACTIVITY);
        User u = user(1L);

        Enrollment e = new Enrollment(u, a);
        ReflectionTestUtils.setField(e, "id", 400L);

        when(activityRepository.findById(10L)).thenReturn(Optional.of(a));
        when(enrollmentRepository.findByUserIdAndActivityId(1L, 10L))
                .thenReturn(Optional.of(e));

        assertDoesNotThrow(() ->
                enrollmentService.unsubscribeFromActivity(1L, 10L)
        );
    }


    @Test
    public void testUnsubscribeNotEnrolled() {

        Activity a = activity(10L, ActivityType.ACTIVITY);

        when(activityRepository.findById(10L)).thenReturn(Optional.of(a));
        when(enrollmentRepository.findByUserIdAndActivityId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> enrollmentService.unsubscribeFromActivity(1L, 10L));
    }


    @Test
    public void testUnsubscribeActivityNotFound() {

        when(activityRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> enrollmentService.unsubscribeFromActivity(1L, 10L));
    }
}
