package com.marcoaga02.modularhub.core.api.exception;

import lombok.Getter;

import java.util.Map;

@Getter
public abstract class ApplicationException extends RuntimeException {

    private final String errorCode;

    private final transient Map<String, Object> params;

    protected ApplicationException(String message, String errorCode, Map<String, Object> params) {
        super(message);
        this.errorCode = errorCode;
        this.params = params;
    }

    protected ApplicationException(String message, String errorCode, Map<String, Object> params, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.params = params;
    }
}
