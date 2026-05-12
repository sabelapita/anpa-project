package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.Document;
import es.udc.anpaproject.backend.model.entities.User;

import java.util.List;
import java.util.stream.Collectors;

public class DocumentConverter {

    private DocumentConverter() {}

    public static DocumentDto toDocumentDto(Document document) {
        return new DocumentDto(
                document.getId(),
                document.getTitle(),
                document.getDescription(),
                document.getFileName(),
                document.getContentType(),
                document.getUploadedAt(),
                document.getUploadedBy().getId()
        );
    }

    public static List<DocumentDto> toDocumentDtos(List<Document> documents) {
        return documents.stream()
                .map(DocumentConverter::toDocumentDto)
                .collect(Collectors.toList());
    }

    public static Document toDocumentEntity(DocumentDto dto, User uploadedBy,
                                            String fileName, String filePath,
                                            String contentType) {

        Document document = new Document();
        document.setTitle(dto.getTitle());
        document.setDescription(dto.getDescription());
        document.setFileName(fileName);
        document.setFilePath(filePath);
        document.setContentType(contentType);
        document.setUploadedBy(uploadedBy);

        return document;
    }
}