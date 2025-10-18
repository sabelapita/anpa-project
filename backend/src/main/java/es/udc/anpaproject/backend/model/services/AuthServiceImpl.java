package es.udc.anpaproject.backend.model.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.IncorrectLoginException;
import es.udc.anpaproject.backend.rest.common.JwtGenerator;
import es.udc.anpaproject.backend.rest.common.JwtInfo;
import es.udc.anpaproject.backend.rest.dtos.AuthenticatedUserDto;
import es.udc.anpaproject.backend.rest.dtos.LoginParamsDto;
import es.udc.anpaproject.backend.rest.dtos.UserConversor;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtGenerator jwtGenerator;

    @Override
    public AuthenticatedUserDto login(LoginParamsDto credentials) throws IncorrectLoginException {

        User user = userRepository.findByUserName(credentials.getUserName())
            .orElseThrow(() -> new IncorrectLoginException());

        if (!passwordEncoder.matches(credentials.getPassword(), user.getPassword())) {
            throw new IncorrectLoginException();
        }

    String token = jwtGenerator.generate(new JwtInfo(user.getId(), user.getRole().toString()));

        return UserConversor.toAuthenticatedUserDto(token, user);
    }

    
}
