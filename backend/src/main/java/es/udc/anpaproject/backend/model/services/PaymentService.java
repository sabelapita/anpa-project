package es.udc.anpaproject.backend.model.services;

import com.stripe.exception.StripeException;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;

public interface PaymentService {

    String createCheckoutSession(Long enrollmentId, Long userId)
            throws InstanceNotFoundException, PermissionException, StripeException;
}