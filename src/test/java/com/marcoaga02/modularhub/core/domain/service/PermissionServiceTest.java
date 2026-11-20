package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRolePermission;
import com.marcoaga02.modularhub.core.util.constant.SecurityConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class PermissionServiceTest {

    private static final String IDENTITY_ID = "identity-id";
    private static final String USERS = "USER_MANAGEMENT";
    private static final String REPORTS = "REPORTS";
    private static final String AUDIT = "AUDIT";

    @Mock
    private UserRepository userRepository;

    private PermissionService permissionService;

    @BeforeEach
    void setUp() {
        permissionService = new PermissionService(
                List.of(plugin(USERS, REPORTS), plugin(REPORTS), plugin(AUDIT)), userRepository);
    }

    @Nested
    class GetAvailablePermissions {

        @Test
        void getAvailablePermissions_shouldReturnSortedDistinctPermissionsOfAllPlugins() {
            assertThat(permissionService.getAvailablePermissions()).containsExactly(AUDIT, REPORTS, USERS);
        }

        @Test
        void getAvailablePermissions_shouldReturnUnmodifiableSet() {
            Set<String> available = permissionService.getAvailablePermissions();

            assertThatThrownBy(() -> available.add("OTHER")).isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    class GetEffectivePermissions {

        @Test
        void getEffectivePermissions_shouldReturnUnionOfTheRolesPermissions_whenUserHasCustomRoles() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID))
                    .thenReturn(List.of(custom(USERS), custom(REPORTS), custom(USERS)));

            assertThat(permissionService.getEffectivePermissions(IDENTITY_ID)).containsExactly(REPORTS, USERS);
            verify(userRepository).findActiveRolePermissionsByIdentityId(IDENTITY_ID);
        }

        @Test
        void getEffectivePermissions_shouldDropPermissionsNoLongerDeclaredByPlugins() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID))
                    .thenReturn(List.of(custom(USERS), custom("REMOVED_MODULE")));

            assertThat(permissionService.getEffectivePermissions(IDENTITY_ID)).containsExactly(USERS);
        }

        @Test
        void getEffectivePermissions_shouldIgnoreRolesWithoutPermissions() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID))
                    .thenReturn(List.of(custom(null), custom(AUDIT)));

            assertThat(permissionService.getEffectivePermissions(IDENTITY_ID)).containsExactly(AUDIT);
        }

        @Test
        void getEffectivePermissions_shouldReturnEveryAvailablePermission_whenUserHasASystemRole() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID))
                    .thenReturn(List.of(custom(USERS), new UserRolePermission(SecurityConstants.ADMIN_ROLE_CODE, null)));

            assertThat(permissionService.getEffectivePermissions(IDENTITY_ID)).containsExactly(AUDIT, REPORTS, USERS);
        }

        @Test
        void getEffectivePermissions_shouldReturnEmpty_whenRepositoryReturnsNoRows() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID)).thenReturn(List.of());

            assertThat(permissionService.getEffectivePermissions(IDENTITY_ID)).isEmpty();
        }

        @Test
        void getEffectivePermissions_shouldReturnUnmodifiableSet() {
            when(userRepository.findActiveRolePermissionsByIdentityId(IDENTITY_ID)).thenReturn(List.of(custom(USERS)));

            Set<String> permissions = permissionService.getEffectivePermissions(IDENTITY_ID);

            assertThatThrownBy(() -> permissions.add("OTHER")).isInstanceOf(UnsupportedOperationException.class);
        }

        private static UserRolePermission custom(String permission) {
            return new UserRolePermission(null, permission);
        }
    }

    private static ModulePlugin plugin(String... permissions) {
        ModulePlugin plugin = mock(ModulePlugin.class, withSettings().strictness(Strictness.LENIENT));
        when(plugin.getPermissions()).thenReturn(List.of(permissions));
        return plugin;
    }
}
