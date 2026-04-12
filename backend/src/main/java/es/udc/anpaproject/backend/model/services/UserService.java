package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.*;
import es.udc.anpaproject.backend.rest.dtos.*;

public interface UserService {
    
    UserDto signUp(UserDto userDto) throws DuplicateInstanceException;
    
    // Unificamos el login aquí (lo que antes estaba en AuthService)
    AuthenticatedUserDto login(LoginParamsDto params) throws IncorrectLoginException;
    
    // Necesario para que el front recupere la sesión (F5)
    AuthenticatedUserDto loginFromId(Long id) throws InstanceNotFoundException;
        
    UserDto updateProfile(Long id, UserDto userDto) throws InstanceNotFoundException;
    
    void changePassword(Long id, ChangePasswordParamsDto params)
        throws InstanceNotFoundException, IncorrectPasswordException;
}