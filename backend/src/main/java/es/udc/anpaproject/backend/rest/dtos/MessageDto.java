package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class MessageDto {

    private Long id;
    private Long userId;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private String response;
    private LocalDateTime respondedAt;

    public MessageDto() {}

    public MessageDto(Long id, Long userId, String subject, String content,
                      LocalDateTime sentAt, String response, LocalDateTime respondedAt) {

        this.id = id;
        this.userId = userId;
        this.subject = subject != null ? subject.trim() : null;
        this.content = content != null ? content.trim() : null;
        this.sentAt = sentAt;
        this.response = response != null ? response.trim() : null;
        this.respondedAt = respondedAt;
    }

    public Long getId() {
        return id;
    }

    public Long getUserId() {
        return userId;
    }

    @NotNull
    @Size(min = 1, max = 100)
    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject != null ? subject.trim() : null;
    }

    @NotNull
    @Size(min = 1, max = 5000)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content.trim() : null;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }

    @Size(max = 5000)
    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response != null ? response.trim() : null;
    }

    public LocalDateTime getRespondedAt() {
        return respondedAt;
    }
}