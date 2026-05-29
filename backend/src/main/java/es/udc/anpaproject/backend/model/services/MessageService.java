package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MessageService {

    Block<MessageDto> findUserMessages(Long userId, int page, int size)
            throws InstanceNotFoundException;

    Block<MessageDto> findAllMessages(Long userId, int page, int size)
            throws InstanceNotFoundException, PermissionException;

    MessageDto findById(Long messageId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    MessageDto createMessage(MessageDto dto, Long userId)
            throws InstanceNotFoundException;

    MessageDto createMessage(MessageDto dto, Long userId, MultipartFile[] files)
            throws InstanceNotFoundException, IOException, InvalidFileException;

    MessageDto respondMessage(Long messageId, Long userId, String response)
        throws InstanceNotFoundException, PermissionException;

    MessageDto replyMessage(Long messageId, Long userId, String content)
        throws InstanceNotFoundException, PermissionException;

    MessageAttachment getAttachmentForDownload(Long attachmentId, Long userId)
            throws InstanceNotFoundException, PermissionException;
}
