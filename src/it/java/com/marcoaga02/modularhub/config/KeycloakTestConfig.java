package com.marcoaga02.modularhub.config;

import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test & keycloak")
public class KeycloakTestConfig {

    @Bean
    public Keycloak keycloak(
            @Value("${keycloak.server-url}") String serverUrl,
            @Value("${keycloak.admin-username}") String username,
            @Value("${keycloak.admin-password}") String password
    ) {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("master")
                .clientId("admin-cli")
                .username(username)
                .password(password)
                .grantType(OAuth2Constants.PASSWORD)
                .build();
    }
}
