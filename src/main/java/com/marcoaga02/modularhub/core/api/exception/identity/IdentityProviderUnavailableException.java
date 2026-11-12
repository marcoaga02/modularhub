package com.marcoaga02.modularhub.core.api.exception.identity;

import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.ServiceUnavailableException;

import java.util.Map;

public class IdentityProviderUnavailableException extends ServiceUnavailableException {

    public IdentityProviderUnavailableException(String message, Throwable cause) {
        super(message, ExceptionCodes.IdentityProvider.ERROR, Map.of(), cause);
    }
}
