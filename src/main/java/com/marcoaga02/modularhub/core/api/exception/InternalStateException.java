package com.marcoaga02.modularhub.core.api.exception;

import java.util.Map;

public class InternalStateException extends ApplicationException {

    public InternalStateException(String message) {
        super(message, ExceptionCodes.Internal.ERROR, Map.of());
    }
}
