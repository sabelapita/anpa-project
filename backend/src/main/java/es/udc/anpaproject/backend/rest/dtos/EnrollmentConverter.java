package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.Enrollment;

import java.util.List;
import java.util.stream.Collectors;

public class EnrollmentConverter {

    public static EnrollmentDto toEnrollmentDto(Enrollment e) {
        return new EnrollmentDto(
                e.getId(),
                e.getUser().getId(),
                e.getActivity().getId(),
                e.isPaid(),
                e.getPaymentDate(),
                e.getPaymentStatus(),
                e.getAmountCents()
        );
    }

    public static List<EnrollmentDto> toEnrollmentDtos(List<Enrollment> enrollments) {
        return enrollments.stream()
                .map(EnrollmentConverter::toEnrollmentDto)
                .collect(Collectors.toList());
    }
}
