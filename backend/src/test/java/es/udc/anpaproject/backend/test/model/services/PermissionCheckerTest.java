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
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.model.services.PermissionChecker;

@SpringBootTest
@Transactional
public class PermissionCheckerTest {

    @Autowired
    private PermissionChecker permissionChecker;

    @Autowired
    private UserRepository userRepository;

    @Test
    public void testCheckUserExistingUser() throws InstanceNotFoundException {

        User user = new User();
        user.setUserName("sabela");
        user.setPassword("password");
        user.setFirstName("Sabela");
        user.setLastName("Pita");
        user.setEmail("sabela@test.com");
        user.setRole(RoleType.USER);
        user.setMembershipStatus(MembershipStatus.NON_MEMBER);

        User savedUser = userRepository.save(user);

        User result = permissionChecker.checkUser(savedUser.getId());

        assertEquals(savedUser.getId(), result.getId());
        assertEquals("sabela", result.getUserName());
        assertEquals(RoleType.USER, result.getRole());
    }

    @Test
    public void testCheckUserNonExistingUser() {

        Long nonExistingUserId = -1L;

        assertThrows(
                InstanceNotFoundException.class,
                () -> permissionChecker.checkUser(nonExistingUserId)
        );
    }
}
