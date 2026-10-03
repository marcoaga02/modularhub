package com.marcoaga02.modularhub.core.api.exception;

public final class ExceptionCodes {

    private ExceptionCodes() {
        /* This utility class should not be instantiated */
    }

    public static final String INTERNAL_ERROR = "exception.internal.error";

    public static final String VALIDATION_INVALID_FIELDS = "exception.validation.invalid-fields";

    public static final String IDENTITY_PROVIDER_ERROR = "exception.identity-provider.error";

    public static final String LANGUAGE_NOT_FOUND = "exception.model.language.not-found";

    public static final String ACCOUNT_PREFERENCES_NOT_FOUND = "exception.model.account-preferences.not-found";

    public static final String SELF_DELETION = "exception.model.account.self-deletion";

    public static final String ACCESS_DENIED = "exception.access-denied";

}
