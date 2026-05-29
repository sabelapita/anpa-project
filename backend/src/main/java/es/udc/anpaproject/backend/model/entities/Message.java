package es.udc.anpaproject.backend.model.entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "Message")
public class Message {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "userId", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String subject;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false, updatable = false)
    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String response;

    private LocalDateTime respondedAt;

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MessageAttachment> attachments = new ArrayList<>();

    @OneToMany(mappedBy = "message", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("sentAt ASC")
    private List<MessageReply> replies = new ArrayList<>();

    public Message() {}

    public Message(User user, String subject, String content) {
        this.user = user;
        this.subject = subject;
        this.content = content;
    }

    @PrePersist
    public void prePersist() {
        if (sentAt == null) {
            sentAt = LocalDateTime.now();
        }
    }

    public Long getId() { return id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) {
        this.subject = subject != null ? subject.trim() : null;
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content != null ? content.trim() : null;
    }

    public LocalDateTime getSentAt() { return sentAt; }

    public String getResponse() { return response; }
    public void setResponse(String response) {
        this.response = response != null ? response.trim() : null;
    }

    public LocalDateTime getRespondedAt() { return respondedAt; }
    public void setRespondedAt(LocalDateTime respondedAt) {
        this.respondedAt = respondedAt;
    }

    public List<MessageAttachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MessageAttachment> attachments) {
        this.attachments = attachments;
    }

    public void addAttachment(MessageAttachment attachment) {
        attachment.setMessage(this);
        attachments.add(attachment);
    }

    public List<MessageReply> getReplies() {
        return replies;
    }

    public void setReplies(List<MessageReply> replies) {
        this.replies = replies;
    }

    public void addReply(MessageReply reply) {
        reply.setMessage(this);
        replies.add(reply);
    }
}
