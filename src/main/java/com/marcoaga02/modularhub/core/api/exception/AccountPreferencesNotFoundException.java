package com.marcoaga02.modularhub.core.api.exception;

public class AccountPreferencesNotFoundException extends NotFoundException {

    public AccountPreferencesNotFoundException(String identityId) {
        super(ExceptionCodes.ACCOUNT_PREFERENCES_NOT_FOUND,
                String.format("AccountPreferences for identityId '%s' not found", identityId));
    }

}
