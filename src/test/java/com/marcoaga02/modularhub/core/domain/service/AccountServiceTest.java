package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.*;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.spi.AccountDetailsProvider;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
    private AccountPreferencesService accountPreferencesService;

    @Mock
    private AccountDetailsProvider accountDetailsProvider;

    @InjectMocks
    private AccountService accountService;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentAccount_shouldReturnCorrectAccount() {
        final String identityId = "uuid-123";
        mockAuthentication(
                Map.of(
                        "sub", identityId,
                        "email", "mario@test.com",
                        "preferred_username", "mario.rossi",
                        "given_name", "Mario",
                        "family_name", "Rossi"
                ),
                List.of("ROLE")
        );

        LanguageResponseDTO languageResponseDTO = new LanguageResponseDTO();
        languageResponseDTO.setId("lang-id");
        languageResponseDTO.setCode("it-IT");
        languageResponseDTO.setLabel("italian");
        languageResponseDTO.setIsDefault(true);

        AccountPreferencesResponseDTO preferencesDTO = new AccountPreferencesResponseDTO();
        preferencesDTO.setLanguage(languageResponseDTO);

        AccountProfileDTO profileDTO = new AccountProfileDTO();
        profileDTO.setGender(Gender.M);
        profileDTO.setMobileNumber("mobileNumber");
        profileDTO.setTaxIdNumber("taxIdNumber");


        when(accountPreferencesService.getAccountPreferences(identityId)).thenReturn(preferencesDTO);
        when(accountDetailsProvider.getDetails(identityId)).thenReturn(profileDTO);

        AccountDTO account = accountService.getCurrentAccount();

        assertThat(account.getIdentityId()).isEqualTo(identityId);
        assertThat(account.getEmail()).isEqualTo("mario@test.com");
        assertThat(account.getUsername()).isEqualTo("mario.rossi");
        assertThat(account.getFirstName()).isEqualTo("Mario");
        assertThat(account.getLastName()).isEqualTo("Rossi");
        assertThat(account.getRoles()).containsExactly("ROLE");
        assertThat(account.getPreferences()).isEqualTo(preferencesDTO);
        assertThat(account.getGender()).isEqualTo(Gender.M);
        assertThat(account.getMobileNumber()).isEqualTo("mobileNumber");
        assertThat(account.getTaxIdNumber()).isEqualTo("taxIdNumber");

        verify(accountPreferencesService).getAccountPreferences(identityId);
        verify(accountDetailsProvider).getDetails(identityId);
    }

    @Test
    void getCurrentAccount_shouldThrowIllegalStateException_whenNoJwtAuthentication() {
        SecurityContextHolder.clearContext();
        assertThatThrownBy(() -> accountService.getCurrentAccount())
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("No JWT authentication found in security context");
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("No JWT authentication found in security context");
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
        final String identityId = "uuid-123";
        mockAuthentication(Map.of("sub", identityId), List.of());

        AccountPreferencesUpdateDTO dto = new AccountPreferencesUpdateDTO();
        dto.setLanguageId("lang-456");

        LanguageResponseDTO language = new LanguageResponseDTO();
        language.setId("lang-456");
        language.setCode("en-US");
        language.setLabel("English");

        AccountPreferencesResponseDTO expected = new AccountPreferencesResponseDTO();
        expected.setLanguage(language);

        when(accountPreferencesService.updateAccountPreferencesByIdentityId(identityId, dto))
                .thenReturn(expected);

        AccountPreferencesResponseDTO result = accountService.updateCurrentAccountPreferences(dto);

        assertThat(result).isEqualTo(expected);
        verify(accountPreferencesService).updateAccountPreferencesByIdentityId(identityId, dto);
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("No JWT authentication found in security context");
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
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("No JWT authentication found in security context");
                });
    }

    private void mockAuthentication(Map<String, Object> claims, List<String> roles) {
        Map<String, Object> headers = Map.of("alg", "RS256");
        Jwt jwt = new Jwt("token", Instant.now(), Instant.now().plusSeconds(300), headers, claims);

        List<GrantedAuthority> authorities = roles.stream()
                .map(r -> (GrantedAuthority) new SimpleGrantedAuthority(SecurityConstants.ROLE_PREFIX + r))
                .toList();

        JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}