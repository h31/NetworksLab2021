package ru.filimonov.vacancy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class UserAlreadyExistsException extends ApplicationException {
    public static final String ERROR = "Profession not found";

    public UserAlreadyExistsException() {
        super(ERROR);
    }

    public UserAlreadyExistsException(String message) {
        super(message, ERROR);
    }

    public UserAlreadyExistsException(String message, Throwable cause) {
        super(message, cause, ERROR);
    }

    public UserAlreadyExistsException(Throwable cause) {
        super(cause, ERROR);
    }

    public UserAlreadyExistsException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR);
    }
}
