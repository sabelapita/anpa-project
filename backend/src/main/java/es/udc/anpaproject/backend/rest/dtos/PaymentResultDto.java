package es.udc.anpaproject.backend.rest.dtos;

public class PaymentResultDto {

    private Long enrollmentId;
    private Long activityId;
    private String paymentStatus;
    private boolean paid;

    public PaymentResultDto() {}

    public PaymentResultDto(Long enrollmentId, Long activityId, String paymentStatus, boolean paid) {
        this.enrollmentId = enrollmentId;
        this.activityId = activityId;
        this.paymentStatus = paymentStatus;
        this.paid = paid;
    }

    public Long getEnrollmentId() {
        return enrollmentId;
    }

    public void setEnrollmentId(Long enrollmentId) {
        this.enrollmentId = enrollmentId;
    }

    public Long getActivityId() {
        return activityId;
    }

    public void setActivityId(Long activityId) {
        this.activityId = activityId;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public void setPaymentStatus(String paymentStatus) {
        this.paymentStatus = paymentStatus;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }
}
