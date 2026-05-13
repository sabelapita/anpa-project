package es.udc.anpaproject.backend.model.exceptions;

@SuppressWarnings("serial")
public class InvalidFileException extends Exception {

    private final String messageKey;

    public InvalidFileException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}