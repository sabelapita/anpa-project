package es.udc.anpaproject.backend.rest.common;

import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import es.udc.anpaproject.backend.model.exceptions.*;

@ControllerAdvice
public class CommonControllerAdvice {

    private static final String INSTANCE_NOT_FOUND_EXCEPTION_CODE = "project.exceptions.InstanceNotFoundException";
    private static final String DUPLICATE_INSTANCE_EXCEPTION_CODE = "project.exceptions.DuplicateInstanceException";
    private static final String PERMISSION_EXCEPTION_CODE = "project.exceptions.PermissionException";

    @Autowired
    private MessageSource messageSource;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {

        List<FieldErrorDto> fieldErrors = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldErrorDto(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        return new ErrorsDto(fieldErrors);
    }

    @ExceptionHandler(InstanceNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorsDto handleInstanceNotFoundException(InstanceNotFoundException exception, Locale locale) {

        String nameMessage = messageSource.getMessage(exception.getName(), null, exception.getName(), locale);
        String errorMessage = messageSource.getMessage(
                INSTANCE_NOT_FOUND_EXCEPTION_CODE,
                new Object[] { nameMessage, exception.getKey().toString() },
                INSTANCE_NOT_FOUND_EXCEPTION_CODE,
                locale
        );

        return new ErrorsDto(errorMessage);
    }

    @ExceptionHandler(DuplicateInstanceException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleDuplicateInstanceException(DuplicateInstanceException exception, Locale locale) {

        String nameMessage = messageSource.getMessage(exception.getName(), null, exception.getName(), locale);
        String errorMessage = messageSource.getMessage(
                DUPLICATE_INSTANCE_EXCEPTION_CODE,
                new Object[] { nameMessage, exception.getKey().toString() },
                DUPLICATE_INSTANCE_EXCEPTION_CODE,
                locale
        );

        return new ErrorsDto(errorMessage);
    }

    @ExceptionHandler(PermissionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    @ResponseBody
    public ErrorsDto handlePermissionException(PermissionException exception, Locale locale) {

        String errorMessage = messageSource.getMessage(
                PERMISSION_EXCEPTION_CODE,
                null,
                PERMISSION_EXCEPTION_CODE,
                locale
        );

        return new ErrorsDto(errorMessage);
    }

    @ExceptionHandler(IncorrectLoginException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleIncorrectLoginException(IncorrectLoginException exception, Locale locale) {

        return new ErrorsDto(getMessage("project.exceptions.IncorrectLoginException", locale));
    }

    @ExceptionHandler(IncorrectPasswordException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleIncorrectPasswordException(IncorrectPasswordException exception, Locale locale) {

        return new ErrorsDto(getMessage("project.exceptions.IncorrectPasswordException", locale));
    }

    @ExceptionHandler(AlreadyEnrolledException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorsDto handleAlreadyEnrolledException(AlreadyEnrolledException exception, Locale locale) {

        return new ErrorsDto(getMessage("project.exceptions.AlreadyEnrolledException", locale));
    }

    @ExceptionHandler(DuplicateEnrollmentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorsDto handleDuplicateEnrollmentException(DuplicateEnrollmentException exception, Locale locale) {

        return new ErrorsDto(getMessage("project.exceptions.DuplicateEnrollmentException", locale));
    }

    @ExceptionHandler(EnrollmentNotAllowedException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleEnrollmentNotAllowedException(EnrollmentNotAllowedException exception, Locale locale) {

        return new ErrorsDto(getMessage("project.exceptions.EnrollmentNotAllowedException", locale));
    }

    @ExceptionHandler(PaymentException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorsDto handlePaymentException(PaymentException exception, Locale locale) {

        return new ErrorsDto(getMessage(exception.getMessageKey(), locale));
    }

    @ExceptionHandler(InvalidFileException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorsDto handleInvalidFileException(InvalidFileException exception, Locale locale) {

        return new ErrorsDto(getMessage(exception.getMessageKey(), locale));
    }

    @ExceptionHandler(ActivityDeletionException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    @ResponseBody
    public ErrorsDto handleActivityDeletionException(ActivityDeletionException exception, Locale locale) {

        return new ErrorsDto(getMessage(exception.getMessageKey(), locale));
    }

    private String getMessage(String messageKey, Locale locale) {
        return messageSource.getMessage(
                messageKey,
                null,
                messageKey,
                locale
        );
    }
}
