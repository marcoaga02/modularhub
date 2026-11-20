package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.config.BaseITWithMockIdentity;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserAccountServiceIT extends BaseITWithMockIdentity {

    private static final String IDENTITY_ID = "identity-id";

    @Autowired
    private UserAccountService userAccountService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

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
            assertThat(saved.getPreferences().getLanguage()).isEqualTo(Language.DEFAULT);
        }

        @Test
        void syncUser_shouldUpdateClaimsWithoutCreatingAnotherUser_whenUserAlreadyExists() {
            User existing = user("existing", Language.EN);
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
            assertThat(synced.getPreferences().getLanguage()).isEqualTo(Language.EN);
        }
    }

    @Nested
    class InitialAdministrator {

        @Test
        void syncUser_shouldGrantTheSeededAdminRole_whenInitialAdministratorLogsInWithoutActiveAdmins() {
            userAccountService.syncUser(jwtWithClaims("admin", "Admin"));
            flushAndClear();

            User saved = userRepository.findByIdentityId(IDENTITY_ID).orElseThrow();
            assertThat(saved.getRoles())
                    .extracting(Role::getCode)
                    .containsExactly(SecurityConstants.ADMIN_ROLE_CODE);
        }

        @Test
        void syncUser_shouldNotGrantAdminRole_whenAnotherActiveAdminExists() {
            User otherAdmin = user("other-admin", Language.EN);
            otherAdmin.getRoles().add(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE).orElseThrow());
            userRepository.save(otherAdmin);
            flushAndClear();

            userAccountService.syncUser(jwtWithClaims("admin", "Admin"));
            flushAndClear();

            assertThat(userRepository.findByIdentityId(IDENTITY_ID).orElseThrow().getRoles()).isEmpty();
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldPersistRequestedLanguage_whenRequestIsValid() {
            Jwt jwt = jwtWithClaims("mario.rossi", "Mario");
            userAccountService.syncUser(jwt);
            flushAndClear();

            AccountPreferencesResponseDTO result = userAccountService.updatePreferences(jwt,
                    new AccountPreferencesUpdateDTO().language(LanguageDTO.IT));
            flushAndClear();

            assertThat(result.getLanguage()).isEqualTo(LanguageDTO.IT);
            User saved = userRepository.findByIdentityId(IDENTITY_ID).orElseThrow();
            assertThat(saved.getPreferences().getLanguage()).isEqualTo(Language.IT);
        }
    }

    @Nested
    class ResolveFullName {

        @Test
        void resolveFullName_shouldReturnFullNameOfStoredUser_whenIdentityIdExists() {
            User existing = user("existing", Language.IT);
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
