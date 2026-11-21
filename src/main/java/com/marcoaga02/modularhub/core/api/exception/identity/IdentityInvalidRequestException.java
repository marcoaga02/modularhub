package com.marcoaga02.modularhub.core.api.exception.identity;

import com.marcoaga02.modularhub.core.api.exception.BadRequestException;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;

import java.util.Map;

public class IdentityInvalidRequestException extends BadRequestException {

    public IdentityInvalidRequestException(String message, Throwable cause) {
        super(message, ExceptionCodes.IdentityProvider.INVALID_REQUEST, Map.of(), cause);
    }
}
