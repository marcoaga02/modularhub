package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.IdentityProviderException;
import com.marcoaga02.modularhub.core.api.exception.InvalidArgumentException;
import com.marcoaga02.modularhub.core.spi.IdentityProviderService;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

        if (user.getGroupIds() != null && !user.getGroupIds().isEmpty()) {
            updateUserGroups(userId, new HashSet<>(user.getGroupIds()));
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

        Set<String> newGroups = user.getGroupIds() != null
                ? new HashSet<>(user.getGroupIds())
                : new HashSet<>();

        updateUserGroups(userId, newGroups);
    }

    @Override
    public void deleteUser(String userId) {
        try (Response response = realm().users().delete(userId)) {
            checkResponse(response);
        }
    }

    @Override
    public IdentityUserResponseDTO getUserById(String userId) {
        UserRepresentation kcUser = execute(() -> realm().users().get(userId).toRepresentation());

        final String identityId = kcUser.getId();

        IdentityUserResponseDTO user = new IdentityUserResponseDTO();
        user.setId(identityId);
        user.setUsername(kcUser.getUsername());
        user.setEmail(kcUser.getEmail());
        user.setFirstName(kcUser.getFirstName());
        user.setLastName(kcUser.getLastName());
        user.setEnabled(kcUser.isEnabled());
        user.setGroups(getUserGroups(identityId));

        return user;
    }

    @Override
    public List<IdentityGroupDTO> getGroups() {
        return execute(() -> realm().groups().groups().stream()
                .map(g -> new IdentityGroupDTO(g.getId(), g.getName(), g.getDescription()))
                .toList());
    }

    @Override
    public List<IdentityGroupDTO> getUserGroups(String userId) {
        return execute(() -> realm().users().get(userId).groups().stream()
                .map(g -> new IdentityGroupDTO(g.getId(), g.getName(), g.getDescription()))
                .toList());
    }

    @Override
    public void updatePassword(String userId, String password) {
        if (!StringUtils.hasText(password)) {
            throw new InvalidArgumentException("Password must not be blank");
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

    private void updateUserGroups(String userId, Set<String> groupIds) {
        execute(() -> {
            var userResource = realm().users().get(userId);
            Set<String> currentGroups = userResource.groups().stream()
                    .map(GroupRepresentation::getId)
                    .collect(Collectors.toSet());
            currentGroups.stream()
                    .filter(id -> !groupIds.contains(id))
                    .forEach(userResource::leaveGroup);
            groupIds.stream()
                    .filter(id -> !currentGroups.contains(id))
                    .forEach(userResource::joinGroup);
        });
    }

    private void checkResponse(Response response) {
        Response.Status.Family family = response.getStatusInfo().getFamily();

        if (family == Response.Status.Family.CLIENT_ERROR ||
                family == Response.Status.Family.SERVER_ERROR) {

            String errorMessage = response.readEntity(String.class);
            throw new IdentityProviderException(response.getStatus(), errorMessage);
        }
    }

    private <T> T execute(Supplier<T> action) {
        try {
            return action.get();
        } catch (WebApplicationException e) {
            throw new IdentityProviderException(e.getResponse().getStatus(), e.getMessage(), e);
        }
    }

    private void execute(Runnable action) {
        try {
            action.run();
        } catch (WebApplicationException e) {
            throw new IdentityProviderException(e.getResponse().getStatus(), e.getMessage(), e);
        }
    }
}