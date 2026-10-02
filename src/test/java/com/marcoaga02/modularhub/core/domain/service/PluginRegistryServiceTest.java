package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PluginRegistryServiceTest {

    @Mock
    private AccountService accountService;

    private PluginRegistryService service;

    @BeforeEach
    void setUp() {
        service = new PluginRegistryService(
                accountService,
                List.of(
                        new StubPlugin("user-management", "User Management", "Manage users", "users", List.of("USER_MANAGEMENT")),
                        new StubPlugin("admin", "Admin", "Admin panel", "shield", List.of("USER_MANAGEMENT", "ADMIN"))
                )
        );
    }

    @Test
    void getAuthorizedPlugins_shouldReturnOnlyPluginsMatchingUserRoles() {
        when(accountService.getRoles()).thenReturn(Set.of("USER_MANAGEMENT"));

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result)
                .extracting(PluginDTO::getPath)
                .containsExactly("user-management");
    }

    @Test
    void getAuthorizedPlugins_shouldReturnAllPlugins_whenUserHasAllRoles() {
        when(accountService.getRoles()).thenReturn(Set.of("USER_MANAGEMENT", "ADMIN"));

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result)
                .extracting(PluginDTO::getPath)
                .containsExactlyInAnyOrder("user-management", "admin");
    }

    @Test
    void getAuthorizedPlugins_shouldReturnEmpty_whenUserHasNoRoles() {
        when(accountService.getRoles()).thenReturn(Set.of());

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result).isEmpty();
    }

    @Test
    void getAuthorizedPlugins_shouldReturnEmpty_whenUserRolesDoNotMatch() {
        when(accountService.getRoles()).thenReturn(Set.of("SOME_OTHER_ROLE"));

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result).isEmpty();
    }

    private record StubPlugin(
            String path,
            String name,
            String description,
            String icon,
            List<String> requiredRoles
    ) implements ModulePlugin {

        @Override
        public String getPath() {
            return path;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getDescription() {
            return description;
        }

        @Override
        public String getIcon() {
            return icon;
        }

        @Override
        public List<String> getRequiredRoles() {
            return requiredRoles;
        }
    }
}