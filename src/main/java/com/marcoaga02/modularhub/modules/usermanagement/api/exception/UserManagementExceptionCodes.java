package com.marcoaga02.modularhub.modules.usermanagement.api.exception;

public final class UserManagementExceptionCodes {

    private UserManagementExceptionCodes() {
        /* This utility class should not be instantiated */
    }

    public static final String USER_NOT_FOUND = "exception.model.user.not-found";

    public static final String USER_TAX_ID_ALREADY_EXISTS = "exception.model.user.already-exists.tax-id";

    public static final String USER_USERNAME_ALREADY_EXISTS = "exception.model.user.already-exists.username";

    public static final String USER_EMAIL_ALREADY_EXISTS = "exception.model.user.already-exists.email";
}
