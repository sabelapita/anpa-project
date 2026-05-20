package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.model.services.BlogPostService;
import es.udc.anpaproject.backend.model.services.Block;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.dtos.BlogPostDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

@SpringBootTest
@Transactional
public class BlogPostServiceTest {

    @Autowired
    private BlogPostService blogPostService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User createAdminUser(String userName) throws DuplicateInstanceException {
        UserDto userDto = new UserDto(null, userName, "Admin", "Test", userName + "@udc.es", "USER");
        userDto.setPassword("password");

        UserDto registeredUser = userService.signUp(userDto);

        User user = userRepository.findById(registeredUser.getId()).get();
        user.setRole(RoleType.ADMIN);

        return user;
    }

    private User createNormalUser(String userName) throws DuplicateInstanceException {
        UserDto userDto = new UserDto(null, userName, "User", "Test", userName + "@udc.es", "USER");
        userDto.setPassword("password");

        UserDto registeredUser = userService.signUp(userDto);

        return userRepository.findById(registeredUser.getId()).get();
    }

    @Test
    public void testCreatePostAsAdmin() 
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminblog");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Noticia ANPA",
                "Contenido de prueba para el blog",
                null,
                null,
                null,
                true,
                "NOTICIAS"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());

        assertNotNull(createdPost.getId());
        assertEquals("Noticia ANPA", createdPost.getTitle());
        assertEquals("Contenido de prueba para el blog", createdPost.getContent());
        assertEquals("NOTICIAS", createdPost.getCategory());
        assertEquals(admin.getId(), createdPost.getAuthorId());
        assertTrue(createdPost.isPublished());
    }

    @Test
    public void testCreatePostAsUserThrowsPermissionException() throws DuplicateInstanceException {

        User user = createNormalUser("userblog");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Post no permitido",
                "Este post no debería crearse",
                null,
                null,
                null,
                true,
                "NOTICIAS"
        );

        assertThrows(PermissionException.class, () -> {
            blogPostService.createPost(postDto, user.getId());
        });
    }

    @Test
    public void testFindPublishedPosts() 
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminpublished");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Noticia publicada",
                "Contenido visible para usuarios",
                null,
                null,
                null,
                true,
                "ACTUALIDAD"
        );

        blogPostService.createPost(postDto, admin.getId());

        Block<BlogPostDto> block = blogPostService.findPublished(0, 10);

        assertFalse(block.getItems().isEmpty());
        assertTrue(
                block.getItems().stream()
                        .anyMatch(post -> post.getTitle().equals("Noticia publicada"))
        );
    }

    @Test
    public void testFindPublishedByIdDoesNotReturnUnpublishedPost()
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminhidden");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Borrador privado",
                "Contenido no visible publicamente",
                null,
                null,
                null,
                false,
                "BORRADORES"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());

        assertThrows(InstanceNotFoundException.class, () -> {
            blogPostService.findPublishedById(createdPost.getId());
        });
    }

    @Test
    public void testFindByIdAsAdminReturnsUnpublishedPost()
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminfindhidden");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Borrador visible para admin",
                "Contenido interno",
                null,
                null,
                null,
                false,
                "BORRADORES"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());
        BlogPostDto foundPost = blogPostService.findById(createdPost.getId(), admin.getId());

        assertEquals(createdPost.getId(), foundPost.getId());
        assertFalse(foundPost.isPublished());
    }

    @Test
    public void testUpdatePostAsAdmin() 
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminupdate");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Título inicial",
                "Contenido inicial",
                null,
                null,
                null,
                true,
                "NOTICIAS"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());

        BlogPostDto updateDto = new BlogPostDto(
                null,
                "Título actualizado",
                "Contenido actualizado",
                null,
                null,
                null,
                true,
                "ACTUALIDAD"
        );

        BlogPostDto updatedPost = blogPostService.updatePost(
                createdPost.getId(),
                updateDto,
                admin.getId()
        );

        assertEquals("Título actualizado", updatedPost.getTitle());
        assertEquals("Contenido actualizado", updatedPost.getContent());
        assertEquals("ACTUALIDAD", updatedPost.getCategory());
    }

    @Test
    public void testSetPublished() 
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("adminpublish");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Post publicable",
                "Contenido del post",
                null,
                null,
                null,
                true,
                "NOTICIAS"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());

        BlogPostDto unpublishedPost = blogPostService.setPublished(
                createdPost.getId(),
                false,
                admin.getId()
        );

        assertFalse(unpublishedPost.isPublished());
    }

    @Test
    public void testDeletePostAsAdmin() 
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        User admin = createAdminUser("admindelete");

        BlogPostDto postDto = new BlogPostDto(
                null,
                "Post para borrar",
                "Contenido temporal",
                null,
                null,
                null,
                true,
                "NOTICIAS"
        );

        BlogPostDto createdPost = blogPostService.createPost(postDto, admin.getId());

        blogPostService.deletePost(createdPost.getId(), admin.getId());

        assertThrows(InstanceNotFoundException.class, () -> {
            blogPostService.findById(createdPost.getId(), admin.getId());
        });
    }
}
