package com.marcoaga02.modularhub.core.api.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;
    private final HttpStatus httpStatus;
    private final String logMessage;

    protected ApplicationException(
            String errorCode,
            HttpStatus httpStatus,
            String logMessage
    ) {
        super(logMessage);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.logMessage = logMessage;
    }

    protected ApplicationException(
            String errorCode,
            HttpStatus httpStatus,
            String logMessage,
            Throwable cause
    ) {
        super(logMessage, cause);
        this.errorCode = errorCode;
        this.httpStatus = httpStatus;
        this.logMessage = logMessage;
    }
}
