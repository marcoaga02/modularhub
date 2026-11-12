package com.marcoaga02.modularhub.core.api.exception;

public final class ExceptionCodes {

    private ExceptionCodes() {
        /* This utility class should not be instantiated */
    }

    public static final class Internal {

        public static final String ERROR = "exception.internal.error";

        private Internal() {
        }
    }

    public static final class Validation {

        public static final String INVALID_FIELDS = "exception.validation.invalid-fields";
        public static final String INVALID_REQUEST = "exception.validation.invalid-request";

        private Validation() {
        }
    }

    public static final class Access {

        public static final String UNAUTHORIZED = "exception.access.unauthorized";
        public static final String DENIED = "exception.access.denied";

        private Access() {
        }
    }

    public static final class Generic {

        public static final String NOT_FOUND = "exception.generic.not-found";
        public static final String METHOD_NOT_ALLOWED = "exception.generic.method-not-allowed";
        public static final String CONCURRENT_MODIFICATION = "exception.generic.concurrent-modification";

        private Generic() {
        }
    }

    public static final class IdentityProvider {

        public static final String ERROR = "exception.identity-provider.error";
        public static final String NOT_FOUND = "exception.identity-provider.not-found";
        public static final String CONFLICT = "exception.identity-provider.conflict";

        private IdentityProvider() {
        }
    }

    public static final class Model {

        private Model() {
        }

        public static final class Language {

            public static final String NOT_FOUND = "exception.model.language.not-found";

            private Language() {
            }
        }

        public static final class Account {

            public static final String SELF_DELETION = "exception.model.account.self-deletion";

            private Account() {
            }
        }
    }
}
