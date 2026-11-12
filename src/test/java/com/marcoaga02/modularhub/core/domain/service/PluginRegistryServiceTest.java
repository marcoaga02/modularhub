package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.core.ModulePlugin;
import com.marcoaga02.modularhub.core.api.dto.PluginDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(MockitoExtension.class)
class PluginRegistryServiceTest {

    private static final String USER_MANAGEMENT = "USER_MANAGEMENT";
    private static final String ADMIN = "ADMIN";

    @Mock
    private AccountService accountService;

    private final ModulePlugin userPlugin = plugin("users", USER_MANAGEMENT);
    private final ModulePlugin adminPlugin = plugin("admin", USER_MANAGEMENT, ADMIN);
    private final ModulePlugin publicPlugin = plugin("public");

    @Test
    void getAuthorizedPlugins_shouldExposePluginDescriptor_whenUserHasRequiredRoles() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(userPlugin));
        when(accountService.getRoles()).thenReturn(Set.of(USER_MANAGEMENT));

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result)
                .extracting(PluginDTO::getPath, PluginDTO::getName, PluginDTO::getDescription, PluginDTO::getIcon)
                .containsExactly(tuple("users", "name-users", "description-users", "icon-users"));
    }

    @Test
    void getAuthorizedPlugins_shouldExcludePlugins_whenUserLacksAnyRequiredRole() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(userPlugin, adminPlugin));
        when(accountService.getRoles()).thenReturn(Set.of(USER_MANAGEMENT));

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("users");
    }

    @Test
    void getAuthorizedPlugins_shouldReturnPluginsInRegistrationOrder_whenUserHasAllRoles() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(adminPlugin, userPlugin));
        when(accountService.getRoles()).thenReturn(Set.of(USER_MANAGEMENT, ADMIN));

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("admin", "users");
    }

    @Test
    void getAuthorizedPlugins_shouldIncludePluginsWithoutRequiredRoles_whenUserHasNoRoles() {
        PluginRegistryService service = new PluginRegistryService(accountService,
                List.of(userPlugin, publicPlugin));
        when(accountService.getRoles()).thenReturn(Set.of());

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("public");
    }

    @Test
    void getAuthorizedPlugins_shouldReadUserRolesOnce_whenManyPluginsAreRegistered() {
        PluginRegistryService service = new PluginRegistryService(accountService,
                List.of(userPlugin, adminPlugin, publicPlugin));
        when(accountService.getRoles()).thenReturn(Set.of());

        service.getAuthorizedPlugins();

        verify(accountService).getRoles();
    }

    private static ModulePlugin plugin(String path, String... requiredRoles) {
        ModulePlugin plugin = mock(ModulePlugin.class, withSettings().strictness(Strictness.LENIENT));
        when(plugin.getPath()).thenReturn(path);
        when(plugin.getName()).thenReturn("name-" + path);
        when(plugin.getDescription()).thenReturn("description-" + path);
        when(plugin.getIcon()).thenReturn("icon-" + path);
        when(plugin.getRequiredRoles()).thenReturn(List.of(requiredRoles));
        return plugin;
    }
}
