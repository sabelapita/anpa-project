package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.MessageRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.Message;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.MessageConverter;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional
public class MessageServiceImpl implements MessageService {

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;

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
                .findByOrderBySentAtDesc(PageRequest.of(page, size));

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

        User user = getUser(userId);

        Message message = MessageConverter.toMessageEntity(dto, user);

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

        Message saved = messageRepository.save(message);

        return MessageConverter.toMessageDto(saved);
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