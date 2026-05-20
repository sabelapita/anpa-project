import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;

public interface BlogPostService {

    Block<BlogPostDto> findPublished(int page, int size);

    Block<BlogPostDto> findAll(int page, int size);
    
    BlogPostDto findPublishedById(Long postId) throws InstanceNotFoundException;

    BlogPostDto findById(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto createPost(BlogPostDto dto, Long authorId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto updatePost(Long postId, BlogPostDto dto, Long userId)
            throws InstanceNotFoundException, PermissionException;

    void deletePost(Long postId, Long userId)
            throws InstanceNotFoundException, PermissionException;

    BlogPostDto setPublished(Long postId, boolean published, Long userId)
            throws InstanceNotFoundException, PermissionException;
}
