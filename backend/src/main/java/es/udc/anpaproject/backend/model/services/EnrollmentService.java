package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.DuplicateEnrollmentException;
import es.udc.anpaproject.backend.model.exceptions.EnrollmentNotAllowedException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;

import java.util.List;

public interface EnrollmentService {

    EnrollmentDto enroll(Long userId, Long activityId)
            throws InstanceNotFoundException,
            EnrollmentNotAllowedException,
            DuplicateEnrollmentException;

    List<EnrollmentDto> findUserEnrollments(Long userId)
            throws InstanceNotFoundException;

    List<EnrollmentDto> findEnrollmentsByActivity(Long activityId)
            throws InstanceNotFoundException;

    void unsubscribeFromActivity(Long userId, Long activityId)
            throws InstanceNotFoundException;
}