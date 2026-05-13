package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.stripe.exception.StripeException;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.*;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PaymentException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.PaymentService;

@SpringBootTest
@Transactional
public class PaymentServiceTest {

    @Autowired
    private PaymentService paymentService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private User createUser(String username) {

        User user = new User();

        user.setUserName(username);
        user.setPassword("password");
        user.setFirstName("Test");
        user.setLastName("User");
        user.setEmail(username + "@test.com");
        user.setRole(RoleType.USER);

        return userRepository.save(user);
    }

    private Activity createActivity(Double price) {

        Activity activity = new Activity();

        activity.setTitle("Actividad de prueba");
        activity.setDescription("Descripción");
        activity.setType(ActivityType.ACTIVITY);
        activity.setPrice(price);
        activity.setStartDate(LocalDate.now().plusDays(1));
        activity.setEndDate(LocalDate.now().plusDays(2));

        return activityRepository.save(activity);
    }

    private Enrollment createEnrollment(User user, Activity activity) {

        Enrollment enrollment = new Enrollment(user, activity);

        enrollment.setPaid(false);
        enrollment.setPaymentStatus(PaymentStatus.PENDING);

        return enrollmentRepository.save(enrollment);
    }

    @Test
    public void testCreateCheckoutSessionWithNonExistingEnrollment()
            throws StripeException {

        assertThrows(
                InstanceNotFoundException.class,
                () -> paymentService.createCheckoutSession(-1L, 1L)
        );
    }

    @Test
    public void testCreateCheckoutSessionWithDifferentUser()
            throws StripeException {

        User owner = createUser("ownerPayment");
        User intruder = createUser("intruderPayment");

        Activity activity = createActivity(20.0);

        Enrollment enrollment = createEnrollment(owner, activity);

        assertThrows(
                PermissionException.class,
                () -> paymentService.createCheckoutSession(
                        enrollment.getId(),
                        intruder.getId()
                )
        );
    }

    @Test
    public void testCreateCheckoutSessionAlreadyPaid()
            throws StripeException {

        User user = createUser("paidUser");

        Activity activity = createActivity(20.0);

        Enrollment enrollment = createEnrollment(user, activity);

        enrollment.setPaid(true);
        enrollment.setPaymentStatus(PaymentStatus.PAID);

        enrollmentRepository.save(enrollment);

        assertThrows(
                PaymentException.class,
                () -> paymentService.createCheckoutSession(
                        enrollment.getId(),
                        user.getId()
                )
        );
    }

    @Test
    public void testCreateCheckoutSessionForFreeActivity()
            throws StripeException {

        User user = createUser("freeActivityUser");

        Activity activity = createActivity(0.0);

        Enrollment enrollment = createEnrollment(user, activity);

        assertThrows(
                PaymentException.class,
                () -> paymentService.createCheckoutSession(
                        enrollment.getId(),
                        user.getId()
                )
        );
    }
}