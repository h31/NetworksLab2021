package ru.filimonov.vacancy.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class VacancyNotFoundException extends ApplicationException {
    public static final String ERROR = "Vacancy not found";

    public VacancyNotFoundException() {
        super(ERROR);
    }

    public VacancyNotFoundException(String message) {
        super(message, ERROR);
    }

    public VacancyNotFoundException(String message, Throwable cause) {
        super(message, cause, ERROR);
    }

    public VacancyNotFoundException(Throwable cause) {
        super(cause, ERROR);
    }

    public VacancyNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace, ERROR);
    }
}
