package com.marcoaga02.modularhub.core.infrastructure.keycloak;

public final class KeycloakClaims {

    private KeycloakClaims() {
        /* This utility class should not be instantiated */
    }

    public static final String REALM_ACCESS_ROLES = "realm_access.roles";

    public static final String EMAIL = "email";

    public static final String USERNAME = "preferred_username";

    public static final String FIRST_NAME = "given_name";

    public static final String LAST_NAME  = "family_name";

}
