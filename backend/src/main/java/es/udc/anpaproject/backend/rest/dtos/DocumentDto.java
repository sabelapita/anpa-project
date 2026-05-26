package es.udc.anpaproject.backend.rest.dtos;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class DocumentDto {

    private Long id;
    private String title;
    private String description;
    private String fileName;
    private String contentType;
    private LocalDateTime uploadedAt;
    private Long uploadedBy;

    public DocumentDto() {}

    public DocumentDto(Long id, String title, String description,
                       String fileName, String contentType,
                       LocalDateTime uploadedAt, Long uploadedBy) {

        this.id = id;
        this.title = title != null ? title.trim() : null;
        this.description = description != null ? description.trim() : null;
        this.fileName = fileName;
        this.contentType = contentType;
        this.uploadedAt = uploadedAt;
        this.uploadedBy = uploadedBy;
    }

    public Long getId() {
        return id;
    }

    @NotNull
    @Size(min = 1, max = 100)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title != null ? title.trim() : null;
    }

    @Size(max = 250)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description.trim() : null;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    public Long getUploadedBy() {
        return uploadedBy;
    }
}
