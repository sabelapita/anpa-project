package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class MessageDto {

    private Long id;
    private Long userId;
    private String subject;
    private String content;
    private LocalDateTime sentAt;
    private String response;
    private LocalDateTime respondedAt;
    private String userName;
    private String userFullName;
    private List<MessageAttachmentDto> attachments = new ArrayList<>();
    private List<MessageReplyDto> replies = new ArrayList<>();

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

    public MessageDto(Long id, Long userId, String subject, String content,
                      LocalDateTime sentAt, String response, LocalDateTime respondedAt,
                      String userName, String userFullName, List<MessageAttachmentDto> attachments,
                      List<MessageReplyDto> replies) {

        this(id, userId, subject, content, sentAt, response, respondedAt);
        this.userName = userName;
        this.userFullName = userFullName;
        this.attachments = attachments != null ? attachments : new ArrayList<>();
        this.replies = replies != null ? replies : new ArrayList<>();
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

    public String getUserName() {
        return userName;
    }

    public String getUserFullName() {
        return userFullName;
    }

    public List<MessageAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<MessageAttachmentDto> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }

    public List<MessageReplyDto> getReplies() {
        return replies;
    }

    public void setReplies(List<MessageReplyDto> replies) {
        this.replies = replies != null ? replies : new ArrayList<>();
    }
}
