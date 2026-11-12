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
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static com.marcoaga02.modularhub.fixture.UserFixtures.language;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAccountServiceTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String USERNAME = "mario.rossi";
    private static final String EMAIL = "mario@test.com";
    private static final String FIRST_NAME = "Mario";
    private static final String LAST_NAME = "Rossi";
    private static final String LANGUAGE_ID = "language-id";

    private static final Jwt JWT = jwt(IDENTITY_ID, Map.of(
            KeycloakClaims.USERNAME, USERNAME,
            KeycloakClaims.EMAIL, EMAIL,
            KeycloakClaims.FIRST_NAME, FIRST_NAME,
            KeycloakClaims.LAST_NAME, LAST_NAME
    ));

    @Mock
    private UserRepository userRepository;

    @Mock
    private LanguageRepository languageRepository;

    @Mock
    private AccountPreferencesMapper accountPreferencesMapper;

    @InjectMocks
    private UserAccountService userAccountService;

    @Nested
    class SyncUser {

        @Test
        void syncUser_shouldCreateEnabledUserWithDefaultLanguageAndTokenClaims_whenUserDoesNotExist() {
            Language defaultLanguage = language("it-IT", true);
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());
            when(languageRepository.findByIsDefaultTrue()).thenReturn(Optional.of(defaultLanguage));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userAccountService.syncUser(JWT);

            ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(saved.capture());
            assertThat(result).isSameAs(saved.getValue());
            assertClaimsCopied(result);
            assertThat(result)
                    .extracting(User::getIdentityId, User::getEnabled)
                    .containsExactly(IDENTITY_ID, true);
            assertThat(result.getPreferences().getLanguage()).isSameAs(defaultLanguage);
        }

        @Test
        void syncUser_shouldRefreshClaimsAndKeepOtherData_whenUserExists() {
            Language language = language("en-US", false);
            User existing = user("existing", language);
            existing.setEnabled(false);
            AccountPreferences preferences = existing.getPreferences();
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);

            User result = userAccountService.syncUser(JWT);

            assertThat(result).isSameAs(existing);
            assertClaimsCopied(result);
            assertThat(result)
                    .extracting(User::getEnabled, User::getTaxIdNumber, User::getPreferences)
                    .containsExactly(false, "TAX-existing", preferences);
            assertThat(preferences.getLanguage()).isSameAs(language);
            verify(userRepository).save(existing);
            verifyNoInteractions(languageRepository);
        }

        @Test
        void syncUser_shouldThrowInternalStateException_whenUserDoesNotExistAndNoDefaultLanguage() {
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());
            when(languageRepository.findByIsDefaultTrue()).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userAccountService.syncUser(JWT))
                    .isInstanceOfSatisfying(InternalStateException.class, ex -> {
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Internal.ERROR);
                        assertThat(ex).hasMessage("No default language configured");
                    });

            verify(userRepository, never()).save(any());
        }

        private static void assertClaimsCopied(User user) {
            assertThat(user)
                    .extracting(User::getUsername, User::getEmail, User::getFirstname, User::getLastname)
                    .containsExactly(USERNAME, EMAIL, FIRST_NAME, LAST_NAME);
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldSyncUserAndSetRequestedLanguage_whenLanguageExists() {
            User existing = user("existing", language("en-US", false));
            Language newLanguage = language("it-IT", true);
            AccountPreferencesResponseDTO response = new AccountPreferencesResponseDTO();
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);
            when(languageRepository.findByUuid(LANGUAGE_ID)).thenReturn(Optional.of(newLanguage));
            when(accountPreferencesMapper.toDto(existing.getPreferences())).thenReturn(response);

            AccountPreferencesResponseDTO result = userAccountService.updatePreferences(JWT, updateRequest());

            assertThat(result).isSameAs(response);
            assertThat(existing.getPreferences().getLanguage()).isSameAs(newLanguage);
            assertThat(existing.getUsername()).isEqualTo(USERNAME);
            verify(userRepository).save(existing);
            verify(accountPreferencesMapper).toDto(existing.getPreferences());
        }

        @Test
        void updatePreferences_shouldThrowLanguageNotFoundException_whenLanguageDoesNotExist() {
            Language currentLanguage = language("en-US", false);
            User existing = user("existing", currentLanguage);
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);
            when(languageRepository.findByUuid(LANGUAGE_ID)).thenReturn(Optional.empty());
            AccountPreferencesUpdateDTO request = updateRequest();

            assertThatThrownBy(() -> userAccountService.updatePreferences(JWT, request))
                    .isInstanceOfSatisfying(LanguageNotFoundException.class, ex -> {
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.Model.Language.NOT_FOUND);
                        assertThat(ex).hasMessage("Language not found: " + LANGUAGE_ID);
                    });

            assertThat(existing.getPreferences().getLanguage()).isSameAs(currentLanguage);
            verifyNoInteractions(accountPreferencesMapper);
        }

        private static AccountPreferencesUpdateDTO updateRequest() {
            return new AccountPreferencesUpdateDTO().languageId(LANGUAGE_ID);
        }
    }

    @Nested
    class ResolveFullName {

        @Test
        void resolveFullName_shouldReturnEmpty_whenIdentityIdIsNull() {
            assertThat(userAccountService.resolveFullName(null)).isEmpty();

            verifyNoInteractions(userRepository);
        }

        @Test
        void resolveFullName_shouldReturnEmpty_whenUserIsNotFound() {
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());

            assertThat(userAccountService.resolveFullName(IDENTITY_ID)).isEmpty();

            verify(userRepository).findByIdentityId(IDENTITY_ID);
        }

        @Test
        void resolveFullName_shouldReturnFirstnameAndLastname_whenUserIsFound() {
            User user = new User();
            user.setFirstname(FIRST_NAME);
            user.setLastname(LAST_NAME);
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(user));

            assertThat(userAccountService.resolveFullName(IDENTITY_ID)).isEqualTo("Mario Rossi");

            verify(userRepository).findByIdentityId(IDENTITY_ID);
        }
    }
}
