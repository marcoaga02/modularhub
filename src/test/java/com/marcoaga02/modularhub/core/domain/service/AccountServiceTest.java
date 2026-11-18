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
import static com.marcoaga02.modularhub.fixture.SecurityFixtures.roles;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String ADMIN = "ADMIN";
    private static final String USER = "USER";
    private static final String NO_JWT_MESSAGE = "No JWT authentication found in security context";

    @Mock
    private UserAccountService userAccountService;

    @Mock
    private AccountMapper accountMapper;

    @InjectMocks
    private AccountService accountService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class GetCurrentAccount {

        @Test
        void getCurrentAccount_shouldReturnMappedSyncedUserWithTokenRoles_whenJwtIsPresent() {
            Jwt jwt = authenticate(IDENTITY_ID, ADMIN);
            User user = new User();
            AccountDTO accountDTO = new AccountDTO().identityId(IDENTITY_ID);
            when(userAccountService.syncUser(jwt)).thenReturn(user);
            when(accountMapper.toDto(user)).thenReturn(accountDTO);

            AccountDTO result = accountService.getCurrentAccount();

            assertThat(result).isSameAs(accountDTO);
            assertThat(result.getRoles()).containsExactly(ADMIN);
            verify(userAccountService).syncUser(jwt);
            verify(accountMapper).toDto(user);
        }

        @Test
        void getCurrentAccount_shouldThrowInternalStateException_whenNoJwtAuthentication() {
            assertNoJwtAuthentication(() -> accountService.getCurrentAccount());

            verifyNoInteractions(userAccountService, accountMapper);
        }
    }

    @Nested
    class GetRoles {

        @Test
        void getRoles_shouldReturnRolesWithoutPrefix_whenUserHasRoles() {
            authenticate(IDENTITY_ID, ADMIN, USER);

            assertThat(accountService.getRoles()).containsExactlyInAnyOrder(ADMIN, USER);
        }

        @Test
        void getRoles_shouldIgnoreAuthoritiesWithoutRolePrefix() {
            List<GrantedAuthority> authorities = new ArrayList<>(roles(ADMIN));
            authorities.add(new SimpleGrantedAuthority("SCOPE_openid"));
            authorities.add(new SimpleGrantedAuthority("FACTOR_BEARER"));
            authenticate(jwt(IDENTITY_ID), authorities);

            assertThat(accountService.getRoles()).containsExactly(ADMIN);
        }

        @Test
        void getRoles_shouldReturnEmptySet_whenUserHasNoRoles() {
            authenticate(IDENTITY_ID);

            assertThat(accountService.getRoles()).isEmpty();
        }

        @Test
        void getRoles_shouldReturnUnmodifiableSet() {
            authenticate(IDENTITY_ID, ADMIN);

            Set<String> roles = accountService.getRoles();

            assertThatThrownBy(() -> roles.add(USER)).isInstanceOf(UnsupportedOperationException.class);
        }

        @Test
        void getRoles_shouldThrowInternalStateException_whenNoAuthentication() {
            assertNoJwtAuthentication(() -> accountService.getRoles());
        }

        @Test
        void getRoles_shouldThrowInternalStateException_whenAuthenticationIsNotJwt() {
            SecurityContextHolder.getContext().setAuthentication(
                    UsernamePasswordAuthenticationToken.authenticated("user", null, roles(ADMIN)));

            assertNoJwtAuthentication(() -> accountService.getRoles());
        }
    }

    @Nested
    class HasRole {

        @Test
        void hasRole_shouldReturnTrue_whenUserHasTheRole() {
            authenticate(IDENTITY_ID, ADMIN);

            assertThat(accountService.hasRole(ADMIN)).isTrue();
        }

        @Test
        void hasRole_shouldReturnFalse_whenUserDoesNotHaveTheRole() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasRole(ADMIN)).isFalse();
        }
    }

    @Nested
    class HasAnyRole {

        @Test
        void hasAnyRole_shouldReturnTrue_whenUserHasAtLeastOneOfTheRoles() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyRole(ADMIN, USER)).isTrue();
        }

        @Test
        void hasAnyRole_shouldReturnFalse_whenUserHasNoneOfTheRoles() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyRole(ADMIN, "OTHER")).isFalse();
        }

        @Test
        void hasAnyRole_shouldReturnFalse_whenNoRoleIsRequested() {
            authenticate(IDENTITY_ID, USER);

            assertThat(accountService.hasAnyRole()).isFalse();
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
