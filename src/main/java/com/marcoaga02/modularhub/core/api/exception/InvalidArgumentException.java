package com.marcoaga02.modularhub.core.api.exception;

import org.springframework.http.HttpStatus;

public class InvalidArgumentException extends ApplicationException {

    public InvalidArgumentException(String logMessage) {
        super(ExceptionCodes.INTERNAL_ERROR, HttpStatus.INTERNAL_SERVER_ERROR, logMessage);
    }
}
