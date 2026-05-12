package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;

public interface MessageService {

    Block<MessageDto> findUserMessages(Long userId, int page, int size)
            throws InstanceNotFoundException;

    Block<MessageDto> findAllMessages(Long userId, int page, int size)
            throws InstanceNotFoundException, PermissionException;

    MessageDto findById(Long messageId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    MessageDto createMessage(MessageDto dto, Long userId)
            throws InstanceNotFoundException;

    MessageDto respondMessage(Long messageId, Long userId, String response)
        throws InstanceNotFoundException, PermissionException;
}