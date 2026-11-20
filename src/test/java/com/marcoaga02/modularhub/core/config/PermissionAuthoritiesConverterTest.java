package com.marcoaga02.modularhub.core.config;

import com.marcoaga02.modularhub.core.domain.service.PermissionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;

import java.util.Set;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PermissionAuthoritiesConverterTest {

    private static final String SUBJECT = "subject";

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private PermissionAuthoritiesConverter converter;

    @Test
    void convert_shouldPrefixEveryPermissionOfTheTokenSubject() {
        when(permissionService.getEffectivePermissions(SUBJECT)).thenReturn(Set.of("USER_MANAGEMENT", "REPORTS"));

        assertThat(converter.convert(jwt(SUBJECT)))
                .extracting(GrantedAuthority::getAuthority)
                .containsExactlyInAnyOrder("PERMISSION_USER_MANAGEMENT", "PERMISSION_REPORTS");
        verify(permissionService).getEffectivePermissions(SUBJECT);
    }

    @Test
    void convert_shouldReturnNoAuthorities_whenSubjectHasNoPermissions() {
        when(permissionService.getEffectivePermissions(SUBJECT)).thenReturn(Set.of());

        assertThat(converter.convert(jwt(SUBJECT))).isEmpty();
    }
}
