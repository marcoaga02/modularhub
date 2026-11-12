package com.marcoaga02.modularhub.modules.usermanagement.api.exception;

public final class UserManagementExceptionCodes {

    private UserManagementExceptionCodes() {
        /* This utility class should not be instantiated */
    }

    public static final class Model {

        private Model() {
        }

        public static final class User {

            public static final String NOT_FOUND = "exception.model.user.not-found";
            public static final String ALREADY_EXISTS_TAX_ID = "exception.model.user.already-exists.tax-id";
            public static final String ALREADY_EXISTS_USERNAME = "exception.model.user.already-exists.username";
            public static final String ALREADY_EXISTS_EMAIL = "exception.model.user.already-exists.email";

            private User() {
            }
        }
    }
}
