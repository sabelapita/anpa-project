package es.udc.anpaproject.backend.rest.dtos;

public class MessageAttachmentDto {

    private Long id;
    private String fileName;
    private String contentType;

    public MessageAttachmentDto() {}

    public MessageAttachmentDto(Long id, String fileName, String contentType) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
    }

    public Long getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getContentType() {
        return contentType;
    }
}
