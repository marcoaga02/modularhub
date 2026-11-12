package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityConfigTest {

    private final JwtAuthenticationConverter converter =
            new SecurityConfig(new CorsProperties(), null, null).jwtAuthenticationConverter();

    @Nested
    class JwtAuthenticationConverterBean {

        @Test
        void jwtAuthenticationConverter_shouldMapRealmRolesWithRolePrefix_whenRealmAccessHasRoles() {
            Jwt jwt = jwt(Map.of("realm_access", Map.of("roles", List.of("USER_MANAGEMENT", "OTHER"))));

            assertThat(authorities(jwt)).containsExactlyInAnyOrder("ROLE_USER_MANAGEMENT", "ROLE_OTHER");
        }

        @Test
        void jwtAuthenticationConverter_shouldReturnNoRoles_whenRealmAccessIsMissing() {
            Jwt jwt = jwt(Map.of("scope", "openid"));

            assertThat(authorities(jwt)).isEmpty();
        }

        @Test
        void jwtAuthenticationConverter_shouldReturnNoRoles_whenRealmAccessHasNoRoles() {
            Jwt jwt = jwt(Map.of("realm_access", Map.of("other", List.of("value"))));

            assertThat(authorities(jwt)).isEmpty();
        }

        private List<String> authorities(Jwt jwt) {
            return converter.convert(jwt).getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith(SecurityConstants.ROLE_PREFIX))
                    .toList();
        }

        private Jwt jwt(Map<String, Object> claims) {
            return Jwt.withTokenValue("token")
                    .header("alg", "none")
                    .subject("subject")
                    .claims(c -> c.putAll(claims))
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(60))
                    .build();
        }
    }
}
