package com.marcoaga02.modularhub.core.api.exception;

import org.springframework.http.HttpStatus;

public abstract class ConflictException extends ApplicationException {

    protected ConflictException(String errorCode, String logMessage) {
        super(errorCode, HttpStatus.CONFLICT, logMessage);
    }

    protected ConflictException(String errorCode, String logMessage, Throwable cause) {
        super(errorCode, HttpStatus.CONFLICT, logMessage, cause);
    }
}
