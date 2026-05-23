package es.udc.anpaproject.backend.model.services;

import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import es.udc.anpaproject.backend.model.daos.EnrollmentRepository;
import es.udc.anpaproject.backend.model.entities.Activity;
import es.udc.anpaproject.backend.model.entities.Enrollment;
import es.udc.anpaproject.backend.model.entities.PaymentStatus;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PaymentException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.PaymentResultDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.stripe.model.Event;


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

        @Value("${stripe.webhook-secret}")
        private String webhookSecret;

        @Override
       public String createCheckoutSession(Long enrollmentId, Long userId)
                throws InstanceNotFoundException, PermissionException, StripeException, PaymentException {
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new InstanceNotFoundException(
                                                "project.entities.enrollment",
                                                enrollmentId));

                if (!enrollment.getUser().getId().equals(userId)) {
                        throw new PermissionException();
                }

                if (enrollment.isPaid() || enrollment.getPaymentStatus() == PaymentStatus.PAID) {
                        throw new PaymentException("project.exceptions.payment.alreadyPaid");
                }

                Activity activity = enrollment.getActivity();

                if (activity.getPrice() == null || activity.getPrice() <= 0) {
                        throw new PaymentException("project.exceptions.payment.notRequired");
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
                                                                                SessionCreateParams.LineItem.PriceData
                                                                                                .builder()
                                                                                                .setCurrency("eur")
                                                                                                .setUnitAmount(amountCents
                                                                                                                .longValue())
                                                                                                .setProductData(
                                                                                                                SessionCreateParams.LineItem.PriceData.ProductData
                                                                                                                                .builder()
                                                                                                                                .setName(activity
                                                                                                                                                .getTitle())
                                                                                                                                .build())
                                                                                                .build())
                                                                .build())
                                .build();

        Session session = Session.create(params);

                enrollment.setStripeSessionId(session.getId());
                enrollment.setPaymentStatus(PaymentStatus.PENDING);
                enrollmentRepository.save(enrollment);

                return session.getUrl();
        }

        @Override
        public PaymentResultDto confirmCheckoutSession(String sessionId, Long userId)
                throws InstanceNotFoundException, PermissionException, StripeException, PaymentException {

                Stripe.apiKey = stripeSecretKey;

                Session session = Session.retrieve(sessionId);
                String enrollmentIdValue = session.getMetadata().get("enrollmentId");

                if (enrollmentIdValue == null) {
                        throw new PaymentException("project.exceptions.payment.invalidSession");
                }

                Long enrollmentId = Long.valueOf(enrollmentIdValue);
                Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                        .orElseThrow(() -> new InstanceNotFoundException(
                                "project.entities.enrollment",
                                enrollmentId
                        ));

                if (!enrollment.getUser().getId().equals(userId)) {
                        throw new PermissionException();
                }

                if ("paid".equals(session.getPaymentStatus())) {
                        enrollment.setPaid(true);
                        enrollment.setPaymentStatus(PaymentStatus.PAID);
                        enrollment.setPaymentDate(java.time.LocalDate.now());
                        enrollment.setStripePaymentIntentId(session.getPaymentIntent());
                        enrollment = enrollmentRepository.save(enrollment);
                }

                return new PaymentResultDto(
                        enrollment.getId(),
                        enrollment.getActivity().getId(),
                        enrollment.getPaymentStatus().toString(),
                        enrollment.isPaid()
                );
        }

        @Override
        public void handleStripeWebhook(String payload, String sigHeader) throws Exception {

                Stripe.apiKey = stripeSecretKey;

                Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);

                if ("checkout.session.completed".equals(event.getType())) {

                        ObjectMapper objectMapper = new ObjectMapper();

                        JsonNode root = objectMapper.readTree(
                                event.getDataObjectDeserializer().getRawJson()
                        );

                        String checkoutSessionId = root.get("id").asText();

                        Session session = Session.retrieve(checkoutSessionId);

                        String enrollmentIdValue = session.getMetadata().get("enrollmentId");

                        
                        if (enrollmentIdValue == null) {
                        return;
                        }

                        Long enrollmentId = Long.valueOf(enrollmentIdValue);

                        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                                .orElseThrow(() -> new InstanceNotFoundException(
                                        "project.entities.enrollment",
                                        enrollmentId
                                ));

                        enrollment.setPaid(true);
                        enrollment.setPaymentStatus(PaymentStatus.PAID);
                        enrollment.setPaymentDate(java.time.LocalDate.now());
                        enrollment.setStripePaymentIntentId(session.getPaymentIntent());

                        Enrollment saved = enrollmentRepository.saveAndFlush(enrollment);

                        
                }
        }
}
