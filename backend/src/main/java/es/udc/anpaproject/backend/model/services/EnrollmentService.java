package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.*;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentConverter;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class EnrollmentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ActivityRepository activityRepository;

    public EnrollmentDto enroll(Long userId, Long activityId)
            throws InstanceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.activity", activityId));

        if (activity.getType() == ActivityType.EVENT) {
            throw new RuntimeException("No puedes inscribirte en un evento");
        }

        if (enrollmentRepository.existsByUserAndActivity(user, activity)) {
            throw new RuntimeException("El usuario ya está inscrito en esta actividad");
        }

        Enrollment enrollment = new Enrollment(user, activity);
        enrollment = enrollmentRepository.save(enrollment);

        return EnrollmentConverter.toEnrollmentDto(enrollment);
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> findUserEnrollments(Long userId)
            throws InstanceNotFoundException {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));

        return EnrollmentConverter.toEnrollmentDtos(enrollmentRepository.findByUser(user));
    }

    @Transactional(readOnly = true)
    public List<EnrollmentDto> findEnrollmentsByActivity(Long activityId)
            throws InstanceNotFoundException {

        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.activity", activityId));

        return EnrollmentConverter.toEnrollmentDtos(enrollmentRepository.findByActivity(activity));
    }

    public void unsubscribeFromActivity(Long userId, Long activityId)
            throws InstanceNotFoundException {

        activityRepository.findById(activityId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.activity", activityId));

        Enrollment enrollment = enrollmentRepository
                .findByUserIdAndActivityId(userId, activityId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.enrollment", activityId));

        enrollmentRepository.delete(enrollment);
    }
}