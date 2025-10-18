package es.udc.anpaproject.backend.model.services;

import es.udc.anpaproject.backend.model.entities.User;
import es.udc.anpaproject.backend.model.exceptions.InstanceNotFoundException;

public interface PermissionChecker {
	
	User checkUser(Long userId) throws InstanceNotFoundException;
	
}
