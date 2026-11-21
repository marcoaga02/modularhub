package com.marcoaga02.modularhub.core.domain.repository;

import com.marcoaga02.modularhub.config.BaseRepositoryTest;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.model.Role;
import com.marcoaga02.modularhub.core.domain.model.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static com.marcoaga02.modularhub.fixture.UserFixtures.adminRole;
import static com.marcoaga02.modularhub.fixture.UserFixtures.role;
import static com.marcoaga02.modularhub.fixture.UserFixtures.user;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UserRepositoryTest extends BaseRepositoryTest {

    private static final String UNKNOWN = "unknown";
    private static final String ADMIN = "ADMIN";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private EntityManager entityManager;

    private User activeUser;

    private User deletedUser;

    @BeforeEach
    void setUp() {
        activeUser = userRepository.save(user("active", Language.IT));

        deletedUser = user("deleted", Language.IT);
        deletedUser.setDeletedOn(OffsetDateTime.now());
        userRepository.save(deletedUser);

        entityManager.flush();
        entityManager.clear();
    }

    @Nested
    class FindByUuidAndDeletedOnIsNull {

        @Test
        void findByUuidAndDeletedOnIsNull_shouldReturnUser_whenUserIsActive() {
            assertFound(userRepository.findByUuidAndDeletedOnIsNull(activeUser.getUuid()));
        }

        @Test
        void findByUuidAndDeletedOnIsNull_shouldReturnEmpty_whenUuidIsUnknown() {
            assertThat(userRepository.findByUuidAndDeletedOnIsNull(UNKNOWN)).isEmpty();
        }

        @Test
        void findByUuidAndDeletedOnIsNull_shouldReturnEmpty_whenUserIsDeleted() {
            assertThat(userRepository.findByUuidAndDeletedOnIsNull(deletedUser.getUuid())).isEmpty();
        }
    }

    @Nested
    class FindByTaxIdNumberAndDeletedOnIsNull {

        @Test
        void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnUser_whenUserIsActive() {
            assertFound(userRepository.findByTaxIdNumberAndDeletedOnIsNull(activeUser.getTaxIdNumber()));
        }

        @Test
        void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnEmpty_whenTaxIdNumberIsUnknown() {
            assertThat(userRepository.findByTaxIdNumberAndDeletedOnIsNull(UNKNOWN)).isEmpty();
        }

        @Test
        void findByTaxIdNumberAndDeletedOnIsNull_shouldReturnEmpty_whenUserIsDeleted() {
            assertThat(userRepository.findByTaxIdNumberAndDeletedOnIsNull(deletedUser.getTaxIdNumber())).isEmpty();
        }
    }

    @Nested
    class FindByUsernameAndDeletedOnIsNull {

        @Test
        void findByUsernameAndDeletedOnIsNull_shouldReturnUser_whenUserIsActive() {
            assertFound(userRepository.findByUsernameAndDeletedOnIsNull(activeUser.getUsername()));
        }

        @Test
        void findByUsernameAndDeletedOnIsNull_shouldReturnEmpty_whenUsernameIsUnknown() {
            assertThat(userRepository.findByUsernameAndDeletedOnIsNull(UNKNOWN)).isEmpty();
        }

        @Test
        void findByUsernameAndDeletedOnIsNull_shouldReturnEmpty_whenUserIsDeleted() {
            assertThat(userRepository.findByUsernameAndDeletedOnIsNull(deletedUser.getUsername())).isEmpty();
        }
    }

    @Nested
    class FindByEmailAndDeletedOnIsNull {

        @Test
        void findByEmailAndDeletedOnIsNull_shouldReturnUser_whenUserIsActive() {
            assertFound(userRepository.findByEmailAndDeletedOnIsNull(activeUser.getEmail()));
        }

        @Test
        void findByEmailAndDeletedOnIsNull_shouldReturnEmpty_whenEmailIsUnknown() {
            assertThat(userRepository.findByEmailAndDeletedOnIsNull(UNKNOWN)).isEmpty();
        }

        @Test
        void findByEmailAndDeletedOnIsNull_shouldReturnEmpty_whenUserIsDeleted() {
            assertThat(userRepository.findByEmailAndDeletedOnIsNull(deletedUser.getEmail())).isEmpty();
        }
    }

    @Nested
    class FindByIdentityId {

        @Test
        void findByIdentityId_shouldReturnUserWithPreferencesAndLanguage_whenIdentityIdExists() {
            Optional<User> result = userRepository.findByIdentityId(activeUser.getIdentityId());

            assertFound(result);
            assertThat(result.orElseThrow().getPreferences().getUuid())
                    .isEqualTo(activeUser.getPreferences().getUuid());
            assertThat(result.orElseThrow().getPreferences().getLanguage()).isEqualTo(Language.IT);
        }

        @Test
        void findByIdentityId_shouldReturnUser_whenUserIsDeleted() {
            Optional<User> result = userRepository.findByIdentityId(deletedUser.getIdentityId());

            assertThat(result).map(User::getUuid).contains(deletedUser.getUuid());
        }

        @Test
        void findByIdentityId_shouldReturnEmpty_whenIdentityIdIsUnknown() {
            assertThat(userRepository.findByIdentityId(UNKNOWN)).isEmpty();
        }
    }

    @Nested
    class Save {

        @Test
        void save_shouldThrowOptimisticLockingFailure_whenEntityVersionIsStale() {
            User stale = userRepository.findById(activeUser.getId()).orElseThrow();
            entityManager.detach(stale);

            User current = userRepository.findById(activeUser.getId()).orElseThrow();
            current.setFirstname("updated");
            userRepository.saveAndFlush(current);

            stale.setFirstname("stale");

            assertThatThrownBy(() -> userRepository.saveAndFlush(stale))
                    .isInstanceOf(ObjectOptimisticLockingFailureException.class);
        }

        @Test
        void save_shouldFillCreationAuditDate_whenUserIsCreated() {
            User saved = userRepository.findById(activeUser.getId()).orElseThrow();

            assertThat(saved.getCreatedOn()).isNotNull();
        }
    }

    @Nested
    class CountActiveUsersByRoleCode {

        private Role admin;

        @BeforeEach
        void setUpAdmins() {
            admin = roleRepository.save(adminRole());
            Role custom = roleRepository.save(role("custom"));
            saveWithRoles("active-admin", false, false, admin);
            saveWithRoles("second-admin", false, false, admin, custom);
            saveWithRoles("disabled-admin", true, false, admin);
            saveWithRoles("deleted-admin", false, true, admin);
            saveWithRoles("custom-only", false, false, custom);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull_shouldCountOnlyEnabledAndNotDeletedHolders() {
            assertThat(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(ADMIN)).isEqualTo(2);
        }

        @Test
        void countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot_shouldExcludeTheGivenUser() {
            Long activeAdminId = userRepository.findByIdentityId("identity-active-admin").orElseThrow().getId();

            assertThat(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNullAndIdNot(ADMIN, activeAdminId))
                    .isEqualTo(1);
        }

        @Test
        void countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull_shouldReturnZero_whenNoUserHasTheRole() {
            assertThat(userRepository.countByRoles_CodeAndEnabledTrueAndDeletedOnIsNull(UNKNOWN)).isZero();
        }
    }

    @Nested
    class FindActiveRolePermissionsByIdentityId {

        @BeforeEach
        void setUpRoles() {
            Role admin = roleRepository.save(adminRole());
            Role operators = roleRepository.save(role("operators", "USER_MANAGEMENT", "REPORTS"));
            Role empty = roleRepository.save(role("empty"));
            saveWithRoles("operator", false, false, operators, empty);
            saveWithRoles("admin", false, false, admin);
            saveWithRoles("disabled", true, false, operators);
            saveWithRoles("removed", false, true, operators);
            entityManager.flush();
            entityManager.clear();
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnAPermissionRowForEveryRole_whenUserIsActive() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId("identity-operator"))
                    .containsExactlyInAnyOrder(
                            new UserRolePermission(null, "USER_MANAGEMENT"),
                            new UserRolePermission(null, "REPORTS"),
                            new UserRolePermission(null, null));
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnTheRoleCode_whenUserHasASystemRole() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId("identity-admin"))
                    .containsExactly(new UserRolePermission(ADMIN, null));
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnEmpty_whenUserIsDisabled() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId("identity-disabled")).isEmpty();
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnEmpty_whenUserIsDeleted() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId("identity-removed")).isEmpty();
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnEmpty_whenUserHasNoRoles() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId(activeUser.getIdentityId())).isEmpty();
        }

        @Test
        void findActiveRolePermissionsByIdentityId_shouldReturnEmpty_whenIdentityIsUnknown() {
            assertThat(userRepository.findActiveRolePermissionsByIdentityId(UNKNOWN)).isEmpty();
        }
    }

    private void assertFound(Optional<User> result) {
        assertThat(result).map(User::getUuid).contains(activeUser.getUuid());
    }

    private void saveWithRoles(String key, boolean disabled, boolean deleted, Role... roles) {
        User user = user(key, Language.IT);
        user.setEnabled(!disabled);
        if (deleted) {
            user.setDeletedOn(OffsetDateTime.now());
        }
        user.getRoles().addAll(List.of(roles));
        userRepository.save(user);
    }
}
