package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.MessageAttachmentRepository;
import es.udc.anpaproject.backend.model.daos.MessageRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Message;
import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import es.udc.anpaproject.backend.model.entities.MessageReply;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.MessageConverter;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;

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
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    private static final String BLOG_NOTICE_SUBJECT = "Nueva entrada del blog";

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageAttachmentRepository messageAttachmentRepository;

    @Autowired
    private UserRepository userRepository;

    private final Path uploadDir = Paths.get("uploads", "messages");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Override
    @Transactional(readOnly = true)
    public Block<MessageDto> findUserMessages(Long userId, int page, int size)
            throws InstanceNotFoundException {

        getUser(userId);

        Slice<Message> slice = messageRepository
                .findByUserIdOrderBySentAtDesc(userId, PageRequest.of(page, size));

        return new Block<>(
                MessageConverter.toMessageDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Block<MessageDto> findAllMessages(Long userId, int page, int size)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        Slice<Message> slice = messageRepository
                .findBySubjectNotOrderBySentAtDesc(BLOG_NOTICE_SUBJECT, PageRequest.of(page, size));

        return new Block<>(
                MessageConverter.toMessageDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public MessageDto findById(Long messageId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        Message message = getMessage(messageId);

        if (user.getRole() != RoleType.ADMIN &&
                !message.getUser().getId().equals(userId)) {
            throw new PermissionException();
        }

        return MessageConverter.toMessageDto(message);
    }

    @Override
    public MessageDto createMessage(MessageDto dto, Long userId)
            throws InstanceNotFoundException {
        try {
            return createMessage(dto, userId, null);
        } catch (IOException | InvalidFileException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public MessageDto createMessage(MessageDto dto, Long userId, MultipartFile[] files)
            throws InstanceNotFoundException, IOException, InvalidFileException {

        User user = getUser(userId);

        Message message = MessageConverter.toMessageEntity(dto, user);
        addAttachments(message, files);

        Message saved = messageRepository.save(message);

        return MessageConverter.toMessageDto(saved);
    }

    @Override
    public MessageDto respondMessage(Long messageId, Long userId, String response)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        Message message = getMessage(messageId);

        message.setResponse(response);
        message.setRespondedAt(LocalDateTime.now());
        message.addReply(new MessageReply(message, true, response));

        Message saved = messageRepository.save(message);

        return MessageConverter.toMessageDto(saved);
    }

    @Override
    public MessageDto replyMessage(Long messageId, Long userId, String content)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        Message message = getMessage(messageId);

        if (!message.getUser().getId().equals(userId)) {
            throw new PermissionException();
        }

        if (message.getResponse() == null || message.getResponse().isBlank()) {
            throw new PermissionException();
        }

        message.addReply(new MessageReply(message, false, content));

        Message saved = messageRepository.save(message);

        return MessageConverter.toMessageDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public MessageAttachment getAttachmentForDownload(Long attachmentId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        MessageAttachment attachment = messageAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.messageAttachment", attachmentId));

        if (user.getRole() != RoleType.ADMIN &&
                !attachment.getMessage().getUser().getId().equals(userId)) {
            throw new PermissionException();
        }

        return attachment;
    }

    private void addAttachments(Message message, MultipartFile[] files)
            throws IOException, InvalidFileException {

        if (files == null || files.length == 0) {
            return;
        }

        for (MultipartFile file : files) {
            validateFile(file);
        }

        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        for (MultipartFile file : files) {
            String originalFileName = file.getOriginalFilename();
            String storedFileName = UUID.randomUUID() + "_" + originalFileName;
            Path filePath = uploadDir.resolve(storedFileName);

            Files.copy(file.getInputStream(), filePath);

            message.addAttachment(new MessageAttachment(
                    message,
                    originalFileName,
                    filePath.toString(),
                    file.getContentType()
            ));
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

    private User getUser(Long userId) throws InstanceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
    }

    private Message getMessage(Long messageId) throws InstanceNotFoundException {
        return messageRepository.findById(messageId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.message", messageId));
    }

    private void checkAdmin(User user) throws PermissionException {
        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }
    }
}
