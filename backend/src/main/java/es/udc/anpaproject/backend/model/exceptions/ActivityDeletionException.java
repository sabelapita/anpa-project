package es.udc.anpaproject.backend.model.exceptions;

public class ActivityDeletionException extends Exception {

    private final String messageKey;

    public ActivityDeletionException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
