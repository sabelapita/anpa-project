package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.MessageService;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;
import es.udc.anpaproject.backend.rest.dtos.MessageResponseDto;
import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/messages")
public class MessageController {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public BlockDto<MessageDto> getUserMessages(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws InstanceNotFoundException {

        Block<MessageDto> block = messageService.findUserMessages(userId, page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/all")
    public BlockDto<MessageDto> getAllMessages(
            @RequestAttribute Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size)
            throws InstanceNotFoundException, PermissionException {

        Block<MessageDto> block = messageService.findAllMessages(userId, page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @GetMapping("/{messageId}")
    public MessageDto getById(@PathVariable Long messageId,
                              @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return messageService.findById(messageId, userId);
    }

    @PostMapping
    public MessageDto create(@Valid @RequestBody MessageDto dto,
                             @RequestAttribute Long userId)
            throws InstanceNotFoundException {

        return messageService.createMessage(dto, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{messageId}/response")
    public MessageDto respond(@PathVariable Long messageId,
                            @Valid @RequestBody MessageResponseDto dto,
                            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return messageService.respondMessage(
                messageId,
                userId,
                dto.getResponse()
        );
    }
}