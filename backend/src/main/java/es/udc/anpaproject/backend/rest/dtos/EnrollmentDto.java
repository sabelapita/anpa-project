package es.udc.anpaproject.backend.rest.dtos;

import java.time.LocalDate;

public class EnrollmentDto {
    private Long id;
    private Long userId;
    private Long activityId;
    private boolean paid;
    private LocalDate paymentDate;

    public EnrollmentDto() {}

    public EnrollmentDto(Long id, Long userId, Long activityId, boolean paid, LocalDate paymentDate) {
        this.id = id;
        this.userId = userId;
        this.activityId = activityId;
        this.paid = paid;
        this.paymentDate = paymentDate;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getActivityId() { return activityId; }
    public void setActivityId(Long activityId) { this.activityId = activityId; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}
