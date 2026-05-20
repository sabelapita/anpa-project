package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.IncorrectLoginException;
import es.udc.anpaproject.backend.rest.dtos.AuthenticatedUserDto;
import es.udc.anpaproject.backend.rest.dtos.LoginParamsDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;
import es.udc.anpaproject.backend.model.services.*;

@SpringBootTest
@Transactional // Esto hace que el test no ensucie la base de datos (hace rollback al terminar)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Test
    public void testSignUpAndLogin() throws DuplicateInstanceException, IncorrectLoginException {
        
        // 1. Datos del usuario de prueba
        UserDto userDto = new UserDto(null, "testuser", "Test", "User", "test@udc.es", "USER");
        userDto.setPassword("password");

        // 2. Probar registro
        UserDto registeredUser = userService.signUp(userDto);
        assertNotNull(registeredUser.getId());
        assertEquals("testuser", registeredUser.getUserName());

        // 3. Probar login exitoso
        LoginParamsDto loginParams = new LoginParamsDto();
        loginParams.setUserName("testuser");
        loginParams.setPassword("password");

        AuthenticatedUserDto authDto = userService.login(loginParams);
        
        assertNotNull(authDto.getServiceToken());
        assertEquals("testuser", authDto.getUserDto().getUserName());
    }

    @Test
    public void testDuplicateUser() throws DuplicateInstanceException {
        UserDto user1 = new UserDto(null, "dupe", "Name", "Last", "u1@udc.es", "USER");
        user1.setPassword("pass");
        userService.signUp(user1);

        // Intentar registrar el mismo userName debe lanzar la excepción
        assertThrows(DuplicateInstanceException.class, () -> {
            userService.signUp(user1);
        });
    }

    @Test
    public void testSignUpAlwaysCreatesRegularUser() throws DuplicateInstanceException {
        UserDto userDto = new UserDto(null, "notadmin", "Name", "Last", "notadmin@udc.es", "ADMIN");
        userDto.setPassword("password");

        UserDto registeredUser = userService.signUp(userDto);

        assertEquals("USER", registeredUser.getRole());
    }

    @Test
    public void testIncorrectLogin() throws DuplicateInstanceException {
        UserDto user = new UserDto(null, "wrongpass", "Name", "Last", "u2@udc.es", "USER");
        user.setPassword("correct_password");
        userService.signUp(user);

        LoginParamsDto wrongParams = new LoginParamsDto();
        wrongParams.setUserName("wrongpass");
        wrongParams.setPassword("incorrect_password");

        assertThrows(IncorrectLoginException.class, () -> {
            userService.login(wrongParams);
        });
    }
}
