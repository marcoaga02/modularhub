package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.domain.service.PermissionService;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SecurityConfigTest {

    private static final String SUBJECT = "subject";

    @Mock
    private PermissionService permissionService;

    @Nested
    class JwtAuthenticationConverterBean {

        @Test
        void jwtAuthenticationConverter_shouldGrantPermissionsResolvedForTheTokenSubject() {
            Jwt jwt = jwt(SUBJECT, Map.of("realm_access", Map.of("roles", List.of("IGNORED_REALM_ROLE"))));
            when(permissionService.getEffectivePermissions(SUBJECT)).thenReturn(Set.of("USER_MANAGEMENT"));

            assertThat(authorities(jwt)).containsExactly(SecurityConstants.PERMISSION_PREFIX + "USER_MANAGEMENT");
            verify(permissionService).getEffectivePermissions(SUBJECT);
        }

        @Test
        void jwtAuthenticationConverter_shouldGrantNoPermissions_whenSubjectHasNone() {
            when(permissionService.getEffectivePermissions(SUBJECT)).thenReturn(Set.of());

            assertThat(authorities(jwt(SUBJECT))).isEmpty();
        }

        private List<String> authorities(Jwt jwt) {
            JwtAuthenticationConverter converter = new SecurityConfig(new CorsProperties(), null, null,
                    new PermissionAuthoritiesConverter(permissionService)).jwtAuthenticationConverter();

            return converter.convert(jwt).getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .filter(authority -> authority.startsWith(SecurityConstants.PERMISSION_PREFIX))
                    .toList();
        }
    }
}
