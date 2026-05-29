package es.udc.anpaproject.backend.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.entities.RoleType; // Importamos el enum externo
import es.udc.anpaproject.backend.model.exceptions.*;
import es.udc.anpaproject.backend.rest.dtos.*;
import es.udc.anpaproject.backend.rest.common.JwtGenerator;
import es.udc.anpaproject.backend.rest.common.JwtInfo;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtGenerator jwtGenerator; // Inyectamos el motor de tokens

    @Override
    public UserDto signUp(UserDto userDto) throws DuplicateInstanceException {
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            throw new DuplicateInstanceException("project.entities.user", userDto.getUserName());
        }
            
        User user = UserConverter.toUser(userDto);
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(RoleType.USER);
        user.setMembershipStatus(MembershipStatus.NON_MEMBER);

        return UserConverter.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUserDto login(LoginParamsDto params) throws IncorrectLoginException {
        User user = userRepository.findByUserName(params.getUserName())
            .orElseThrow(() -> new IncorrectLoginException(params.getUserName(), params.getPassword()));

        if (!passwordEncoder.matches(params.getPassword(), user.getPassword())) {
            throw new IncorrectLoginException(params.getUserName(), params.getPassword());
        }

        return generateAuthenticatedUserDto(user);
    }

    @Override
    @Transactional(readOnly = true)
    public AuthenticatedUserDto loginFromId(Long id) throws InstanceNotFoundException {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", id));

        return generateAuthenticatedUserDto(user);
    }

    @Override
    public UserDto updateProfile(Long userId, UserDto dto) throws InstanceNotFoundException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));

        user.setFirstName(dto.getFirstName());
        user.setLastName(dto.getLastName());
        user.setEmail(dto.getEmail());

        return UserConverter.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateMembershipStatus(Long adminId, Long userId, MembershipStatus membershipStatus)
        throws InstanceNotFoundException, PermissionException {

        User admin = getUser(adminId);
        checkAdmin(admin);

        User user = getUser(userId);
        user.setMembershipStatus(membershipStatus);

        return UserConverter.toUserDto(userRepository.save(user));
    }

    @Override
    public UserDto updateRole(Long adminId, Long userId, RoleType role)
        throws InstanceNotFoundException, PermissionException {

        User admin = getUser(adminId);
        checkAdmin(admin);

        User user = getUser(userId);
        user.setRole(role);

        return UserConverter.toUserDto(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public Block<UserDto> findUsers(Long adminId, String keywords, int page, int size)
        throws InstanceNotFoundException, PermissionException {

        User admin = getUser(adminId);
        checkAdmin(admin);

        Slice<User> slice = keywords == null || keywords.trim().isEmpty()
                ? userRepository.findByOrderByUserNameAsc(PageRequest.of(page, size))
                : userRepository.findByUserNameContainingIgnoreCaseOrderByUserNameAsc(
                        keywords.trim(),
                        PageRequest.of(page, size)
                );

        return new Block<>(
                slice.getContent().stream()
                        .map(UserConverter::toUserDto)
                        .toList(),
                slice.hasNext()
        );
    }

    @Override
    public void changePassword(Long id, ChangePasswordParamsDto params)
        throws InstanceNotFoundException, IncorrectPasswordException {
        
        User user = userRepository.findById(id)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", id));

        if (!passwordEncoder.matches(params.getOldPassword(), user.getPassword())) {
            throw new IncorrectPasswordException();
        }
        user.setPassword(passwordEncoder.encode(params.getNewPassword()));
        userRepository.save(user);
    }

    // Método privado para no repetir código de creación de Token
    private User getUser(Long userId) throws InstanceNotFoundException {
        return userRepository.findById(userId)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));
    }

    private void checkAdmin(User user) throws PermissionException {
        if (user.getRole() != RoleType.ADMIN) {
            throw new PermissionException();
        }
    }

    private AuthenticatedUserDto generateAuthenticatedUserDto(User user) {
        String token = jwtGenerator.generate(new JwtInfo(user.getId(), user.getRole().toString()));
        return UserConverter.toAuthenticatedUserDto(token, user);
    }
}
