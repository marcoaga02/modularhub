package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.exception.language.LanguageNotFoundException;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static com.marcoaga02.modularhub.fixture.UserFixtures.language;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Transactional
class UserAccountServiceIT extends BaseITWithMockIdentity {

    private static final String IDENTITY_ID = "identity-id";

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LanguageRepository languageRepository;

    @Autowired
    private EntityManager entityManager;

    private Language italian;

    private Language english;

    @BeforeEach
    void setUp() {
        italian = languageRepository.save(language("it-IT", true));
        english = languageRepository.save(language("en-US", false));
    }

    @Nested
    class SyncUser {

        @Test
        void syncUser_shouldPersistNewEnabledUserWithDefaultLanguage_whenFirstLogin() {
            userAccountService.syncUser(jwtWithClaims("mario.rossi", "Mario"));
            flushAndClear();

            User saved = userRepository.findByIdentityId(IDENTITY_ID).orElseThrow();
            assertThat(saved)
                    .extracting(User::getUsername, User::getFirstname, User::getEnabled)
                    .containsExactly("mario.rossi", "Mario", true);
            assertThat(saved.getPreferences().getLanguage().getUuid()).isEqualTo(italian.getUuid());
        }

        @Test
        void syncUser_shouldUpdateClaimsWithoutCreatingAnotherUser_whenUserAlreadyExists() {
            User existing = user("existing", english);
            existing.setIdentityId(IDENTITY_ID);
            userRepository.save(existing);
            flushAndClear();

            userAccountService.syncUser(jwtWithClaims("renamed", "Renamed"));
            flushAndClear();

            assertThat(userRepository.findAll()).hasSize(1);
            User synced = userRepository.findByIdentityId(IDENTITY_ID).orElseThrow();
            assertThat(synced)
                    .extracting(User::getUuid, User::getUsername, User::getFirstname, User::getTaxIdNumber)
                    .containsExactly(existing.getUuid(), "renamed", "Renamed", existing.getTaxIdNumber());
            assertThat(synced.getPreferences().getLanguage().getUuid()).isEqualTo(english.getUuid());
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldPersistRequestedLanguage_whenLanguageExists() {
            Jwt jwt = jwtWithClaims("mario.rossi", "Mario");
            userAccountService.syncUser(jwt);
            flushAndClear();

            AccountPreferencesResponseDTO result = userAccountService.updatePreferences(jwt,
                    new AccountPreferencesUpdateDTO().languageId(english.getUuid()));
            flushAndClear();

            assertThat(result.getLanguage().getId()).isEqualTo(english.getUuid());
            User saved = userRepository.findByIdentityId(IDENTITY_ID).orElseThrow();
            assertThat(saved.getPreferences().getLanguage().getUuid()).isEqualTo(english.getUuid());
        }

        @Test
        void updatePreferences_shouldThrowLanguageNotFoundException_whenLanguageDoesNotExist() {
            Jwt jwt = jwtWithClaims("mario.rossi", "Mario");
            AccountPreferencesUpdateDTO request = new AccountPreferencesUpdateDTO().languageId("unknown-language");

            assertThatThrownBy(() -> userAccountService.updatePreferences(jwt, request))
                    .isInstanceOf(LanguageNotFoundException.class);
        }
    }

    @Nested
    class ResolveFullName {

        @Test
        void resolveFullName_shouldReturnFullNameOfStoredUser_whenIdentityIdExists() {
            User existing = user("existing", italian);
            existing.setIdentityId(IDENTITY_ID);
            userRepository.save(existing);
            flushAndClear();

            assertThat(userAccountService.resolveFullName(IDENTITY_ID)).isEqualTo("firstname-existing lastname-existing");
        }

        @Test
        void resolveFullName_shouldReturnEmpty_whenIdentityIdIsUnknown() {
            assertThat(userAccountService.resolveFullName("unknown")).isEmpty();
        }
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static Jwt jwtWithClaims(String username, String firstName) {
        return jwt(IDENTITY_ID, Map.of(
                KeycloakClaims.USERNAME, username,
                KeycloakClaims.EMAIL, username + "@test.com",
                KeycloakClaims.FIRST_NAME, firstName,
                KeycloakClaims.LAST_NAME, "Rossi"));
    }
}
