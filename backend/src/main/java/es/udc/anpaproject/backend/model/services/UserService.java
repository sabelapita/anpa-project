package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.IncorrectPasswordException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.rest.dtos.ChangePasswordParamsDto;
import es.udc.anpaproject.backend.rest.dtos.UserDto;


public interface UserService {
	
	UserDto signUp(UserDto userDto) throws DuplicateInstanceException;
		
	UserDto updateProfile(Long id, UserDto userDto) throws InstanceNotFoundException;
	
	void changePassword(Long id, ChangePasswordParamsDto params)
		throws InstanceNotFoundException, IncorrectPasswordException;

}
