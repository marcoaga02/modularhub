package com.marcoaga02.modularhub.core.api.exception;

import org.springframework.http.HttpStatus;

public abstract class NotFoundException extends ApplicationException {

    protected NotFoundException(String errorCode, String logMessage) {
        super(errorCode, HttpStatus.NOT_FOUND, logMessage);
    }

    protected NotFoundException(String errorCode, String logMessage, Throwable cause) {
        super(errorCode, HttpStatus.NOT_FOUND, logMessage, cause);
    }
}
