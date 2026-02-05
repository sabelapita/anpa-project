package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.*;
import es.udc.anpaproject.backend.model.daos.ActivityRepository;
import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentConversor;
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


    public EnrollmentDto enroll(Long userId, Long activityId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        // Evitar inscripción en eventos si procede
        if (activity.getType() == ActivityType.EVENT) {
            throw new RuntimeException("No puedes inscribirte en un evento");
        }

        if (enrollmentRepository.existsByUserAndActivity(user, activity)) {
            throw new RuntimeException("El usuario ya está inscrito en esta actividad");
        }

        Enrollment enrollment = new Enrollment(user, activity);  // paymentDate = null
        enrollment = enrollmentRepository.save(enrollment);

        return EnrollmentConversor.toEnrollmentDto(enrollment);
    }


    /** Enrollments de un usuario */
    public List<EnrollmentDto> findUserEnrollments(Long userId) {

        User user = userRepository.findById(userId)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return EnrollmentConversor.toEnrollmentDtos(enrollmentRepository.findByUser(user));
    }


    /** Enrollments de una actividad */
    public List<EnrollmentDto> findEnrollmentsByActivity(Long activityId) {

        Activity activity = activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        return EnrollmentConversor.toEnrollmentDtos(enrollmentRepository.findByActivity(activity));
    }


    /** Cancelar inscripción */
    public void unsubscribeFromActivity(Long userId, Long activityId) {

        // Validamos actividad para no devolver error engañoso
        activityRepository.findById(activityId)
            .orElseThrow(() -> new RuntimeException("Actividad no encontrada"));

        Enrollment enrollment = enrollmentRepository
            .findByUserIdAndActivityId(userId, activityId)
            .orElseThrow(() -> new RuntimeException("No estás inscrito en esta actividad"));

        enrollmentRepository.delete(enrollment);
    }
}
