package ru.filimonov.vacancy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProfessionNotFoundException extends ApplicationException {
    public static final String ERROR = "Profession not found";

    public ProfessionNotFoundException() {
        super(ERROR);
    }

    public ProfessionNotFoundException(String message) {
        super(message, ERROR);
    }

    public ProfessionNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR);
    }

    public ProfessionNotFoundException(Throwable cause) {
        super(cause, ERROR);
    }

    public ProfessionNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR);
    }
}
