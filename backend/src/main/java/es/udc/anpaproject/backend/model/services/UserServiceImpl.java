package es.udc.anpaproject.backend.model.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import es.udc.anpaproject.backend.model.daos.PasswordResetTokenRepository;
import es.udc.anpaproject.backend.model.daos.UserRepository;
import es.udc.anpaproject.backend.model.entities.MembershipStatus;
import es.udc.anpaproject.backend.model.entities.PasswordResetToken;
import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.entities.RoleType; // Importamos el enum externo
import es.udc.anpaproject.backend.model.exceptions.*;
import es.udc.anpaproject.backend.rest.dtos.*;
import es.udc.anpaproject.backend.rest.common.JwtGenerator;
import es.udc.anpaproject.backend.rest.common.JwtInfo;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);
    private static final int PASSWORD_RESET_TOKEN_BYTES = 32;
    
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private JwtGenerator jwtGenerator; // Inyectamos el motor de tokens

    @Autowired
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    @Value("${project.frontend.base-url:http://localhost:3000}")
    private String frontendBaseUrl;

    @Value("${project.passwordReset.expirationMinutes:30}")
    private long passwordResetExpirationMinutes;

    @Value("${project.passwordReset.from:no-reply@anpa-project.local}")
    private String passwordResetFrom;

    @Override
    public UserDto signUp(UserDto userDto) throws DuplicateInstanceException {
        if (userRepository.findByUserName(userDto.getUserName()).isPresent()) {
            throw new DuplicateInstanceException("project.entities.user", userDto.getUserName());
        }

        if (userRepository.findByEmailIgnoreCase(userDto.getEmail()).isPresent()) {
            throw new DuplicateInstanceException("project.entities.user", userDto.getEmail());
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
    public UserDto updateProfile(Long userId, UserDto dto) throws InstanceNotFoundException, DuplicateInstanceException {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new InstanceNotFoundException("project.entities.user", userId));

        if (!user.getEmail().equalsIgnoreCase(dto.getEmail()) &&
            userRepository.findByEmailIgnoreCase(dto.getEmail()).isPresent()) {
            throw new DuplicateInstanceException("project.entities.user", dto.getEmail());
        }

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

    @Override
    public void requestPasswordReset(ForgotPasswordParamsDto params) {
        userRepository.findByEmailIgnoreCase(params.getEmail()).ifPresent(user -> {
            LocalDateTime now = LocalDateTime.now();
            passwordResetTokenRepository.findByUserAndUsedAtIsNull(user)
                .forEach(token -> token.setUsedAt(now));

            String rawToken = generateSecureToken();
            PasswordResetToken token = new PasswordResetToken(
                user,
                hashToken(rawToken),
                now.plusMinutes(passwordResetExpirationMinutes)
            );
            passwordResetTokenRepository.save(token);

            sendPasswordResetEmail(user, rawToken);
        });
    }

    @Override
    public void resetPassword(ResetPasswordParamsDto params) throws InvalidPasswordResetTokenException {
        PasswordResetToken token = passwordResetTokenRepository.findByTokenHash(hashToken(params.getToken()))
            .orElseThrow(InvalidPasswordResetTokenException::new);

        LocalDateTime now = LocalDateTime.now();
        if (token.getUsedAt() != null || token.getExpiresAt().isBefore(now)) {
            throw new InvalidPasswordResetTokenException();
        }

        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(params.getNewPassword()));
        token.setUsedAt(now);
        userRepository.save(user);
        passwordResetTokenRepository.save(token);
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

    private String generateSecureToken() {
        byte[] bytes = new byte[PASSWORD_RESET_TOKEN_BYTES];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            StringBuilder builder = new StringBuilder(hash.length * 2);
            for (byte value : hash) {
                builder.append(String.format("%02x", value));
            }
            return builder.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm is not available", e);
        }
    }

    private void sendPasswordResetEmail(User user, String rawToken) {
        String resetUrl = UriComponentsBuilder.fromHttpUrl(frontendBaseUrl)
            .path("/reset-password")
            .queryParam("token", rawToken)
            .build()
            .toUriString();

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();
        if (mailSender == null) {
            logger.warn("Password reset email for {} was not sent because no mail sender is configured",
                user.getEmail());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(passwordResetFrom);
            message.setTo(user.getEmail());
            message.setSubject("Recuperacion de contrasena - ANPA Project");
            message.setText("""
                Hola %s,

                Hemos recibido una solicitud para recuperar tu contrasena.
                Puedes crear una nueva contrasena en este enlace:

                %s

                El enlace caduca en %d minutos. Si no has solicitado este cambio, ignora este mensaje.
                """.formatted(user.getFirstName(), resetUrl, passwordResetExpirationMinutes));
            mailSender.send(message);
        } catch (MailException exception) {
            logger.warn("Could not send password reset email to {}", user.getEmail(), exception);
        }
    }
}
