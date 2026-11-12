package com.marcoaga02.modularhub.core.api.exception;

import java.util.Map;

public abstract class ServiceUnavailableException extends ApplicationException {

    protected ServiceUnavailableException(String message, String errorCode, Map<String, Object> params, Throwable cause) {
        super(message, errorCode, params, cause);
    }
}
