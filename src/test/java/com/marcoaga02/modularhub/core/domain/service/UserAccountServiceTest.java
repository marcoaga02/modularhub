package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.InternalStateException;
import com.marcoaga02.modularhub.core.api.exception.language.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    private static final String IDENTITY_ID = "identity-123";

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private AccountPreferencesMapper accountPreferencesMapper;

    @InjectMocks
    private UserAccountService userAccountService;

    private Jwt jwt;

    private Language defaultLanguage;

    @BeforeEach
    void setUp() {
        jwt = new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "RS256"),
                Map.of(
                        "sub", IDENTITY_ID,
                        "preferred_username", "mario.rossi",
                        "email", "mario@test.com",
                        "given_name", "Mario",
                        "family_name", "Rossi"
                )
        );

        defaultLanguage = new Language();
        defaultLanguage.setCode("it-IT");
        defaultLanguage.setIsDefault(true);
    }

    @Test
    void syncUser_shouldCreateUserWithDefaultLanguage_whenUserNotExists() {
        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());
        when(languageRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultLanguage));
        when(userRepository.save(any(User.class))).thenAnswer(inv -> inv.getArgument(0));

        User result = userAccountService.syncUser(jwt);

        assertThat(result.getIdentityId()).isEqualTo(IDENTITY_ID);
        assertThat(result.getUsername()).isEqualTo("mario.rossi");
        assertThat(result.getEmail()).isEqualTo("mario@test.com");
        assertThat(result.getFirstname()).isEqualTo("Mario");
        assertThat(result.getLastname()).isEqualTo("Rossi");
        assertThat(result.getEnabled()).isTrue();
        assertThat(result.getPreferences()).isNotNull();
        assertThat(result.getPreferences().getLanguage()).isSameAs(defaultLanguage);

        verify(userRepository).save(result);
    }

    @Test
    void syncUser_shouldUpdateClaimsAndKeepData_whenUserExists() {
        AccountPreferences preferences = new AccountPreferences();
        Language language = new Language();
        preferences.setLanguage(language);

        User existing = new User();
        existing.setIdentityId(IDENTITY_ID);
        existing.setUsername("old.username");
        existing.setEmail("old@test.com");
        existing.setFirstname("Old");
        existing.setLastname("Name");
        existing.setEnabled(false);
        existing.setTaxIdNumber("TAX");
        existing.setPreferences(preferences);

        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);

        User result = userAccountService.syncUser(jwt);

        assertThat(result).isSameAs(existing);
        assertThat(result.getUsername()).isEqualTo("mario.rossi");
        assertThat(result.getEmail()).isEqualTo("mario@test.com");
        assertThat(result.getFirstname()).isEqualTo("Mario");
        assertThat(result.getLastname()).isEqualTo("Rossi");
        assertThat(result.getEnabled()).isFalse();
        assertThat(result.getTaxIdNumber()).isEqualTo("TAX");
        assertThat(result.getPreferences()).isSameAs(preferences);
        assertThat(result.getPreferences().getLanguage()).isSameAs(language);

        verify(languageRepository, never()).findByIsDefaultTrue();
    }

    @Test
    void syncUser_shouldThrowInternalStateException_whenNoDefaultLanguage() {
        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());
        when(languageRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userAccountService.syncUser(jwt))
                .isInstanceOf(InternalStateException.class)
                .satisfies(e -> {
                    InternalStateException ex = (InternalStateException) e;
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                    assertThat(ex.getMessage()).isEqualTo("No default language configured");
                });

        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePreferences_shouldUpdateLanguage_whenLanguageExists() {
        User existing = new User();
        existing.setIdentityId(IDENTITY_ID);
        existing.setPreferences(new AccountPreferences());

        Language newLanguage = new Language();
        AccountPreferencesResponseDTO expected = new AccountPreferencesResponseDTO();

        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(languageRepository.findByUuid("lang-id")).thenReturn(Optional.of(newLanguage));
        when(accountPreferencesMapper.toDto(existing.getPreferences())).thenReturn(expected);

        AccountPreferencesResponseDTO result =
                userAccountService.updatePreferences(jwt, new AccountPreferencesUpdateDTO().languageId("lang-id"));

        assertThat(result).isSameAs(expected);
        assertThat(existing.getPreferences().getLanguage()).isSameAs(newLanguage);
    }

    @Test
    void updatePreferences_shouldThrowLanguageNotFoundException_whenLanguageNotExists() {
        User existing = new User();
        existing.setIdentityId(IDENTITY_ID);
        existing.setPreferences(new AccountPreferences());

        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
        when(userRepository.save(existing)).thenReturn(existing);
        when(languageRepository.findByUuid("missing")).thenReturn(Optional.empty());

        AccountPreferencesUpdateDTO dto = new AccountPreferencesUpdateDTO().languageId("missing");

        assertThatThrownBy(() -> userAccountService.updatePreferences(jwt, dto))
                .isInstanceOf(LanguageNotFoundException.class)
                .satisfies(e -> assertThat(((LanguageNotFoundException) e).getErrorCode())
                        .isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND));

        verifyNoInteractions(accountPreferencesMapper);
    }

    @Test
    void resolveFullName_shouldReturnEmpty_whenIdentityIdIsNull() {
        assertThat(userAccountService.resolveFullName(null)).isEmpty();

        verifyNoInteractions(userRepository);
    }

    @Test
    void resolveFullName_shouldReturnEmpty_whenUserNotFound() {
        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());

        assertThat(userAccountService.resolveFullName(IDENTITY_ID)).isEmpty();
    }

    @Test
    void resolveFullName_shouldReturnFullName_whenUserFound() {
        User user = new User();
        user.setFirstname("Mario");
        user.setLastname("Rossi");

        when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(user));

        assertThat(userAccountService.resolveFullName(IDENTITY_ID)).isEqualTo("Mario Rossi");
    }
}
