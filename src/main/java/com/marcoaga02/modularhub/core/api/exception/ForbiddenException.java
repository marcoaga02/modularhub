package com.marcoaga02.modularhub.core.api.exception;

import java.util.Map;

public abstract class ForbiddenException extends ApplicationException {

    protected ForbiddenException(String message, String errorCode, Map<String, Object> params) {
        super(message, errorCode, params);
    }

    protected ForbiddenException(String message, String errorCode, Map<String, Object> params, Throwable cause) {
        super(message, errorCode, params, cause);
    }
}
