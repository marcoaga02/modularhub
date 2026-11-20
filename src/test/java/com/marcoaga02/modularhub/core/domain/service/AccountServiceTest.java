package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.api.mapper.AccountMapper;
import com.marcoaga02.modularhub.core.domain.model.User;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.authenticate;
import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static com.marcoaga02.modularhub.fixture.SecurityFixtures.permissions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String ADMIN = "USER_MANAGEMENT";
    private static final String USER = "REPORTS";
    private static final String NO_JWT_MESSAGE = "No JWT authentication found in security context";

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private AccountMapper accountMapper;

    @Mock
    private PermissionService permissionService;

    @InjectMocks
    private AccountService accountService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetCurrentAccount {

        @Test
        void getCurrentAccount_shouldReturnMappedSyncedUserWithPermissionsFromRoles_whenJwtIsPresent() {
            Jwt jwt = authenticate(IDENTITY_ID);
            User user = new User();
            AccountDTO accountDTO = new AccountDTO().identityId(IDENTITY_ID);
            when(userAccountService.syncUser(jwt)).thenReturn(user);
            when(accountMapper.toDto(user)).thenReturn(accountDTO);
            when(permissionService.getEffectivePermissions(IDENTITY_ID)).thenReturn(Set.of(ADMIN));

            AccountDTO result = accountService.getCurrentAccount();

            assertThat(result).isSameAs(accountDTO);
            assertThat(result.getPermissions()).containsExactly(ADMIN);
            verify(userAccountService).syncUser(jwt);
            verify(accountMapper).toDto(user);
            verify(permissionService).getEffectivePermissions(IDENTITY_ID);
        }

        @Test
        void getCurrentAccount_shouldThrowInternalStateException_whenNoJwtAuthentication() {
            assertNoJwtAuthentication(() -> accountService.getCurrentAccount());

            verifyNoInteractions(userAccountService, accountMapper, permissionService);
        }
    }

    @Nested
    class GetPermissions {

        @Test
        void getPermissions_shouldReturnPermissionsWithoutPrefix_whenUserHasPermissions() {
            authenticate(IDENTITY_ID, ADMIN, USER);

            assertThat(accountService.getPermissions()).containsExactlyInAnyOrder(ADMIN, USER);
        }

        @Test
        void getPermissions_shouldIgnoreAuthoritiesWithoutPermissionPrefix() {
            List<GrantedAuthority> authorities = new ArrayList<>(permissions(ADMIN));
            authorities.add(new SimpleGrantedAuthority("SCOPE_openid"));
            authorities.add(new SimpleGrantedAuthority("FACTOR_BEARER"));
            authenticate(jwt(IDENTITY_ID), authorities);

            assertThat(accountService.getPermissions()).containsExactly(ADMIN);
        }

        @Test
        void getPermissions_shouldReturnEmptySet_whenUserHasNoPermissions() {
            authenticate(IDENTITY_ID);

            assertThat(accountService.getPermissions()).isEmpty();
        }

        @Test
        void getPermissions_shouldReturnUnmodifiableSet() {
            authenticate(IDENTITY_ID, ADMIN);

            Set<String> userPermissions = accountService.getPermissions();

            assertThatThrownBy(() -> userPermissions.add(USER)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void getPermissions_shouldThrowInternalStateException_whenNoAuthentication() {
            assertNoJwtAuthentication(() -> accountService.getPermissions());
        }

        @Test
        void getPermissions_shouldThrowInternalStateException_whenAuthenticationIsNotJwt() {
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated("user", null, permissions(ADMIN)));

            assertNoJwtAuthentication(() -> accountService.getPermissions());
        }
    }

    @Nested
    class HasPermission {

        @Test
        void hasPermission_shouldReturnTrue_whenUserHasThePermission() {
            authenticate(IDENTITY_ID, ADMIN);

            assertThat(accountService.hasPermission(ADMIN)).isTrue();
        }

        @Test
        void hasPermission_shouldReturnFalse_whenUserDoesNotHaveThePermission() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasPermission(ADMIN)).isFalse();
        }
    }

    @Nested
    class HasAnyPermission {

        @Test
        void hasAnyPermission_shouldReturnTrue_whenUserHasAtLeastOneOfThePermissions() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyPermission(ADMIN, USER)).isTrue();
        }

        @Test
        void hasAnyPermission_shouldReturnFalse_whenUserHasNoneOfThePermissions() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyPermission(ADMIN, "OTHER")).isFalse();
        }

        @Test
        void hasAnyPermission_shouldReturnFalse_whenNoPermissionIsRequested() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyPermission()).isFalse();
        }
    }

    @Nested
    class UpdateCurrentAccountPreferences {

        @Test
        void updateCurrentAccountPreferences_shouldDelegateWithCurrentJwt_whenJwtIsPresent() {
            Jwt jwt = authenticate(IDENTITY_ID);
            AccountPreferencesUpdateDTO request = new AccountPreferencesUpdateDTO().language(LanguageDTO.IT);
            AccountPreferencesResponseDTO response = new AccountPreferencesResponseDTO();
            when(userAccountService.updatePreferences(jwt, request)).thenReturn(response);

            AccountPreferencesResponseDTO result = accountService.updateCurrentAccountPreferences(request);

            assertThat(result).isSameAs(response);
            verify(userAccountService).updatePreferences(jwt, request);
        }

        @Test
        void updateCurrentAccountPreferences_shouldThrowInternalStateException_whenNoJwtAuthentication() {
            AccountPreferencesUpdateDTO request = new AccountPreferencesUpdateDTO().language(LanguageDTO.IT);

            assertNoJwtAuthentication(() -> accountService.updateCurrentAccountPreferences(request));

            verifyNoInteractions(userAccountService);
        }
    }

    @Nested
    class GetCurrentIdentityId {

        @Test
        void getCurrentIdentityId_shouldReturnTokenSubject_whenJwtIsPresent() {
            authenticate(IDENTITY_ID);

            assertThat(accountService.getCurrentIdentityId()).isEqualTo(IDENTITY_ID);
        }

        @Test
        void getCurrentIdentityId_shouldThrowInternalStateException_whenNoJwtAuthentication() {
            assertNoJwtAuthentication(() -> accountService.getCurrentIdentityId());
        }
    }

    private static void assertNoJwtAuthentication(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(InternalStateException.class, ex -> {
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex).hasMessage(NO_JWT_MESSAGE);
                });
    }
}
