package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.RoleType;
import es.udc.anpaproject.backend.model.exceptions.*;
import es.udc.anpaproject.backend.rest.dtos.*;

public interface UserService {
    
    UserDto signUp(UserDto userDto) throws DuplicateInstanceException;
    
    // Unificamos el login aquí (lo que antes estaba en AuthService)
    AuthenticatedUserDto login(LoginParamsDto params) throws IncorrectLoginException;
    
    // Necesario para que el front recupere la sesión (F5)
    AuthenticatedUserDto loginFromId(Long id) throws InstanceNotFoundException;
        
    UserDto updateProfile(Long id, UserDto userDto) throws InstanceNotFoundException, DuplicateInstanceException;

    UserDto updateMembershipStatus(Long adminId, Long userId, MembershipStatus membershipStatus)
        throws InstanceNotFoundException, PermissionException;

    UserDto updateRole(Long adminId, Long userId, RoleType role)
        throws InstanceNotFoundException, PermissionException;

    Block<UserDto> findUsers(Long adminId, String keywords, int page, int size)
        throws InstanceNotFoundException, PermissionException;
    
    void changePassword(Long id, ChangePasswordParamsDto params)
        throws InstanceNotFoundException, IncorrectPasswordException;

    void requestPasswordReset(ForgotPasswordParamsDto params);

    void resetPassword(ResetPasswordParamsDto params) throws InvalidPasswordResetTokenException;
}
