package es.udc.anpaproject.backend.rest.controllers;

import static es.udc.anpaproject.backend.rest.dtos.UserConverter.toAuthenticatedUserDto;

import java.net.URI;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import es.udc.anpaproject.backend.model.exceptions.*;
import es.udc.anpaproject.backend.model.services.UserService;
import es.udc.anpaproject.backend.rest.common.ErrorsDto;
import es.udc.anpaproject.backend.rest.common.JwtGenerator;
import es.udc.anpaproject.backend.rest.common.JwtInfo;
import es.udc.anpaproject.backend.rest.dtos.AuthenticatedUserDto;
import es.udc.anpaproject.backend.rest.dtos.ChangePasswordParamsDto;
import es.udc.anpaproject.backend.rest.dtos.LoginParamsDto; // ¡Añadido!
import es.udc.anpaproject.backend.rest.dtos.UserDto;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private MessageSource messageSource;
        
    @Autowired
    private UserService userService;

    // --- Manejo de Excepciones (Correcto) ---

    @ExceptionHandler(IncorrectLoginException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleIncorrectLoginException(IncorrectLoginException exception, Locale locale) {
        String errorMessage = messageSource.getMessage("project.exceptions.IncorrectLoginException", null,
                "project.exceptions.IncorrectLoginException", locale);
        return new ErrorsDto(errorMessage);
    }
    
    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleIncorrectPasswordException(IncorrectPasswordException exception, Locale locale) {
        String errorMessage = messageSource.getMessage("project.exceptions.IncorrectPasswordException", null,
                "project.exceptions.IncorrectPasswordException", locale);
        return new ErrorsDto(errorMessage);
    }

    // --- Endpoints ---

    @PostMapping("/signUp")
    public ResponseEntity<AuthenticatedUserDto> signUp(
        @Validated({UserDto.AllValidations.class}) @RequestBody UserDto userDto) throws DuplicateInstanceException, IncorrectLoginException {
        
        // 1. Registramos al usuario
        UserDto createdUser = userService.signUp(userDto);
            
        // 2. Hacemos login automático para devolver el token de inmediato
        AuthenticatedUserDto authenticatedUser = userService.login(new LoginParamsDto() {{
            setUserName(userDto.getUserName());
            setPassword(userDto.getPassword());
        }});

        URI location = ServletUriComponentsBuilder
            .fromCurrentRequest().path("/{id}")
            .buildAndExpand(createdUser.getId()).toUri();
            
        return ResponseEntity.created(location).body(authenticatedUser);
    }

    @PostMapping("/login")
    public AuthenticatedUserDto login(@Validated @RequestBody LoginParamsDto params) 
        throws IncorrectLoginException {
        return userService.login(params);
    }

    @PostMapping("/loginFromServiceToken")
    public AuthenticatedUserDto loginFromServiceToken(@RequestAttribute Long userId) 
        throws InstanceNotFoundException {
        return userService.loginFromId(userId);
    }

    @PutMapping("/{id}")
    public UserDto updateProfile(@RequestAttribute Long userId, @PathVariable Long id,
        @Validated({UserDto.UpdateValidations.class}) @RequestBody UserDto userDto) 
        throws InstanceNotFoundException, PermissionException {
                
        if (!id.equals(userId)) {
            throw new PermissionException();
        }
        
        return userService.updateProfile(id, userDto);
    }
    
    @PostMapping("/{id}/changePassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@RequestAttribute Long userId, @PathVariable Long id,
        @Validated @RequestBody ChangePasswordParamsDto params)
        throws PermissionException, InstanceNotFoundException, IncorrectPasswordException {
        
        if (!id.equals(userId)) {
            throw new PermissionException();
        }
        
        userService.changePassword(id, params);
    }
}