package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Document;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.DocumentService;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.dtos.DocumentDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
public class DocumentServiceTest {

    @Autowired
    private DocumentService documentService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private Path lastUploadedPath;

    @AfterEach
    public void cleanFiles() throws IOException {
        if (lastUploadedPath != null && Files.exists(lastUploadedPath)) {
            Files.delete(lastUploadedPath);
        }
    }

    private UserDto createUser(String userName) throws DuplicateInstanceException {
        UserDto userDto = new UserDto(
                null,
                userName,
                "Test",
                "User",
                userName + "@udc.es",
                "USER"
        );

        userDto.setPassword("password");

        return userService.signUp(userDto);
    }

    private UserDto createAdmin(String userName)
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto userDto = createUser(userName);

        User user = userRepository.findById(userDto.getId())
                .orElseThrow(() ->
                        new InstanceNotFoundException(
                                "project.entities.user",
                                userDto.getId()
                        )
                );

        user.setRole(RoleType.ADMIN);
        userRepository.save(user);

        return userDto;
    }

    private DocumentDto createDocumentDto() {
        DocumentDto dto = new DocumentDto();
        dto.setTitle("Circular inicio de curso");
        dto.setDescription("Documento informativo para las familias");
        return dto;
    }

    private MockMultipartFile createMockFile() {
        return new MockMultipartFile(
                "file",
                "circular.pdf",
                "application/pdf",
                "Contenido de prueba del documento".getBytes()
        );
    }

    @Test
    public void testUploadDocumentAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException {

        UserDto admin = createAdmin("adminDocs");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        assertNotNull(savedDocument.getId());
        assertEquals("Circular inicio de curso", savedDocument.getTitle());
        assertEquals("Documento informativo para las familias", savedDocument.getDescription());
        assertEquals("circular.pdf", savedDocument.getFileName());
        assertEquals("application/pdf", savedDocument.getContentType());
        assertEquals(admin.getId(), savedDocument.getUploadedBy());
        assertNotNull(savedDocument.getUploadedAt());

        Document document = documentService.getDocumentForDownload(savedDocument.getId());

        assertTrue(Files.exists(Paths.get(document.getFilePath())));

        lastUploadedPath = Paths.get(document.getFilePath());
    }

    @Test
    public void testUploadDocumentAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException {

        UserDto user = createUser("normalDocs");

        assertThrows(PermissionException.class, () -> {
            documentService.uploadDocument(
                    createDocumentDto(),
                    user.getId(),
                    createMockFile()
            );
        });
    }

    @Test
    public void testUploadEmptyFileThrowsInvalidFileException()
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto admin = createAdmin("adminEmptyFile");

        MockMultipartFile emptyFile = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        assertThrows(InvalidFileException.class, () -> {
            documentService.uploadDocument(
                    createDocumentDto(),
                    admin.getId(),
                    emptyFile
            );
        });
    }

    @Test
    public void testFindById()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException  {

        UserDto admin = createAdmin("adminFindDoc");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        DocumentDto foundDocument = documentService.findById(savedDocument.getId());

        assertEquals(savedDocument.getId(), foundDocument.getId());
        assertEquals("Circular inicio de curso", foundDocument.getTitle());
        assertEquals("circular.pdf", foundDocument.getFileName());
        assertEquals(admin.getId(), foundDocument.getUploadedBy());

        Document document = documentService.getDocumentForDownload(savedDocument.getId());
        lastUploadedPath = Paths.get(document.getFilePath());
    }

    @Test
    public void testGetDocumentForDownload()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException  {

        UserDto admin = createAdmin("adminDownloadDoc");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        Document document = documentService.getDocumentForDownload(savedDocument.getId());

        assertNotNull(document);
        assertEquals(savedDocument.getId(), document.getId());
        assertEquals("circular.pdf", document.getFileName());
        assertEquals("application/pdf", document.getContentType());
        assertTrue(Files.exists(Paths.get(document.getFilePath())));

        lastUploadedPath = Paths.get(document.getFilePath());
    }

    @Test
    public void testFindDocuments()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException  {

        UserDto admin = createAdmin("adminListDocs");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        Block<DocumentDto> documents = documentService.findDocuments(0, 10);

        assertFalse(documents.getItems().isEmpty());

        assertTrue(
                documents.getItems().stream()
                        .anyMatch(document ->
                                document.getId().equals(savedDocument.getId())
                        )
        );

        Document document = documentService.getDocumentForDownload(savedDocument.getId());
        lastUploadedPath = Paths.get(document.getFilePath());
    }

    @Test
    public void testDeleteDocumentAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException  {

        UserDto admin = createAdmin("adminDeleteDoc");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        Document document = documentService.getDocumentForDownload(savedDocument.getId());
        Path filePath = Paths.get(document.getFilePath());

        assertTrue(Files.exists(filePath));

        documentService.deleteDocument(savedDocument.getId(), admin.getId());

        assertFalse(Files.exists(filePath));

        assertThrows(InstanceNotFoundException.class, () -> {
            documentService.findById(savedDocument.getId());
        });
    }

    @Test
    public void testDeleteDocumentAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException, IOException, InvalidFileException  {

        UserDto admin = createAdmin("adminDeletePermission");
        UserDto normalUser = createUser("normalDeletePermission");

        DocumentDto savedDocument = documentService.uploadDocument(
                createDocumentDto(),
                admin.getId(),
                createMockFile()
        );

        Document document = documentService.getDocumentForDownload(savedDocument.getId());
        lastUploadedPath = Paths.get(document.getFilePath());

        assertThrows(PermissionException.class, () -> {
            documentService.deleteDocument(
                    savedDocument.getId(),
                    normalUser.getId()
            );
        });

        assertTrue(Files.exists(lastUploadedPath));
    }

    @Test
    public void testFindNonExistentDocumentThrowsInstanceNotFoundException() {
        assertThrows(InstanceNotFoundException.class, () -> {
            documentService.findById(-1L);
        });
    }
}