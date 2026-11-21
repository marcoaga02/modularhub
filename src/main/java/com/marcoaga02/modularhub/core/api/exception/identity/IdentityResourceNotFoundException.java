package com.marcoaga02.modularhub.core.api.exception.identity;

import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.NotFoundException;

import java.util.Map;

public class IdentityResourceNotFoundException extends NotFoundException {

    public IdentityResourceNotFoundException(String message, Throwable cause) {
        super(message, ExceptionCodes.IdentityProvider.NOT_FOUND, Map.of(), cause);
    }
}
