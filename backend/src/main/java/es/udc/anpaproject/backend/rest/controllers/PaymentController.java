package es.udc.anpaproject.backend.rest.controllers;

import com.stripe.exception.StripeException;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.PaymentService;
import es.udc.anpaproject.backend.rest.dtos.PaymentCheckoutDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/payments")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/checkout/enrollments/{enrollmentId}")
    public PaymentCheckoutDto createCheckoutSession(
            @PathVariable Long enrollmentId,
            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException, StripeException {

        String checkoutUrl = paymentService.createCheckoutSession(enrollmentId, userId);

        return new PaymentCheckoutDto(checkoutUrl);
    }
}