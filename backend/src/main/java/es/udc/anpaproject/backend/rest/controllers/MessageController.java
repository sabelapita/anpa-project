package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.entities.MessageAttachment;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.MessageService;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;
import es.udc.anpaproject.backend.rest.dtos.MessageDto;
import es.udc.anpaproject.backend.rest.dtos.MessageResponseDto;
import jakarta.validation.Valid;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

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

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public MessageDto createWithAttachments(
            @Valid @RequestPart("data") MessageDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestAttribute Long userId)
            throws InstanceNotFoundException, IOException, InvalidFileException {

        return messageService.createMessage(dto, userId, files);
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

    @PostMapping("/{messageId}/replies")
    public MessageDto reply(@PathVariable Long messageId,
                            @Valid @RequestBody MessageResponseDto dto,
                            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return messageService.replyMessage(
                messageId,
                userId,
                dto.getResponse()
        );
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable Long attachmentId,
            HttpServletRequest request)
            throws InstanceNotFoundException, PermissionException, IOException {

        MessageAttachment attachment = messageService.getAttachmentForDownload(
                attachmentId,
                (Long) request.getAttribute("userId")
        );

        Path path = Paths.get(attachment.getFilePath());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(resource);
    }
}
