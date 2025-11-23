package es.udc.anpaproject.backend.model.services;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.entities.User.RoleType;
import es.udc.anpaproject.backend.model.exceptions.DuplicateInstanceException;
import es.udc.anpaproject.backend.model.exceptions.IncorrectPasswordException;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;
import es.udc.anpaproject.backend.rest.dtos.ChangePasswordParamsDto;
import es.udc.anpaproject.backend.rest.dtos.UserConversor;
import es.udc.anpaproject.backend.rest.dtos.UserDto;

@Service
@Transactional
public class UserServiceImpl implements UserService {
	
	@Autowired
    private PasswordEncoder passwordEncoder;

	@Autowired
	private UserRepository userRepository;
	
	@Override
	public UserDto signUp(UserDto userDto) throws DuplicateInstanceException {
		
		if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
			throw new DuplicateInstanceException("project.entities.user", userDto.getUserName());
		}
			
		User user = UserConversor.toUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        // Solo permitir ADMIN si viene explícitamente en el DTO
		if (userDto.getRole() != null && userDto.getRole().equals("ADMIN")) {
			user.setRole(RoleType.ADMIN);
		} else {
			user.setRole(RoleType.USER);
		}
        return UserConversor.toUserDto(userRepository.save(user));
		
	}

	@Override
    public UserDto updateProfile(Long userId, UserDto dto) throws InstanceNotFoundException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));


        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        return UserConversor.toUserDto(userRepository.save(user));
    }
	

	@Override
	public void changePassword(Long id, ChangePasswordParamsDto params)
		throws InstanceNotFoundException, IncorrectPasswordException {
		
		User user = userRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

		if (!passwordEncoder.matches(params.getOldPassword(), user.getPassword())) {
			throw new IncorrectPasswordException();
		}
		user.setPassword(passwordEncoder.encode(params.getNewPassword()));
        userRepository.save(user);
		
	}

}
