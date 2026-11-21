package com.marcoaga02.modularhub.core.api.exception.identity;

import com.marcoaga02.modularhub.core.api.exception.ConflictException;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;

import java.util.Map;

public class IdentityConflictException extends ConflictException {

    public IdentityConflictException(String message, Throwable cause) {
        super(message, ExceptionCodes.IdentityProvider.CONFLICT, Map.of(), cause);
    }
}
