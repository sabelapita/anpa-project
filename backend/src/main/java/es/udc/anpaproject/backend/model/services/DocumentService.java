package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.Document;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.DocumentDto;

import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface DocumentService {

    Block<DocumentDto> findDocuments(String keywords, int page, int size);

    DocumentDto findById(Long documentId) throws InstanceNotFoundException;

    DocumentDto uploadDocument(DocumentDto dto, Long userId, MultipartFile file)
        throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException;

    List<DocumentDto> uploadDocuments(DocumentDto dto, Long userId, MultipartFile[] files)
        throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException;

    Document getDocumentForDownload(Long documentId)
            throws InstanceNotFoundException;

    void deleteDocument(Long documentId, Long userId)
            throws InstanceNotFoundException, PermissionException, IOException;
}
