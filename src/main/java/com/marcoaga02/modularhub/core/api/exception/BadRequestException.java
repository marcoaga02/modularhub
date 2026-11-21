package com.marcoaga02.modularhub.core.api.exception;

import java.util.Map;

public abstract class BadRequestException extends ApplicationException {

    protected BadRequestException(String message, String errorCode, Map<String, Object> params) {
        super(message, errorCode, params);
    }

    protected BadRequestException(String message, String errorCode, Map<String, Object> params, Throwable cause) {
        super(message, errorCode, params, cause);
    }
}
