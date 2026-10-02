package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.config.BaseIT;
import com.marcoaga02.modularhub.config.TestContainersImages;
import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.IdentityProviderException;
import com.marcoaga02.modularhub.core.api.exception.InvalidArgumentException;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import jakarta.ws.rs.WebApplicationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.groups.Tuple.tuple;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Testcontainers
@ActiveProfiles("keycloak")
class KeycloakIdentityProviderServiceIT extends BaseIT {

    private static final String REALM = "test-realm";

    private static final String FIRST_GROUP_NAME = "test-group-first-name";
    private static final String FIRST_GROUP_DESCRIPTION = "test-group-first-desc";

    private static final String SECOND_GROUP_NAME = "test-group-second-name";
    private static final String SECOND_GROUP_DESCRIPTION = "test-group-second-desc";

    @Container
    private static final KeycloakContainer keycloak =
            new KeycloakContainer(TestContainersImages.KEYCLOAK)
                    .withAdminUsername("admin")
                    .withAdminPassword("admin");

    @DynamicPropertySource
    private static void properties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.server-url", keycloak::getAuthServerUrl);
        registry.add("keycloak.realm", () -> REALM);
        registry.add("keycloak.admin-username", () -> "admin");
        registry.add("keycloak.admin-password", () -> "admin");
        registry.add("keycloak.frontend-client-id", () -> "test-frontend-client");
        registry.add("keycloak.app-url", () -> "http://localhost:4200");
    }

    @Autowired
    private KeycloakIdentityProviderService keycloakService;

    private Keycloak adminClient;
    private String firstGroupId;
    private String secondGroupId;

    @BeforeAll
    static void setupRealm() {
        try (Keycloak adminClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build()) {

            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(REALM);
            realm.setEnabled(true);
            realm.setEditUsernameAllowed(true);
            adminClient.realms().create(realm);

            GroupRepresentation firstGroup = new GroupRepresentation();
            firstGroup.setName(FIRST_GROUP_NAME);
            firstGroup.setDescription(FIRST_GROUP_DESCRIPTION);

            GroupRepresentation secondGroup = new GroupRepresentation();
            secondGroup.setName(SECOND_GROUP_NAME);
            secondGroup.setDescription(SECOND_GROUP_DESCRIPTION);

            adminClient.realm(REALM).groups().add(firstGroup);
            adminClient.realm(REALM).groups().add(secondGroup);
        }
    }

    @BeforeEach
    void setupClient() {
        adminClient = KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();

        List<GroupRepresentation> groups = adminClient.realm(REALM).groups().groups();

        firstGroupId = groups.stream()
                .filter(g -> g.getName().equals(FIRST_GROUP_NAME))
                .map(GroupRepresentation::getId)
                .findFirst()
                .orElseThrow();

        secondGroupId = groups.stream()
                .filter(g -> g.getName().equals(SECOND_GROUP_NAME))
                .map(GroupRepresentation::getId)
                .findFirst()
                .orElseThrow();
    }

    @AfterEach
    void cleanup() {
        adminClient.realm(REALM).users().list()
                .forEach(u -> adminClient.realm(REALM).users().delete(u.getId()));
        adminClient.close();
    }

    @Test
    void createUser_shouldReturnId_whenValidRequest() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId, secondGroupId)
        );

        String userId = keycloakService.createUser(dto);

        assertNotNull(userId);

        UserRepresentation created = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(created)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("mario.rossi");
                    assertThat(u.getEmail()).isEqualTo("mario.rossi@example.com");
                    assertThat(u.getFirstName()).isEqualTo("Mario");
                    assertThat(u.getLastName()).isEqualTo("Rossi");
                    assertThat(u.isEnabled()).isTrue();
                    assertThat(u.isEmailVerified()).isTrue();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups)
                .hasSize(2)
                .contains(firstGroupId, secondGroupId);
    }

    @Test
    void createUser_shouldReturnId_whenGroupIdsIsNull() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                null
        );

        String userId = keycloakService.createUser(dto);

        assertNotNull(userId);

        UserRepresentation created = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(created)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("mario.rossi");
                    assertThat(u.getEmail()).isEqualTo("mario.rossi@example.com");
                    assertThat(u.getFirstName()).isEqualTo("Mario");
                    assertThat(u.getLastName()).isEqualTo("Rossi");
                    assertThat(u.isEnabled()).isTrue();
                    assertThat(u.isEmailVerified()).isTrue();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups).isEmpty();
    }

    @Test
    void createUser_shouldReturnId_whenGroupIdsIsEmpty() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of()
        );

        String userId = keycloakService.createUser(dto);

        assertNotNull(userId);

        UserRepresentation created = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(created)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("mario.rossi");
                    assertThat(u.getEmail()).isEqualTo("mario.rossi@example.com");
                    assertThat(u.getFirstName()).isEqualTo("Mario");
                    assertThat(u.getLastName()).isEqualTo("Rossi");
                    assertThat(u.isEnabled()).isTrue();
                    assertThat(u.isEmailVerified()).isTrue();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups).isEmpty();
    }

    @Test
    void createUser_shouldThrowIdentityProviderException_whenUsernameAlreadyExists() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "duplicate",
                "first@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "firstPa55w0rd",
                List.of(firstGroupId)
        );
        keycloakService.createUser(dto);

        IdentityUserCreateRequestDTO duplicate = getCreateRequestDTO(
                "duplicate",
                "second@example.com",
                "Luigi",
                "Verdi",
                false,
                false,
                "secondPa55w0rd",
                List.of(secondGroupId)
        );

        assertThatThrownBy(() -> keycloakService.createUser(duplicate))
                .isInstanceOf(IdentityProviderException.class)
                .satisfies(e -> {
                    IdentityProviderException ex = (IdentityProviderException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.CONFLICT);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IDENTITY_PROVIDER_ERROR);
                    assertThat(ex.getLogMessage()).isNotBlank();
                });
    }

    @Test
    void updateUser_shouldUpdateUser_whenValidRequest() {
        IdentityUserCreateRequestDTO createDto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(secondGroupId)
        );

        String userId = keycloakService.createUser(createDto);

        assertNotNull(userId);

        IdentityUserUpdateRequestDTO updateDto = getUpdateRequestDTO(
                "updated.username",
                "updated@example.com",
                "updatedFirstName",
                "updatedLastName",
                false,
                List.of(secondGroupId)
        );

        keycloakService.updateUser(userId, updateDto);

        UserRepresentation updated = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(updated)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("updated.username");
                    assertThat(u.getEmail()).isEqualTo("updated@example.com");
                    assertThat(u.getFirstName()).isEqualTo("updatedFirstName");
                    assertThat(u.getLastName()).isEqualTo("updatedLastName");
                    assertThat(u.isEnabled()).isFalse();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups).containsExactly(secondGroupId);
    }

    @Test
    void updateUser_shouldUpdateUser_whenGroupIdsIsNull() {
        IdentityUserCreateRequestDTO createDto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(secondGroupId)
        );

        String userId = keycloakService.createUser(createDto);

        assertNotNull(userId);

        IdentityUserUpdateRequestDTO updateDto = getUpdateRequestDTO(
                "updated.username",
                "updated@example.com",
                "updatedFirstName",
                "updatedLastName",
                false,
                null
        );

        keycloakService.updateUser(userId, updateDto);

        UserRepresentation updated = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(updated)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("updated.username");
                    assertThat(u.getEmail()).isEqualTo("updated@example.com");
                    assertThat(u.getFirstName()).isEqualTo("updatedFirstName");
                    assertThat(u.getLastName()).isEqualTo("updatedLastName");
                    assertThat(u.isEnabled()).isFalse();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups).isEmpty();
    }

    @Test
    void updateUser_shouldUpdateUser_whenGroupIdsIsEmpty() {
        IdentityUserCreateRequestDTO createDto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(secondGroupId)
        );

        String userId = keycloakService.createUser(createDto);

        assertNotNull(userId);

        IdentityUserUpdateRequestDTO updateDto = getUpdateRequestDTO(
                "updated.username",
                "updated@example.com",
                "updatedFirstName",
                "updatedLastName",
                false,
                List.of()
        );

        keycloakService.updateUser(userId, updateDto);

        UserRepresentation updated = adminClient.realm(REALM).users().get(userId).toRepresentation();
        assertThat(updated)
                .satisfies(u -> {
                    assertThat(u.getUsername()).isEqualTo("updated.username");
                    assertThat(u.getEmail()).isEqualTo("updated@example.com");
                    assertThat(u.getFirstName()).isEqualTo("updatedFirstName");
                    assertThat(u.getLastName()).isEqualTo("updatedLastName");
                    assertThat(u.isEnabled()).isFalse();
                });

        List<String> groups = adminClient.realm(REALM).users().get(userId).groups()
                .stream()
                .map(GroupRepresentation::getId)
                .toList();

        assertThat(groups).isEmpty();
    }

    @Test
    void updateUser_shouldThrowIdentityProviderException_whenInvalidUserId() {
        IdentityUserUpdateRequestDTO updateDto = getUpdateRequestDTO(
                "updated.username",
                "updated@example.com",
                "updatedFirstName",
                "updatedLastName",
                false,
                List.of(secondGroupId)
        );

        assertThatThrownBy(() -> keycloakService.updateUser("invalid-uuid", updateDto))
                .isInstanceOf(IdentityProviderException.class)
                .satisfies(e -> {
                    IdentityProviderException ex = (IdentityProviderException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IDENTITY_PROVIDER_ERROR);
                    assertThat(ex.getLogMessage()).isNotBlank();
                });
    }

    @Test
    void deleteUser_shouldDeleteUser_whenValidRequest() {
        IdentityUserCreateRequestDTO createDto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(secondGroupId)
        );

        String userId = keycloakService.createUser(createDto);
        assertNotNull(userId);

        keycloakService.deleteUser(userId);

        // if the user has been deleted, this method call throw a WebApplicationException with http status NOT FOUND (404)
        assertThatThrownBy(() -> adminClient.realm(REALM).users().get(userId).toRepresentation())
                .isInstanceOf(WebApplicationException.class)
                .extracting(e -> ((WebApplicationException) e).getResponse().getStatus())
                .isEqualTo(404);
    }

    @Test
    void deleteUser_shouldThrowIdentityProviderException_whenInvalidUserId() {
        assertThatThrownBy(() -> keycloakService.deleteUser("invalid-uuid"))
                .isInstanceOf(IdentityProviderException.class)
                .satisfies(e -> {
                    IdentityProviderException ex = (IdentityProviderException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IDENTITY_PROVIDER_ERROR);
                    assertThat(ex.getLogMessage()).isNotBlank();
                });
    }

    @Test
    void getUserById_shouldReturnUser_whenValidRequest() {
        IdentityUserCreateRequestDTO createDto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId, secondGroupId)
        );

        String userId = keycloakService.createUser(createDto);
        assertNotNull(userId);

        IdentityUserResponseDTO user = keycloakService.getUserById(userId);
        assertThat(user)
                .satisfies(u -> {
                    assertThat(u.getId()).isEqualTo(userId);
                    assertThat(u.getUsername()).isEqualTo("mario.rossi");
                    assertThat(u.getEmail()).isEqualTo("mario.rossi@example.com");
                    assertThat(u.getFirstName()).isEqualTo("Mario");
                    assertThat(u.getLastName()).isEqualTo("Rossi");
                    assertThat(u.getEnabled()).isTrue();
                    assertThat(u.getGroups())
                            .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                            .containsExactlyInAnyOrder(
                                    tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION),
                                    tuple(secondGroupId, SECOND_GROUP_NAME, SECOND_GROUP_DESCRIPTION)
                            );
                });
    }

    @Test
    void getUserById_shouldThrowIdentityProviderException_whenInvalidUserId() {
        assertThatThrownBy(() -> keycloakService.getUserById("invalid-uuid"))
                .isInstanceOf(IdentityProviderException.class)
                .satisfies(e -> {
                    IdentityProviderException ex = (IdentityProviderException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IDENTITY_PROVIDER_ERROR);
                    assertThat(ex.getCause()).isInstanceOf(WebApplicationException.class);
                });
    }

    @Test
    void getGroups_shouldReturnGroups_whenValidRequest() {
        assertThat(keycloakService.getGroups())
                .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                .containsExactlyInAnyOrder(
                        tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION),
                        tuple(secondGroupId, SECOND_GROUP_NAME, SECOND_GROUP_DESCRIPTION)
                );
    }

    @Test
    void getUserGroups_shouldReturnGroups_whenValidRequestWithSingleGroup() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId)
        );

        String userId = keycloakService.createUser(dto);
        assertNotNull(userId);

        assertThat(keycloakService.getUserGroups(userId))
                .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                .containsExactly(tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION));
    }

    @Test
    void getUserGroups_shouldReturnGroups_whenValidRequestWithMultipleGroups() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId, secondGroupId)
        );

        String userId = keycloakService.createUser(dto);
        assertNotNull(userId);

        assertThat(keycloakService.getUserGroups(userId))
                .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                .containsExactlyInAnyOrder(
                        tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION),
                        tuple(secondGroupId, SECOND_GROUP_NAME, SECOND_GROUP_DESCRIPTION)
                );
    }

    @Test
    void getUserGroups_shouldThrowIdentityProviderException_whenInvalidUserId() {
        assertThatThrownBy(() -> keycloakService.getUserGroups("invalid-uuid"))
                .isInstanceOf(IdentityProviderException.class)
                .satisfies(e -> {
                    IdentityProviderException ex = (IdentityProviderException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.NOT_FOUND);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IDENTITY_PROVIDER_ERROR);
                    assertThat(ex.getCause()).isInstanceOf(WebApplicationException.class);
                });
    }

    @Test
    void updatePassword_shouldNotThrow_whenValidRequest() {
        IdentityUserCreateRequestDTO dto = getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId, secondGroupId)
        );

        String userId = keycloakService.createUser(dto);
        assertNotNull(userId);

        assertThatNoException()
                .isThrownBy(() -> keycloakService.updatePassword(userId, "newPassword123"));
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\t", "\t "})
    void updatePassword_shouldThrowIllegalArgumentException_whenPasswordIsBlank(String password) {
        String userId = keycloakService.createUser(getCreateRequestDTO(
                "mario.rossi",
                "mario.rossi@example.com",
                "Mario",
                "Rossi",
                true,
                true,
                "pa55w0rd",
                List.of(firstGroupId, secondGroupId)
        ));

        assertThatThrownBy(() -> keycloakService.updatePassword(userId, password))
                .isInstanceOf(InvalidArgumentException.class)
                .satisfies(e -> {
                    InvalidArgumentException ex = (InvalidArgumentException) e;
                    assertThat(ex.getHttpStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
                    assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.INTERNAL_ERROR);
                    assertThat(ex.getLogMessage()).isEqualTo("Password must not be blank");
                });
    }

    private IdentityUserCreateRequestDTO getCreateRequestDTO(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            Boolean emailVerified,
            String password,
            List<String> groupIds
    ) {
        IdentityUserCreateRequestDTO dto = new IdentityUserCreateRequestDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEnabled(enabled);
        dto.setEmailVerified(emailVerified);
        dto.setPassword(password);
        dto.setGroupIds(groupIds);

        return dto;
    }

    private IdentityUserUpdateRequestDTO getUpdateRequestDTO(
            String username,
            String email,
            String firstName,
            String lastName,
            Boolean enabled,
            List<String> groupIds
    ) {
        IdentityUserUpdateRequestDTO dto = new IdentityUserUpdateRequestDTO();
        dto.setUsername(username);
        dto.setEmail(email);
        dto.setFirstName(firstName);
        dto.setLastName(lastName);
        dto.setEnabled(enabled);
        dto.setGroupIds(groupIds);

        return dto;
    }

}