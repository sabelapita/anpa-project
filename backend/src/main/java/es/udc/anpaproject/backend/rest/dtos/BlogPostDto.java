package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class BlogPostDto {

    private Long id;
    private String title;
    private String content;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long authorId;
    private boolean published = true;
    private String category;
    private List<BlogPostAttachmentDto> attachments = new ArrayList<>();

    public BlogPostDto() {}

    public BlogPostDto(Long id, String title, String content,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   Long authorId, boolean published, String category) {

        this.id = id;
        this.title = title != null ? title.trim() : null;
        this.content = content != null ? content.trim() : null;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.authorId = authorId;
        this.published = published;
        this.category = category != null ? category.trim() : null;
    }

    public BlogPostDto(Long id, String title, String content,
                   LocalDateTime createdAt, LocalDateTime updatedAt,
                   Long authorId, boolean published, String category,
                   List<BlogPostAttachmentDto> attachments) {

        this(id, title, content, createdAt, updatedAt, authorId, published, category);
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @NotNull
    @Size(min = 1, max = 200)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    @NotNull
    @Size(min = 1, max = 5000)
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content != null ? content.trim() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public boolean isPublished() {
        return published;
    }

    public void setPublished(boolean published) {
        this.published = published;
    }

    @Size(max = 50)
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category != null ? category.trim() : null;
    }

    public List<BlogPostAttachmentDto> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<BlogPostAttachmentDto> attachments) {
        this.attachments = attachments != null ? attachments : new ArrayList<>();
    }
}

