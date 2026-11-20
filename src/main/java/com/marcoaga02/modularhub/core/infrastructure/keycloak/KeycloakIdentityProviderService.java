package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ApplicationException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityProviderUnavailableException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityConflictException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityInvalidRequestException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityResourceNotFoundException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.function.Supplier;

@Service
@Profile("keycloak")
public class KeycloakIdentityProviderService implements IdentityProviderService {

    private final Keycloak adminClient;
    private final String realm;
    private final String frontendClientId;
    private final String appUrl;

    public KeycloakIdentityProviderService(
            Keycloak adminClient,
            @Value("${keycloak.realm}") String realm,
            @Value("${keycloak.frontend-client-id}") String frontendClientId,
            @Value("${keycloak.app-url}") String appUrl){
        this.adminClient = adminClient;
        this.realm = realm;
        this.frontendClientId = frontendClientId;
        this.appUrl = appUrl;
    }

    private RealmResource realm() {
        return adminClient.realm(realm);
    }

    @Override
    public String createUser(IdentityUserCreateRequestDTO user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEnabled(user.getEnabled());
        userRepresentation.setEmailVerified(Boolean.TRUE.equals(user.getEmailVerified()));

        String userId;
        try (Response response = realm().users().create(userRepresentation)) {
            checkResponse(response);
            userId = CreatedResponseUtil.getCreatedId(response);
        }

        return userId;
    }

    @Override
    public void updateUser(String userId, IdentityUserUpdateRequestDTO user) {
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername(user.getUsername());
        userRepresentation.setEmail(user.getEmail());
        userRepresentation.setFirstName(user.getFirstName());
        userRepresentation.setLastName(user.getLastName());
        userRepresentation.setEnabled(user.getEnabled());

        execute(() -> realm().users().get(userId).update(userRepresentation));
    }

    @Override
    public void deleteUser(String userId) {
        try (Response response = realm().users().delete(userId)) {
            checkResponse(response);
        }
    }

    @Override
    public void updatePassword(String userId, String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Password must not be blank");
        }

        CredentialRepresentation credential =
                new CredentialRepresentation();
        credential.setTemporary(true);
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(password);

        execute(() -> realm().users().get(userId).resetPassword(credential));
    }

    @Override
    public void resetPassword(String userId) {
        execute(() -> realm().users().get(userId)
                .executeActionsEmail(
                        frontendClientId,
                        appUrl,
                        List.of("UPDATE_PASSWORD")
                ));
    }

    private void checkResponse(Response response) {
        Response.Status.Family family = response.getStatusInfo().getFamily();

        if (family == Response.Status.Family.CLIENT_ERROR ||
                family == Response.Status.Family.SERVER_ERROR) {

            String errorMessage = response.readEntity(String.class);
            throw toApplicationException(response.getStatus(), errorMessage, null);
        }
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebApplicationException e) {
            throw toApplicationException(e.getResponse().getStatus(), e.getMessage(), e);
        } catch (ProcessingException e) {
            throw new IdentityProviderUnavailableException(e.getMessage(), e);
        }
    }

    private void execute(Runnable action) {
        execute(() -> {
            action.run();
            return null;
        });
    }

    private ApplicationException toApplicationException(int status, String message, Throwable cause) {
        return switch (status) {
            case 400 -> new IdentityInvalidRequestException(message, cause);
            case 404 -> new IdentityResourceNotFoundException(message, cause);
            case 409 -> new IdentityConflictException(message, cause);
            default -> new IdentityProviderUnavailableException(message, cause);
        };
    }
}
