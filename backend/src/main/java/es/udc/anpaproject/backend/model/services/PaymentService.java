package es.udc.anpaproject.backend.model.services;

import com.stripe.exception.StripeException;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PaymentException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.PaymentResultDto;

public interface PaymentService {

    String createCheckoutSession(Long enrollmentId, Long userId)
        throws InstanceNotFoundException, PermissionException, StripeException, PaymentException;

    PaymentResultDto confirmCheckoutSession(String sessionId, Long userId)
        throws InstanceNotFoundException, PermissionException, StripeException, PaymentException;

    void handleStripeWebhook(String payload, String sigHeader) throws Exception;
}
