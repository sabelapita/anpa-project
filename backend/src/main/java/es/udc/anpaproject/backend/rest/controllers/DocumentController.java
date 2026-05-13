package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.entities.Document;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.DocumentService;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;
import es.udc.anpaproject.backend.rest.dtos.DocumentDto;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@RestController
@RequestMapping("/documents")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    @GetMapping
    public BlockDto<DocumentDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Block<DocumentDto> block = documentService.findDocuments(page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @GetMapping("/{documentId}")
    public DocumentDto getById(@PathVariable Long documentId)
            throws InstanceNotFoundException {

        return documentService.findById(documentId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    public DocumentDto upload(
            @Valid @RequestPart("data") DocumentDto dto,
            @RequestPart("file") MultipartFile file,
            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException, IOException,InvalidFileException {

        return documentService.uploadDocument(dto, userId, file);
    }

    @GetMapping("/{documentId}/download")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long documentId)
            throws InstanceNotFoundException, IOException {

        Document document = documentService.getDocumentForDownload(documentId);

        Path path = Paths.get(document.getFilePath());

        InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

        String contentType = document.getContentType() != null
                ? document.getContentType()
                : MediaType.APPLICATION_OCTET_STREAM_VALUE;

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + document.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(contentType))
                .body(resource);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{documentId}")
    public void delete(@PathVariable Long documentId,
                       @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException, IOException {

        documentService.deleteDocument(documentId, userId);
    }
}