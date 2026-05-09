package es.udc.anpaproject.backend.rest.controllers;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.BlogPostService;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;
import es.udc.anpaproject.backend.rest.dtos.BlockDto;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/posts")
public class BlogPostController {

    @Autowired
    private BlogPostService blogPostService;

    @GetMapping
    public BlockDto<BlogPostDto> getPublished(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Block<BlogPostDto> block = blogPostService.findPublished(page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin")
    public BlockDto<BlogPostDto> getAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Block<BlogPostDto> block = blogPostService.findAll(page, size);

        return new BlockDto<>(
                block.getItems(),
                block.getExistMoreItems()
        );
    }

    @GetMapping("/{postId}")
    public BlogPostDto getById(@PathVariable Long postId)
            throws InstanceNotFoundException {

        return blogPostService.findById(postId);
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
    @PutMapping("/{postId}")
    public BlogPostDto update(@PathVariable Long postId,
                              @Valid @RequestBody BlogPostDto dto,
                              @RequestAttribute Long userId)
            throws InstanceNotFoundException, PermissionException {

        return blogPostService.updatePost(postId, dto, userId);
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
}