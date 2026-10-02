package com.marcoaga02.modularhub.core.api.exception;

import static com.marcoaga02.modularhub.core.api.exception.ExceptionCodes.SELF_DELETION;

public class SelfDeletionException extends ConflictException {

    public SelfDeletionException(String userId) {
        super(SELF_DELETION, String.format("Account with identityId '%s' attempted to delete their own account", userId));
    }

}
