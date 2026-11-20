package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.AccountPreferencesUpdateDTO;
import com.marcoaga02.modularhub.core.api.dto.LanguageDTO;
import com.marcoaga02.modularhub.core.api.mapper.AccountPreferencesMapper;
import com.marcoaga02.modularhub.core.domain.model.AccountPreferences;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import com.marcoaga02.modularhub.core.domain.repository.RoleRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.infrastructure.keycloak.KeycloakClaims;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Map;
import java.util.Optional;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.jwt;
import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
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
    private static final String INITIAL_ADMIN = "admin";

    private static final Jwt JWT = jwt(IDENTITY_ID, Map.of(
            KeycloakClaims.USERNAME, USERNAME,
            KeycloakClaims.EMAIL, EMAIL,
            KeycloakClaims.FIRST_NAME, FIRST_NAME,
            KeycloakClaims.LAST_NAME, LAST_NAME
    ));

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private AccountPreferencesMapper accountPreferencesMapper;

    private UserAccountService userAccountService;

    @BeforeEach
    void setUp() {
        userAccountService = new UserAccountService(userRepository, roleRepository, accountPreferencesMapper,
                INITIAL_ADMIN);
    }

    @Nested
    class SyncUser {

        @Test
        void syncUser_shouldCreateEnabledUserWithDefaultLanguageAndTokenClaims_whenUserDoesNotExist() {
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.empty());
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User result = userAccountService.syncUser(JWT);

            ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(saved.capture());
            assertThat(result).isSameAs(saved.getValue());
            assertClaimsCopied(result);
            assertThat(result)
                    .extracting(User::getIdentityId, User::getEnabled)
                    .containsExactly(IDENTITY_ID, true);
            assertThat(result.getPreferences().getLanguage()).isEqualTo(Language.EN);
        }

        @Test
        void syncUser_shouldRefreshClaimsAndKeepOtherData_whenUserExists() {
            User existing = user("existing", Language.IT);
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
            assertThat(preferences.getLanguage()).isEqualTo(Language.IT);
            verify(userRepository).save(existing);
        }

        private static void assertClaimsCopied(User user) {
            assertThat(user)
                    .extracting(User::getUsername, User::getEmail, User::getFirstname, User::getLastname)
                    .containsExactly(USERNAME, EMAIL, FIRST_NAME, LAST_NAME);
        }
    }

    @Nested
    class InitialAdministrator {

        private static final Jwt ADMIN_JWT = jwt(IDENTITY_ID, Map.of(KeycloakClaims.USERNAME, INITIAL_ADMIN));

        @Test
        void syncUser_shouldGrantAdminRole_whenInitialAdministratorLogsInAndNoActiveAdminExists() {
            Role admin = adminRole();
            mockSyncOf(ADMIN_JWT);
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(SecurityConstants.ADMIN_ROLE_CODE))
                    .thenReturn(0L);
            when(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE)).thenReturn(Optional.of(admin));

            User result = userAccountService.syncUser(ADMIN_JWT);

            assertThat(result.getRoles()).containsExactly(admin);
            verify(roleRepository).findByCode(SecurityConstants.ADMIN_ROLE_CODE);
        }

        @Test
        void syncUser_shouldNotGrantAdminRole_whenAnActiveAdminAlreadyExists() {
            mockSyncOf(ADMIN_JWT);
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(SecurityConstants.ADMIN_ROLE_CODE))
                    .thenReturn(1L);

            User result = userAccountService.syncUser(ADMIN_JWT);

            assertThat(result.getRoles()).isEmpty();
            verifyNoInteractions(roleRepository);
        }

        @Test
        void syncUser_shouldNotGrantAdminRole_whenUsernameIsNotTheInitialAdministrator() {
            mockSyncOf(JWT);

            User result = userAccountService.syncUser(JWT);

            assertThat(result.getRoles()).isEmpty();
            verify(userRepository, never()).countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(any());
            verifyNoInteractions(roleRepository);
        }

        @Test
        void syncUser_shouldNotGrantAdminRole_whenNoInitialAdministratorIsConfigured() {
            UserAccountService withoutInitialAdmin = new UserAccountService(userRepository, roleRepository,
                    accountPreferencesMapper, "");
            mockSyncOf(ADMIN_JWT);

            User result = withoutInitialAdmin.syncUser(ADMIN_JWT);

            assertThat(result.getRoles()).isEmpty();
            verify(userRepository, never()).countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(any());
            verifyNoInteractions(roleRepository);
        }

        @Test
        void syncUser_shouldLeaveRolesEmpty_whenAdminRoleIsMissing() {
            mockSyncOf(ADMIN_JWT);
            when(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(SecurityConstants.ADMIN_ROLE_CODE))
                    .thenReturn(0L);
            when(roleRepository.findByCode(SecurityConstants.ADMIN_ROLE_CODE)).thenReturn(Optional.empty());

            assertThat(userAccountService.syncUser(ADMIN_JWT).getRoles()).isEmpty();
        }

        private void mockSyncOf(Jwt jwt) {
            when(userRepository.findByIdentityId(jwt.getSubject())).thenReturn(Optional.of(user("existing", Language.EN)));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        }
    }

    @Nested
    class UpdatePreferences {

        @Test
        void updatePreferences_shouldSyncUserAndUpdatePreferencesThroughMapper_whenCalled() {
            User existing = user("existing", Language.EN);
            AccountPreferencesUpdateDTO request = new AccountPreferencesUpdateDTO().language(LanguageDTO.IT);
            AccountPreferencesResponseDTO response = new AccountPreferencesResponseDTO();
            when(userRepository.findByIdentityId(IDENTITY_ID)).thenReturn(Optional.of(existing));
            when(userRepository.save(existing)).thenReturn(existing);
            when(accountPreferencesMapper.toDto(existing.getPreferences())).thenReturn(response);

            AccountPreferencesResponseDTO result = userAccountService.updatePreferences(JWT, request);

            assertThat(result).isSameAs(response);
            assertThat(existing.getUsername()).isEqualTo(USERNAME);
            verify(userRepository).save(existing);
            verify(accountPreferencesMapper).updateEntity(request, existing.getPreferences());
            verify(accountPreferencesMapper).toDto(existing.getPreferences());
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
