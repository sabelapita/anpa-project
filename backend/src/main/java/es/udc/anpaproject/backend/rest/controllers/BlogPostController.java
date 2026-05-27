package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.entities.BlogPostAttachment;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.BlogPostService;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;

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
@RequestMapping("/posts")
public class BlogPostController {

    @Autowired
    private BlogPostService blogPostService;

    @GetMapping
    public BlockDto<BlogPostDto> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String category) {

        Block<BlogPostDto> block = blogPostService.findPublished(keywords, category, page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public BlockDto<BlogPostDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String keywords,
            @RequestParam(required = false) String category) {

        Block<BlogPostDto> block = blogPostService.findAll(keywords, category, page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @GetMapping("/{postId}")
    public BlogPostDto getById(@PathVariable Long postId)
            throws InstanceNotFoundException {

        return blogPostService.findPublishedById(postId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/{postId}")
    public BlogPostDto getByIdAsAdmin(@PathVariable Long postId,
                                      @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return blogPostService.findById(postId, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BlogPostDto create(@Valid @RequestBody BlogPostDto dto,
                              @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return blogPostService.createPost(dto, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public BlogPostDto createWithAttachments(
            @Valid @RequestPart("data") BlogPostDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        return blogPostService.createPost(dto, userId, files);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{postId}")
    public BlogPostDto update(@PathVariable Long postId,
                              @Valid @RequestBody BlogPostDto dto,
                              @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return blogPostService.updatePost(postId, dto, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping(value = "/{postId}", consumes = "multipart/form-data")
    public BlogPostDto updateWithAttachments(
            @PathVariable Long postId,
            @Valid @RequestPart("data") BlogPostDto dto,
            @RequestPart(value = "files", required = false) MultipartFile[] files,
            @RequestParam(value = "attachmentsToDelete", required = false) Long[] attachmentsToDelete,
            @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        return blogPostService.updatePost(postId, dto, userId, files, attachmentsToDelete);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{postId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long postId,
                       @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        blogPostService.deletePost(postId, userId);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{postId}/published")
    public BlogPostDto setPublished(@PathVariable Long postId,
                                    @RequestParam boolean value,
                                    @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return blogPostService.setPublished(postId, value, userId);
    }

    @GetMapping("/attachments/{attachmentId}/download")
    public ResponseEntity<InputStreamResource> downloadAttachment(
            @PathVariable Long attachmentId,
            HttpServletRequest request)
            throws InstanceNotFoundException, PermissionException, IOException {

        BlogPostAttachment attachment = blogPostService.getAttachmentForDownload(
                attachmentId,
                getOptionalUserId(request)
        );

        Path path = Paths.get(attachment.getFilePath());
        InputStreamResource resource = new InputStreamResource(new FileInputStream(path.toFile()));

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + attachment.getFileName() + "\"")
                .contentType(MediaType.parseMediaType(attachment.getContentType()))
                .body(resource);
    }

    private Long getOptionalUserId(HttpServletRequest request) {
        Object userId = request.getAttribute("userId");

        return userId instanceof Long ? (Long) userId : null;
    }
}
