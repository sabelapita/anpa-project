package es.udc.anpaproject.backend.rest.dtos;

import es.udc.anpaproject.backend.model.entities.User;

public class UserConverter {
	
	private UserConverter() {}
	
	public final static UserDto toUserDto(User user) {
		return new UserDto(user.getId(), user.getUserName(), user.getFirstName(), user.getLastName(), user.getEmail(),
			user.getRole().toString());
	}
	
	public final static User toUser(UserDto userDto) {
		
		return new User(userDto.getUserName(), userDto.getPassword(), userDto.getFirstName(), userDto.getLastName(),
			userDto.getEmail());
	}
	
	public final static AuthenticatedUserDto toAuthenticatedUserDto(String token, User user) {
		
		return new AuthenticatedUserDto(token, toUserDto(user));
		
	}
	public static AuthenticatedUserDto toAuthenticatedUserDto(String token, UserDto userDto) {
		
	    return new AuthenticatedUserDto(token, userDto);
	
	}


}
