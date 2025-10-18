package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.exceptions.IncorrectLoginException;
import es.udc.anpaproject.backend.rest.dtos.AuthenticatedUserDto;
import es.udc.anpaproject.backend.rest.dtos.LoginParamsDto;

public interface AuthService {
        AuthenticatedUserDto login(LoginParamsDto credentials) throws IncorrectLoginException;

}
