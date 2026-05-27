package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;
import es.udc.anpaproject.backend.model.entities.BlogPostAttachment;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface BlogPostService {

    Block<BlogPostDto> findPublished(String keywords, String category, int page, int size);

    Block<BlogPostDto> findAll(String keywords, String category, int page, int size);
    
    BlogPostDto findPublishedById(Long postId) throws InstanceNotFoundException;

    BlogPostDto findById(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto createPost(BlogPostDto dto, Long authorId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto createPost(BlogPostDto dto, Long authorId, MultipartFile[] files)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException;

    BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId, MultipartFile[] files)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException;

    BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId,
                           MultipartFile[] files, Long[] attachmentsToDelete)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException;

    void deletePost(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto setPublished(Long postId, boolean published, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostAttachment getAttachmentForDownload(Long attachmentId, Long userId)
            throws InstanceNotFoundException, PermissionException;
}
