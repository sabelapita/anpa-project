package es.udc.anpaproject.backend.model.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;

import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.Enrollment;
import es.udc.anpaproject.backend.model.entities.PaymentStatus;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    @Value("${stripe.success-url}")
    private String successUrl;

    @Value("${stripe.cancel-url}")
    private String cancelUrl;

    @Override
    public String createCheckoutSession(Long enrollmentId, Long userId)
            throws InstanceNotFoundException, PermissionException, StripeException {

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new InstanceNotFoundException(
                        "project.entities.enrollment",
                        enrollmentId
                ));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new PermissionException();
        }

        if (enrollment.isPaid() || enrollment.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalStateException("La inscripción ya está pagada.");
        }

        Activity activity = enrollment.getActivity();

        if (activity.getPrice() == null || activity.getPrice() <= 0) {
            throw new IllegalStateException("La actividad es gratuita y no requiere pago.");
        }

        Integer amountCents = enrollment.getAmountCents();

        if (amountCents == null || amountCents <= 0) {
            amountCents = (int) Math.round(activity.getPrice() * 100);
            enrollment.setAmountCents(amountCents);
        }

        Stripe.apiKey = stripeSecretKey;

        SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl(successUrl + "?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl(cancelUrl + "?enrollmentId=" + enrollmentId)
                .putMetadata("enrollmentId", enrollmentId.toString())
                .putMetadata("userId", userId.toString())
                .addLineItem(
                        SessionCreateParams.LineItem.builder()
                                .setQuantity(1L)
                                .setPriceData(
                                        SessionCreateParams.LineItem.PriceData.builder()
                                                .setCurrency("eur")
                                                .setUnitAmount(amountCents.longValue())
                                                .setProductData(
                                                        SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                                .setName(activity.getTitle())
                                                                .build()
                                                )
                                                .build()
                                )
                                .build()
                )
                .build();

        Session session = Session.create(params);

        enrollment.setStripeSessionId(session.getId());
        enrollment.setPaymentStatus(PaymentStatus.PENDING);
        enrollmentRepository.save(enrollment);

        return session.getUrl();
    }
}