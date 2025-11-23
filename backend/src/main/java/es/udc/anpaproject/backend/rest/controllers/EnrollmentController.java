package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.rest.dtos.EnrollmentDto;
import es.udc.anpaproject.backend.model.services.EnrollmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import java.util.List;
@RestController
@RequestMapping("/enrollments")
public class EnrollmentController {

    @Autowired
    private EnrollmentService enrollmentService;

    /** Inscribir al usuario en una actividad */
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/{activityId}")
    public EnrollmentDto enroll(@PathVariable Long activityId, @RequestAttribute Long userId) {
        return enrollmentService.enroll(userId, activityId);
    }

    /** Mis inscripciones */
    @PreAuthorize("hasRole('USER')")
    @GetMapping("/my")
    public List<EnrollmentDto> myEnrollments(@RequestAttribute Long userId) {
        return enrollmentService.findUserEnrollments(userId);
    }


    /** Inscripciones por actividad */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/activity/{activityId}")
    public List<EnrollmentDto> getEnrollmentsByActivity(@PathVariable Long activityId) {
        return enrollmentService.findEnrollmentsByActivity(activityId);
    }

    /** Desinscribirse de una actividad */
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/{activityId}")
    public void unsubscribe(@PathVariable Long activityId, @RequestAttribute Long userId) {
        enrollmentService.unsubscribeFromActivity(userId, activityId);
    }
}
