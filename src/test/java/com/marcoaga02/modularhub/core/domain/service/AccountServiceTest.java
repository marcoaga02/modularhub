package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.*;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.api.mapper.AccountMapper;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAccount_shouldReturnSyncedAccountWithRoles() {
        Jwt jwt = mockAuthentication(Map.of("sub", "uuid-123"), List.of("ROLE"));

        User user = new User();
        AccountDTO accountDTO = new AccountDTO()
                .identityId("uuid-123")
                .username("mario.rossi");

        when(userAccountService.syncUser(jwt)).thenReturn(user);
        when(accountMapper.toDto(user)).thenReturn(accountDTO);

        AccountDTO account = accountService.getCurrentAccount();

        assertThat(account).isSameAs(accountDTO);
        assertThat(account.getIdentityId()).isEqualTo("uuid-123");
        assertThat(account.getUsername()).isEqualTo("mario.rossi");
        assertThat(account.getRoles()).containsExactly("ROLE");

        verify(userAccountService).syncUser(jwt);
        verify(accountMapper).toDto(user);
    }

    @Test
    void getCurrentAccount_shouldThrowIllegalStateException_whenNoJwtAuthentication() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> accountService.getCurrentAccount())
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex.getMessage()).isEqualTo("No JWT authentication found in security context");
                });
    }

    @Test
    void getRoles_shouldReturnRolesWithoutPrefix() {
        mockAuthentication(Map.of("sub", "uuid-123"), List.of("ADMIN", "USER"));

        Set<String> roles = accountService.getRoles();

        assertThat(roles).containsExactlyInAnyOrder("ADMIN", "USER");
    }

    @Test
    void getRoles_shouldReturnEmptySet_whenNoRoles() {
        mockAuthentication(Map.of("sub", "uuid-123"), List.of());

        Set<String> roles = accountService.getRoles();

        assertThat(roles).isEmpty();
    }

    @Test
    void getRoles_shouldThrowIllegalStateException_whenNoJwtAuthentication() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> accountService.getRoles())
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex.getMessage()).isEqualTo("No JWT authentication found in security context");
                });
    }

    @Test
    void hasRole_shouldReturnTrue_whenRoleExists() {
        mockAuthentication(Map.of("sub", "uuid-123"), List.of("ROLE"));
        assertThat(accountService.hasRole("ROLE")).isTrue();
    }

    @Test
    void hasRole_shouldReturnFalse_whenRoleNotExists() {
        mockAuthentication(Map.of("sub", "uuid-123"), List.of());
        assertThat(accountService.hasRole("ROLE")).isFalse();
    }

    @Test
    void hasAnyRole_shouldReturnTrue_whenAtLeastOneRoleExists() {
        mockAuthentication(Map.of("sub", "uuid-123"), List.of("ROLE"));
        assertThat(accountService.hasAnyRole("ROLE", "ADMIN")).isTrue();
    }

    @Test
    void updateCurrentAccountPreferences_shouldReturnUpdatedPreferences() {
        Jwt jwt = mockAuthentication(Map.of("sub", "uuid-123"), List.of());

        AccountPreferencesUpdateDTO dto = new AccountPreferencesUpdateDTO();
        dto.setLanguageId("lang-456");

        LanguageResponseDTO language = new LanguageResponseDTO();
        language.setId("lang-456");
        language.setCode("en-US");
        language.setLabel("English");

        AccountPreferencesResponseDTO expected = new AccountPreferencesResponseDTO();
        expected.setLanguage(language);

        when(userAccountService.updatePreferences(jwt, dto)).thenReturn(expected);

        AccountPreferencesResponseDTO result = accountService.updateCurrentAccountPreferences(dto);

        assertThat(result).isEqualTo(expected);
        verify(userAccountService).updatePreferences(jwt, dto);
    }

    @Test
    void updateCurrentAccountPreferences_shouldThrowInternalStateException_whenNoJwtAuthentication() {
        SecurityContextHolder.clearContext();

        AccountPreferencesUpdateDTO dto = new AccountPreferencesUpdateDTO();
        dto.setLanguageId("lang-456");

        assertThatThrownBy(() -> accountService.updateCurrentAccountPreferences(dto))
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex.getMessage()).isEqualTo("No JWT authentication found in security context");
                });
    }

    @Test
    void getCurrentIdentityId_shouldReturnCurrentIdentityId_whenValid() {
        String currentId = "current-id";
        mockAuthentication(Map.of("sub", currentId), List.of());

        String result = accountService.getCurrentIdentityId();

        assertThat(result).isEqualTo(currentId);
    }

    @Test
    void getCurrentIdentityId_shouldThrowIllegalStateException_whenNoJwtAuthentication() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> accountService.getCurrentIdentityId())
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex.getMessage()).isEqualTo("No JWT authentication found in security context");
                });
    }

    private Jwt mockAuthentication(Map<String, Object> claims, List<String> roles) {
        Map<String, Object> headers = Map.of("alg", "RS256");
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), headers, claims);

        List<GrantedAuthority> authorities = roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + r))
                .toList();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        return jwt;
    }
}