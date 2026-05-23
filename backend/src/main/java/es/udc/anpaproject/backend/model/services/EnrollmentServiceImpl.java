package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.*;
import es.udc.anpaproject.backend.model.exceptions.DuplicateEnrollmentException;
import es.udc.anpaproject.backend.model.exceptions.EnrollmentNotAllowedException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentConverter;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EnrollmentServiceImpl implements EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Override
    public EnrollmentDto enroll(Long userId, Long activityId)
            throws InstanceNotFoundException,
            EnrollmentNotAllowedException,
            DuplicateEnrollmentException {

        User user = getUser(userId);
        Activity activity = getActivity(activityId);

        if (!canEnroll(activity, user)) {
            throw new EnrollmentNotAllowedException();
        }

        if (activity.getType() == ActivityType.EVENT) {
            throw new EnrollmentNotAllowedException();
        }

        if (enrollmentRepository.existsByUserAndActivity(user, activity)) {
            throw new DuplicateEnrollmentException();
        }

        Enrollment enrollment = new Enrollment(user, activity);

        if (activity.getPrice() == null || activity.getPrice() <= 0) {
                enrollment.setPaid(true);
                enrollment.setPaymentStatus(PaymentStatus.PAID);
                enrollment.setPaymentDate(LocalDate.now());
                enrollment.setAmountCents(0);
        } else {
                enrollment.setPaid(false);
                enrollment.setPaymentStatus(PaymentStatus.PENDING);
                enrollment.setPaymentDate(null);
                enrollment.setAmountCents((int) Math.round(activity.getPrice() * 100));
        }

        enrollment = enrollmentRepository.save(enrollment);

        return EnrollmentConverter.toEnrollmentDto(enrollment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDto> findUserEnrollments(Long userId)
            throws InstanceNotFoundException {

        User user = getUser(userId);

        return EnrollmentConverter.toEnrollmentDtos(
                enrollmentRepository.findByUser(user)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<EnrollmentDto> findEnrollmentsByActivity(Long activityId)
            throws InstanceNotFoundException {

        Activity activity = getActivity(activityId);

        return EnrollmentConverter.toEnrollmentDtos(
                enrollmentRepository.findByActivity(activity)
        );
    }

    @Override
    public void unsubscribeFromActivity(Long userId, Long activityId)
            throws InstanceNotFoundException {

        getActivity(activityId);

        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.enrollment",
                                activityId
                        )
                );

        enrollmentRepository.delete(enrollment);
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

    private Activity getActivity(Long activityId)
            throws InstanceNotFoundException {

        return activityRepository.findById(activityId)
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.activity",
                                activityId
                        )
                );
    }

    private boolean canEnroll(Activity activity, User user) {
        if (activity.getVisibility() == ActivityVisibility.MEMBERS_ONLY) {
            return user.getMembershipStatus() == MembershipStatus.MEMBER;
        }

        return activity.getVisibility() == ActivityVisibility.PUBLIC ||
                activity.getVisibility() == ActivityVisibility.REGISTERED;
    }
}
