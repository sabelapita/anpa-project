package es.udc.anpaproject.backend.test.model.services;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.IncorrectLoginException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.exceptions.PermissionException;
import es.udc.anpaproject.backend.rest.dtos.AuthenticatedUserDto;
import es.udc.anpaproject.backend.rest.dtos.LoginParamsDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;
import es.udc.anpaproject.backend.model.services.*;

@SpringBootTest
@Transactional // Esto hace que el test no ensucie la base de datos (hace rollback al terminar)
public class UserServiceTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testSignUpAndLogin() throws DuplicateInstanceException, IncorrectLoginException {
        
        // 1. Datos del usuario de prueba
        UserDto userDto = new UserDto(null, "testuser", "Test", "User", "test@udc.es", "USER");
        userDto.setPassword("password");

        // 2. Probar registro
        UserDto registeredUser = userService.signUp(userDto);
        assertNotNull(registeredUser.getId());
        assertEquals("testuser", registeredUser.getUserName());
        assertEquals("NON_MEMBER", registeredUser.getMembershipStatus());

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
    public void testAdminCanUpdateMembershipStatus()
            throws DuplicateInstanceException, InstanceNotFoundException, PermissionException {

        UserDto adminDto = new UserDto(null, "membershipadmin", "Admin", "User", "admin@udc.es", "USER");
        adminDto.setPassword("password");
        UserDto registeredAdmin = userService.signUp(adminDto);

        User admin = userRepository.findById(registeredAdmin.getId()).get();
        admin.setRole(RoleType.ADMIN);

        UserDto userDto = new UserDto(null, "membershipuser", "Normal", "User", "normal@udc.es", "USER");
        userDto.setPassword("password");
        UserDto registeredUser = userService.signUp(userDto);

        UserDto updatedUser = userService.updateMembershipStatus(
            admin.getId(),
            registeredUser.getId(),
            MembershipStatus.MEMBER
        );

        assertEquals("MEMBER", updatedUser.getMembershipStatus());
    }

    @Test
    public void testRegularUserCannotUpdateMembershipStatus() throws DuplicateInstanceException {
        UserDto managerDto = new UserDto(null, "regularmanager", "Regular", "Manager", "manager@udc.es", "USER");
        managerDto.setPassword("password");
        UserDto manager = userService.signUp(managerDto);

        UserDto userDto = new UserDto(null, "regularmember", "Normal", "User", "member@udc.es", "USER");
        userDto.setPassword("password");
        UserDto user = userService.signUp(userDto);

        assertThrows(PermissionException.class, () ->
            userService.updateMembershipStatus(manager.getId(), user.getId(), MembershipStatus.MEMBER));
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
