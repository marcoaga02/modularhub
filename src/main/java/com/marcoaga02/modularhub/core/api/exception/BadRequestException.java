package com.marcoaga02.modularhub.core.api.exception;

import org.springframework.http.HttpStatus;

public abstract class BadRequestException extends ApplicationException {

    protected BadRequestException(String errorCode, String logMessage) {
        super(errorCode, HttpStatus.BAD_REQUEST, logMessage);
    }

    protected BadRequestException(String errorCode, String logMessage, Throwable cause) {
        super(errorCode, HttpStatus.BAD_REQUEST, logMessage, cause);
    }
}

