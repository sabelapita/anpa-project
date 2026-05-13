package es.udc.anpaproject.backend.model.exceptions;

@SuppressWarnings("serial")
public class PaymentException extends Exception {

    private final String messageKey;

    public PaymentException(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}