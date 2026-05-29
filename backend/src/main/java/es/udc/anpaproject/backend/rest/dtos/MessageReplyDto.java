package es.udc.anpaproject.backend.rest.dtos;

import java.time.LocalDateTime;

public class MessageReplyDto {

    private Long id;
    private boolean adminReply;
    private String content;
    private LocalDateTime sentAt;

    public MessageReplyDto() {}

    public MessageReplyDto(Long id, boolean adminReply, String content, LocalDateTime sentAt) {
        this.id = id;
        this.adminReply = adminReply;
        this.content = content != null ? content.trim() : null;
        this.sentAt = sentAt;
    }

    public Long getId() {
        return id;
    }

    public boolean isAdminReply() {
        return adminReply;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getSentAt() {
        return sentAt;
    }
}
