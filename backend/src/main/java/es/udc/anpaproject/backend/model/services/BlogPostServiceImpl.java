package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.daos.BlogPostRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.BlogPost;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.BlogPostConverter;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BlogPostServiceImpl implements BlogPostService {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Block<BlogPostDto> findPublished(int page, int size) {

        Slice<BlogPost> slice = blogPostRepository
                .findByPublishedTrueOrderByCreatedAtDesc(PageRequest.of(page, size));

        return new Block<>(
                BlogPostConverter.toPostDtos(slice.getContent()),
                slice.hasNext()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Block<BlogPostDto> findAll(int page, int size) {

        Slice<BlogPost> slice = blogPostRepository
                .findByOrderByCreatedAtDesc(PageRequest.of(page, size));

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

        User author = getUser(authorId);
        checkAdmin(author);

        BlogPost post = BlogPostConverter.toPostEntity(dto, author);
        BlogPost saved = blogPostRepository.save(post);

        return BlogPostConverter.toPostDto(saved);
    }

    @Override
    public BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);

        BlogPostConverter.merge(post, dto);

        BlogPost updated = blogPostRepository.save(post);
        return BlogPostConverter.toPostDto(updated);
    }

    @Override
    public void deletePost(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);

        blogPostRepository.delete(post);
    }

    @Override
    public BlogPostDto setPublished(Long postId, boolean published, Long userId)
            throws InstanceNotFoundException, PermissionException {

        User user = getUser(userId);
        checkAdmin(user);

        BlogPost post = getPost(postId);
        post.setPublished(published);

        BlogPost saved = blogPostRepository.save(post);
        return BlogPostConverter.toPostDto(saved);
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
}
