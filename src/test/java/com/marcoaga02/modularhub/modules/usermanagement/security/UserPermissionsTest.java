package com.marcoaga02.modularhub.modules.usermanagement.security;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import static org.assertj.core.api.Assertions.assertThat;

class UserPermissionsTest {

    private final UserPermissions userPermissions = new UserPermissions();

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void canManageUsers_shouldReturnTrue_whenAuthenticationHasUserManagementRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "password", new SimpleGrantedAuthority("ROLE_USER_MANAGEMENT"))
        );

        assertThat(userPermissions.canManageUsers()).isTrue();
    }

    @Test
    void canManageUsers_shouldReturnFalse_whenAuthenticationLacksUserManagementRole() {
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken("user", "password", new SimpleGrantedAuthority("ROLE_SOME_OTHER_ROLE"))
        );

        assertThat(userPermissions.canManageUsers()).isFalse();
    }

    @Test
    void canManageUsers_shouldReturnFalse_whenNoAuthentication() {
        SecurityContextHolder.getContext().setAuthentication(null);

        assertThat(userPermissions.canManageUsers()).isFalse();
    }

}
