package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.BlogPostAttachmentRepository;
import es.udc.anpaproject.backend.model.daos.BlogPostRepository;
import es.udc.anpaproject.backend.model.daos.MessageRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.BlogPost;
import es.udc.anpaproject.backend.model.entities.BlogPostAttachment;
import es.udc.anpaproject.backend.model.entities.Message;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.InvalidFileException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.BlogPostConverter;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;

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
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class BlogPostServiceImpl implements BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private BlogPostAttachmentRepository blogPostAttachmentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MessageRepository messageRepository;

    private final Path uploadDir = Paths.get("uploads", "posts");

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "application/pdf",
            "image/png",
            "image/jpeg"
    );

    @Override
    @Transactional(readOnly = true)
    public Block<BlogPostDto> findPublished(String keywords, String category, int page, int size) {

        Slice<BlogPost> slice = blogPostRepository
                .findPublishedFiltered(
                        normalizeFilter(keywords),
                        normalizeFilter(category),
                        PageRequest.of(page, size)
                );

        return new Block<>(
                BlogPostConverter.toPostDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Block<BlogPostDto> findAll(String keywords, String category, int page, int size) {

        Slice<BlogPost> slice = blogPostRepository
                .findAllFiltered(
                        normalizeFilter(keywords),
                        normalizeFilter(category),
                        PageRequest.of(page, size)
                );

        return new Block<>(
                BlogPostConverter.toPostDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPostDto findPublishedById(Long postId) throws InstanceNotFoundException {

        BlogPost post = blogPostRepository.findByIdAndPublishedTrue(postId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.blogPost", postId));

        return BlogPostConverter.toPostDto(post);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPostDto findById(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = blogPostRepository.findById(postId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.blogPost", postId));

        return BlogPostConverter.toPostDto(post);
    }

    @Override
    public BlogPostDto createPost(BlogPostDto dto, Long authorId)
            throws InstanceNotFoundException, PermissionException {
        try {
            return createPost(dto, authorId, null);
        } catch (IOException | InvalidFileException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public BlogPostDto createPost(BlogPostDto dto, Long authorId, MultipartFile[] files)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        User author = getUser(authorId);
        checkAdmin(author);

        BlogPost post = BlogPostConverter.toPostEntity(dto, author);
        addAttachments(post, files);
        BlogPost saved = blogPostRepository.save(post);

        if (saved.isPublished()) {
            createBlogNotification(saved);
        }

        return BlogPostConverter.toPostDto(saved);
    }

    @Override
    public BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException {
        try {
            return updatePost(postId, dto, userId, null);
        } catch (IOException | InvalidFileException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId, MultipartFile[] files)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        return updatePost(postId, dto, userId, files, null);
    }

    @Override
    public BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId,
                                  MultipartFile[] files, Long[] attachmentsToDelete)
            throws InstanceNotFoundException, PermissionException, IOException, InvalidFileException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);
        boolean wasPublished = post.isPublished();

        BlogPostConverter.merge(post, dto);
        deleteSelectedAttachments(post, attachmentsToDelete);
        addAttachments(post, files);

        BlogPost updated = blogPostRepository.save(post);
        if (!wasPublished && updated.isPublished()) {
            createBlogNotification(updated);
        }

        return BlogPostConverter.toPostDto(updated);
    }

    @Override
    public void deletePost(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);
        deleteAttachmentFiles(post);

        blogPostRepository.delete(post);
    }

    @Override
    public BlogPostDto setPublished(Long postId, boolean published, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);
        boolean wasPublished = post.isPublished();
        post.setPublished(published);

        BlogPost saved = blogPostRepository.save(post);
        if (!wasPublished && saved.isPublished()) {
            createBlogNotification(saved);
        }

        return BlogPostConverter.toPostDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public BlogPostAttachment getAttachmentForDownload(Long attachmentId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        BlogPostAttachment attachment = blogPostAttachmentRepository.findById(attachmentId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.blogPostAttachment", attachmentId));

        if (attachment.getPost().isPublished()) {
            return attachment;
        }

        if (userId == null) {
            throw new PermissionException();
        }

        User user = getUser(userId);
        checkAdmin(user);

        return attachment;
    }

    private User getUser(Long userId) throws InstanceNotFoundException {
        return userRepository.findById(userId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
    }

    private BlogPost getPost(Long postId) throws InstanceNotFoundException {
        return blogPostRepository.findById(postId)
                .orElseThrow(() -> new InstanceNotFoundException("project.entities.blogPost", postId));
    }

    private void checkAdmin(User user) throws PermissionException {
        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }
    }

    private String normalizeFilter(String value) {
        return value == null || value.trim().isEmpty() ? null : value.trim();
    }

    private void addAttachments(BlogPost post, MultipartFile[] files)
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

            post.addAttachment(new BlogPostAttachment(
                    post,
                    originalFileName,
                    filePath.toString(),
                    file.getContentType()
            ));
        }
    }

    private void deleteSelectedAttachments(BlogPost post, Long[] attachmentIds)
            throws PermissionException {

        if (attachmentIds == null || attachmentIds.length == 0) {
            return;
        }

        Set<Long> idsToDelete = Set.of(attachmentIds);
        for (BlogPostAttachment attachment : new ArrayList<>(post.getAttachments())) {
            if (!idsToDelete.contains(attachment.getId())) {
                continue;
            }

            deleteAttachmentFile(attachment);
            post.removeAttachment(attachment);
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

    private void deleteAttachmentFiles(BlogPost post) throws PermissionException {
        for (BlogPostAttachment attachment : post.getAttachments()) {
            deleteAttachmentFile(attachment);
        }
    }

    private void deleteAttachmentFile(BlogPostAttachment attachment) throws PermissionException {
        try {
            Path path = Paths.get(attachment.getFilePath());

            if (Files.exists(path)) {
                Files.delete(path);
            }
        } catch (IOException e) {
            throw new PermissionException();
        }
    }

    private void createBlogNotification(BlogPost post) {
        String subject = "Nueva entrada del blog";
        String content = "Se ha publicado una nueva entrada en el blog del ANPA: "
                + post.getTitle()
                + ". Categoria: "
                + (post.getCategory() != null ? post.getCategory() : "General")
                + ".";

        userRepository.findAll().stream()
                .filter(user -> user.getRole() != RoleType.ADMIN)
                .forEach(user -> messageRepository.save(new Message(user, subject, content)));
    }
}
