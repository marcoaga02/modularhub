package com.marcoaga02.modularhub.core.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.authenticate;
import static org.assertj.core.api.Assertions.assertThat;

class AuditorAwareImplTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String PRINCIPAL_NAME = "service-user";

    private final AuditorAwareImpl auditorAware = new AuditorAwareImpl();

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAuditor_shouldReturnTokenSubject_whenAuthenticationIsJwt() {
        authenticate(IDENTITY_ID);

        assertThat(auditorAware.getCurrentAuditor()).contains(IDENTITY_ID);
    }

    @Test
    void getCurrentAuditor_shouldReturnPrincipalName_whenAuthenticationIsNotJwt() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(PRINCIPAL_NAME, null, List.of()));

        assertThat(auditorAware.getCurrentAuditor()).contains(PRINCIPAL_NAME);
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenAuthenticationIsNotAuthenticated() {
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.unauthenticated(PRINCIPAL_NAME, null));

        assertThat(auditorAware.getCurrentAuditor()).isEmpty();
    }

    @Test
    void getCurrentAuditor_shouldReturnEmpty_whenNoAuthentication() {
        assertThat(auditorAware.getCurrentAuditor()).isEmpty();
    }
}
