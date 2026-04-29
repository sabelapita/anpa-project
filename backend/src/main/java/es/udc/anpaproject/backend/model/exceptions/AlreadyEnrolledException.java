package es.udc.anpaproject.backend.model.exceptions;

@SuppressWarnings("serial")
public class AlreadyEnrolledException extends Exception {
    public AlreadyEnrolledException() {
        super("User is already enrolled in this activity");
    }
}