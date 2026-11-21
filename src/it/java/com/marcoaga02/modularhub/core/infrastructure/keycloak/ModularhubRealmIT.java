package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.config.BaseKeycloakIT;
import com.marcoaga02.modularhub.core.ModulePlugin;
import org.junit.jupiter.api.Test;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ModularhubRealmIT extends BaseKeycloakIT {

    private static final String FRONTEND_CLIENT = "modularhub-frontend";
    private static final String BACKEND_CLIENT = "modularhub-backend";
    private static final String OFFLINE_ACCESS = "offline_access";
    private static final String DEV_USER = "admin";

    @Autowired
    private List<ModulePlugin> plugins;

    @Test
    void realm_shouldNotDefineRolesForPluginPermissions_becauseTheyAreManagedByTheApplication() {
        List<String> realmRoles = modularhubRealm().roles().list().stream()
                .map(RoleRepresentation::getName)
                .toList();

        assertThat(realmRoles).doesNotContainAnyElementsOf(pluginPermissions());
    }

    @Test
    void realm_shouldNotDefineGroups_becauseRolesAreManagedByTheApplication() {
        assertThat(modularhubRealm().groups().groups()).isEmpty();
    }

    @Test
    void devUser_shouldBeAllowedToRequestOfflineTokens() {
        UserRepresentation devUser = modularhubRealm().users().searchByUsername(DEV_USER, true).getFirst();

        List<String> effectiveRoles = modularhubRealm().users().get(devUser.getId())
                .roles().realmLevel().listEffective().stream()
                .map(RoleRepresentation::getName)
                .toList();

        assertThat(effectiveRoles).contains(OFFLINE_ACCESS);
    }

    @Test
    void frontendClient_shouldBePublicAndRedirectToApp() {
        ClientRepresentation frontend = modularhubRealm().clients().findByClientId(FRONTEND_CLIENT).getFirst();

        assertThat(frontend.isPublicClient()).isTrue();
        assertThat(frontend.getRedirectUris()).containsExactly(APP_URL + "/*");
        assertThat(frontend.getWebOrigins()).containsExactly(APP_URL);
    }

    @Test
    void backendClient_shouldManageUsers_whenAuthenticatedWithSecretFromEnvironment() {
        try (Keycloak backend = KeycloakBuilder.builder()
                .serverUrl(keycloakUrl())
                .realm(MODULARHUB_REALM)
                .grantType(OAuth2Constants.CLIENT_CREDENTIALS)
                .clientId(BACKEND_CLIENT)
                .clientSecret(BACKEND_CLIENT_SECRET)
                .build()) {

            List<UserRepresentation> users = backend.realm(MODULARHUB_REALM).users().searchByUsername(DEV_USER, true);

            assertThat(users).extracting(UserRepresentation::getUsername).containsExactly(DEV_USER);
        }
    }

    @Test
    void smtpServer_shouldUseValuesFromEnvironment() {
        Map<String, String> smtp = modularhubRealm().toRepresentation().getSmtpServer();

        assertThat(smtp)
                .containsEntry("host", MAILPIT_HOST)
                .containsEntry("port", String.valueOf(MAILPIT_SMTP_PORT));
    }

    private RealmResource modularhubRealm() {
        return adminClient.realm(MODULARHUB_REALM);
    }

    private List<String> pluginPermissions() {
        return plugins.stream()
                .flatMap(plugin -> plugin.getPermissions().stream())
                .toList();
    }
}
