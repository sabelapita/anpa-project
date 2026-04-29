package es.udc.anpaproject.backend.model.exceptions;

@SuppressWarnings("serial")
public class PermissionException extends Exception {
    public PermissionException() {
        super("Permission denied");
    }
}
