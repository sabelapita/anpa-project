/*package es.udc.anpaproject.backend.model.entities;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "Enrollment")
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "userId")  
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "activityId")  
    private Activity activity;

    private boolean paid = false;

    private LocalDate paymentDate;

    public Enrollment() {}

    public Enrollment(User user, Activity activity) {
        this.user = user;
        this.activity = activity;
        this.paid = false;
        this.paymentDate = null;
    }

    // Getters y Setters
    public Long getId() { return id; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
    public Activity getActivity() { return activity; }
    public void setActivity(Activity activity) { this.activity = activity; }
    public boolean isPaid() { return paid; }
    public void setPaid(boolean paid) { this.paid = paid; }
    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { this.paymentDate = paymentDate; }
}*/
