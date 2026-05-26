package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.DocumentRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Document;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.DocumentConverter;
import es.udc.anpaproject.backend.rest.dtos.DocumentDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class DocumentServiceImpl implements DocumentService {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path uploadDir = Paths.get("uploads", "documents");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Override
    @Transactional(readOnly = true)
    public Block<DocumentDto> findDocuments(String keywords, int page, int size) {

        PageRequest pageRequest = PageRequest.of(page, size);
        Slice<Document> slice;

        if (keywords == null || keywords.trim().isEmpty()) {
            slice = documentRepository.findByOrderByUploadedAtDesc(pageRequest);
        } else {
            slice = documentRepository.findByTitleContainingIgnoreCaseOrderByUploadedAtDesc(
                    keywords.trim(),
                    pageRequest
            );
        }

        return new Block<>(
                DocumentConverter.toDocumentDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public DocumentDto findById(Long documentId) throws InstanceNotFoundException {

        Document document = getDocument(documentId);

        return DocumentConverter.toDocumentDto(document);
    }

    @Override
    public DocumentDto uploadDocument(DocumentDto dto, Long userId, MultipartFile file)
        throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        List<DocumentDto> documents = uploadDocuments(dto, userId, new MultipartFile[] { file });

        return documents.get(0);
    }

    @Override
    public List<DocumentDto> uploadDocuments(DocumentDto dto, Long userId, MultipartFile[] files)
        throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        User user = getUser(userId);
        checkAdmin(user);

        if (files == null || files.length == 0) {
            throw new InvalidFileException("project.exceptions.document.emptyFile");
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        List<DocumentDto> savedDocuments = new ArrayList<>();

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;

            Path filePath = uploadDir.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath);

            Document document = DocumentConverter.toDocumentEntity(
                    dto,
                    user,
                    originalFileName,
                    filePath.toString(),
                    file.getContentType()
            );

            savedDocuments.add(DocumentConverter.toDocumentDto(documentRepository.save(document)));
        }

        return savedDocuments;
    }

    @Override
    @Transactional(readOnly = true)
    public Document getDocumentForDownload(Long documentId)
            throws InstanceNotFoundException {

        return getDocument(documentId);
    }

    @Override
    public void deleteDocument(Long documentId, Long userId)
            throws InstanceNotFoundException, PermissionException, IOException {

        User user = getUser(userId);
        checkAdmin(user);

        Document document = getDocument(documentId);

        Path path = Paths.get(document.getFilePath());

        if (Files.exists(path)) {
            Files.delete(path);
        }

        documentRepository.delete(document);
    }

    private User getUser(Long userId) throws InstanceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
    }

    private Document getDocument(Long documentId) throws InstanceNotFoundException {
        return documentRepository.findById(documentId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.document", documentId));
    }

    private void checkAdmin(User user) throws PermissionException {
        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }
    }

    private void validateFile(MultipartFile file) throws InvalidFileException {
        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("project.exceptions.document.emptyFile");
        }

        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new InvalidFileException("project.exceptions.document.invalidType");
        }
    }
}
