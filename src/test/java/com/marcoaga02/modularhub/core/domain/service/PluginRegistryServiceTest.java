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
    private static final String REPORTS = "REPORTS";

    @Mock
    private AccountService accountService;

    private final ModulePlugin userPlugin = plugin("users", USER_MANAGEMENT);
    private final ModulePlugin reportsPlugin = plugin("reports", REPORTS);
    private final ModulePlugin publicPlugin = plugin("public");

    @Test
    void getAuthorizedPlugins_shouldExposePluginDescriptor_whenUserHasThePluginPermission() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(userPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of(USER_MANAGEMENT));

        List<PluginDTO> result = service.getAuthorizedPlugins();

        assertThat(result)
                .extracting(PluginDTO::getPath, PluginDTO::getName, PluginDTO::getDescription, PluginDTO::getIcon)
                .containsExactly(tuple("users", "name-users", "description-users", "icon-users"));
    }

    @Test
    void getAuthorizedPlugins_shouldExcludePlugins_whenUserHasNoneOfTheirPermissions() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(userPlugin, reportsPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of(USER_MANAGEMENT));

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("users");
    }

    @Test
    void getAuthorizedPlugins_shouldReturnPluginsInRegistrationOrder_whenUserHasAllPermissions() {
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(reportsPlugin, userPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of(USER_MANAGEMENT, REPORTS));

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("reports", "users");
    }

    @Test
    void getAuthorizedPlugins_shouldIncludePlugin_whenUserHasAtLeastOneOfItsPermissions() {
        ModulePlugin mixedPlugin = plugin("mixed", USER_MANAGEMENT, REPORTS);
        PluginRegistryService service = new PluginRegistryService(accountService, List.of(mixedPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of(REPORTS));

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("mixed");
    }

    @Test
    void getAuthorizedPlugins_shouldIncludePluginsWithoutPermissions_whenUserHasNoPermissions() {
        PluginRegistryService service = new PluginRegistryService(accountService,
                List.of(userPlugin, publicPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of());

        assertThat(service.getAuthorizedPlugins())
                .extracting(PluginDTO::getPath)
                .containsExactly("public");
    }

    @Test
    void getAuthorizedPlugins_shouldReadUserPermissionsOnce_whenManyPluginsAreRegistered() {
        PluginRegistryService service = new PluginRegistryService(accountService,
                List.of(userPlugin, reportsPlugin, publicPlugin));
        when(accountService.getPermissions()).thenReturn(Set.of());

        service.getAuthorizedPlugins();

        verify(accountService).getPermissions();
    }

    private static ModulePlugin plugin(String path, String... permissions) {
        ModulePlugin plugin = mock(ModulePlugin.class, withSettings().strictness(Strictness.LENIENT));
        when(plugin.getPath()).thenReturn(path);
        when(plugin.getName()).thenReturn("name-" + path);
        when(plugin.getDescription()).thenReturn("description-" + path);
        when(plugin.getIcon()).thenReturn("icon-" + path);
        when(plugin.getPermissions()).thenReturn(List.of(permissions));
        return plugin;
    }
}
