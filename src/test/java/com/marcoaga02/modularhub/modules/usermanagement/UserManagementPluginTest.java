package com.marcoaga02.modularhub.modules.usermanagement;

import com.marcoaga02.modularhub.modules.usermanagement.util.constant.UserManagementPermissions;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserManagementPluginTest {

    private final UserManagementPlugin plugin = new UserManagementPlugin();

    @Test
    void getPath_shouldReturnTheFrontendRoutePath() {
        assertThat(plugin.getPath()).isEqualTo("user-management");
    }

    @Test
    void getName_shouldReturnTheModuleName() {
        assertThat(plugin.getName()).isEqualTo("user-management");
    }

    @Test
    void getDescription_shouldReturnTheI18nKey() {
        assertThat(plugin.getDescription()).isEqualTo("modules.user-management.description");
    }

    @Test
    void getIcon_shouldReturnThePrimeIconsClass() {
        assertThat(plugin.getIcon()).isEqualTo("pi pi-user");
    }

    @Test
    void getPermissions_shouldDeclareOnlyTheUserManagementPermission() {
        assertThat(plugin.getPermissions()).containsExactly(UserManagementPermissions.USER_MANAGEMENT);
    }
}
