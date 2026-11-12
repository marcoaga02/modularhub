package com.marcoaga02.modularhub.core.api.exception.account;

import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.ForbiddenException;

import java.util.Map;

public class SelfDeletionException extends ForbiddenException {

    public SelfDeletionException(String identityId) {
        super("Account attempted to delete itself: " + identityId, ExceptionCodes.Model.Account.SELF_DELETION, Map.of());
    }
}
