package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.MessageService;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;

@SpringBootTest
@Transactional
public class MessageServiceTest {

    @Autowired
    private MessageService messageService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

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

    private MessageDto createMessageDto() {
        MessageDto dto = new MessageDto();
        dto.setSubject("Consulta sobre actividad");
        dto.setContent("Quería consultar información sobre una actividad extraescolar.");
        return dto;
    }

    @Test
    public void testCreateMessageAsUser()
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto user = createUser("messageUser");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId()
        );

        assertNotNull(savedMessage.getId());
        assertEquals(user.getId(), savedMessage.getUserId());
        assertEquals("Consulta sobre actividad", savedMessage.getSubject());
        assertEquals(
                "Quería consultar información sobre una actividad extraescolar.",
                savedMessage.getContent()
        );
        assertNotNull(savedMessage.getSentAt());
        assertNull(savedMessage.getResponse());
        assertNull(savedMessage.getRespondedAt());
    }

    @Test
    public void testCreateMessageWithAttachment()
            throws DuplicateInstanceException, InstanceNotFoundException,
            IOException, InvalidFileException, PermissionException {

        UserDto user = createUser("messageAttachmentUser");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId(),
                new MockMultipartFile[] {
                        new MockMultipartFile(
                                "files",
                                "justificante.pdf",
                                "application/pdf",
                                "contenido".getBytes()
                        )
                }
        );

        assertEquals(1, savedMessage.getAttachments().size());
        assertEquals("justificante.pdf", savedMessage.getAttachments().get(0).getFileName());

        MessageAttachment attachment = messageService.getAttachmentForDownload(
                savedMessage.getAttachments().get(0).getId(),
                user.getId()
        );

        assertEquals("justificante.pdf", attachment.getFileName());
    }

    @Test
    public void testFindUserMessages()
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto user = createUser("findUserMessages");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId()
        );

        Block<MessageDto> messages = messageService.findUserMessages(
                user.getId(),
                0,
                10
        );

        assertFalse(messages.getItems().isEmpty());

        assertTrue(
                messages.getItems().stream()
                        .anyMatch(message ->
                                message.getId().equals(savedMessage.getId())
                        )
        );
    }

    @Test
    public void testFindAllMessagesAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException {

        UserDto user = createUser("userAllMessages");
        UserDto admin = createAdmin("adminAllMessages");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId()
        );

        Block<MessageDto> messages = messageService.findAllMessages(
                admin.getId(),
                0,
                10
        );

        assertFalse(messages.getItems().isEmpty());

        assertTrue(
                messages.getItems().stream()
                        .anyMatch(message ->
                                message.getId().equals(savedMessage.getId())
                        )
        );
    }

    @Test
    public void testFindAllMessagesAsAdminDoesNotReturnBlogNotices()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException {

        UserDto user = createUser("userBlogNoticeHidden");
        UserDto admin = createAdmin("adminBlogNoticeHidden");

        MessageDto notice = new MessageDto();
        notice.setSubject("Nueva entrada del blog");
        notice.setContent("Se ha publicado una nueva entrada.");

        MessageDto savedNotice = messageService.createMessage(
                notice,
                user.getId()
        );

        Block<MessageDto> messages = messageService.findAllMessages(
                admin.getId(),
                0,
                10
        );

        assertFalse(
                messages.getItems().stream()
                        .anyMatch(message -> message.getId().equals(savedNotice.getId()))
        );
    }

    @Test
    public void testFindAllMessagesAsNormalUserThrowsPermissionException()
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto user = createUser("normalAllMessages");

        assertThrows(PermissionException.class, () -> {
            messageService.findAllMessages(user.getId(), 0, 10);
        });
    }

    @Test
    public void testFindByIdAsOwner()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException {

        UserDto user = createUser("ownerMessage");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId()
        );

        MessageDto foundMessage = messageService.findById(
                savedMessage.getId(),
                user.getId()
        );

        assertEquals(savedMessage.getId(), foundMessage.getId());
        assertEquals(user.getId(), foundMessage.getUserId());
        assertEquals("Consulta sobre actividad", foundMessage.getSubject());
    }

    @Test
    public void testFindByIdAsAdmin()
            throws DuplicateInstanceException, InstanceNotFoundException,
            PermissionException {

        UserDto user = createUser("userMessageAdminFind");
        UserDto admin = createAdmin("adminFindMessage");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user.getId()
        );

        MessageDto foundMessage = messageService.findById(
                savedMessage.getId(),
                admin.getId()
        );

        assertEquals(savedMessage.getId(), foundMessage.getId());
        assertEquals(user.getId(), foundMessage.getUserId());
    }

    @Test
    public void testUserCannotFindOtherUserMessage()
            throws DuplicateInstanceException, InstanceNotFoundException {

        UserDto user1 = createUser("messageOwner");
        UserDto user2 = createUser("messageIntruder");

        MessageDto savedMessage = messageService.createMessage(
                createMessageDto(),
                user1.getId()
        );

        assertThrows(PermissionException.class, () -> {
            messageService.findById(savedMessage.getId(), user2.getId());
        });
    }

    @Test
public void testRespondMessageAsAdmin()
        throws DuplicateInstanceException, InstanceNotFoundException,
        PermissionException {

    UserDto user = createUser("userResponded");
    UserDto admin = createAdmin("adminResponds");

    MessageDto savedMessage = messageService.createMessage(
            createMessageDto(),
            user.getId()
    );

    MessageDto respondedMessage = messageService.respondMessage(
            savedMessage.getId(),
            admin.getId(),
            "La información solicitada estará disponible próximamente."
    );

    assertEquals(savedMessage.getId(), respondedMessage.getId());
    assertEquals(
            "La información solicitada estará disponible próximamente.",
            respondedMessage.getResponse()
    );
    assertNotNull(respondedMessage.getRespondedAt());
    assertEquals(1, respondedMessage.getReplies().size());
    assertTrue(respondedMessage.getReplies().get(0).isAdminReply());
}

@Test
public void testUserCanReplyAfterAdminResponse()
        throws DuplicateInstanceException, InstanceNotFoundException,
        PermissionException {

    UserDto user = createUser("userThreadReply");
    UserDto admin = createAdmin("adminThreadReply");

    MessageDto savedMessage = messageService.createMessage(
            createMessageDto(),
            user.getId()
    );

    messageService.respondMessage(
            savedMessage.getId(),
            admin.getId(),
            "Respuesta inicial del ANPA."
    );

    MessageDto repliedMessage = messageService.replyMessage(
            savedMessage.getId(),
            user.getId(),
            "Gracias, aporto mas informacion."
    );

    assertEquals(2, repliedMessage.getReplies().size());
    assertFalse(repliedMessage.getReplies().get(1).isAdminReply());
    assertEquals("Gracias, aporto mas informacion.", repliedMessage.getReplies().get(1).getContent());
}

@Test
public void testUserCannotReplyBeforeAdminResponse()
        throws DuplicateInstanceException, InstanceNotFoundException {

    UserDto user = createUser("userThreadNoAdminReply");

    MessageDto savedMessage = messageService.createMessage(
            createMessageDto(),
            user.getId()
    );

    assertThrows(PermissionException.class, () -> {
        messageService.replyMessage(
                savedMessage.getId(),
                user.getId(),
                "No deberia poder contestar todavia."
        );
    });
}

@Test
public void testRespondMessageAsNormalUserThrowsPermissionException()
        throws DuplicateInstanceException, InstanceNotFoundException {

    UserDto user = createUser("userCannotRespond");

    MessageDto savedMessage = messageService.createMessage(
            createMessageDto(),
            user.getId()
    );

    assertThrows(PermissionException.class, () -> {
        messageService.respondMessage(
                savedMessage.getId(),
                user.getId(),
                "Intento responder sin permisos."
        );
    });
}
    @Test
    public void testFindNonExistentMessageThrowsInstanceNotFoundException()
            throws DuplicateInstanceException {

        UserDto user = createUser("nonExistentMessageUser");

        assertThrows(InstanceNotFoundException.class, () -> {
            messageService.findById(-1L, user.getId());
        });
    }
}
