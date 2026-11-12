package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.config.BaseKeycloakIT;
import com.marcoaga02.modularhub.core.api.dto.IdentityGroupDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserResponseDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityConflictException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityInvalidRequestException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityResourceNotFoundException;
import jakarta.ws.rs.NotFoundException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.groups.Tuple.tuple;

class KeycloakIdentityProviderServiceIT extends BaseKeycloakIT {

    private static final String UNKNOWN_ID = "unknown-id";
    private static final String USERNAME = "mario.rossi";
    private static final String EMAIL = "mario.rossi@test.com";

    @Autowired
    private KeycloakIdentityProviderService keycloakService;

    private String firstGroupId;

    private String secondGroupId;

    @BeforeEach
    void loadGroupIds() {
        firstGroupId = groupId(FIRST_GROUP_NAME);
        secondGroupId = groupId(SECOND_GROUP_NAME);
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateUserWithRequestDataAndGroups() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId, secondGroupId)));

            UserRepresentation created = realm().users().get(userId).toRepresentation();
            assertThat(created)
                    .extracting(UserRepresentation::getUsername, UserRepresentation::getEmail,
                            UserRepresentation::getFirstName, UserRepresentation::getLastName,
                            UserRepresentation::isEnabled, UserRepresentation::isEmailVerified)
                    .containsExactly(USERNAME, EMAIL, "Mario", "Rossi", true, true);
            assertThat(groupIdsOf(userId)).containsExactlyInAnyOrder(firstGroupId, secondGroupId);
        }

        @Test
        void createUser_shouldCreateUserWithoutGroups_whenGroupIdsIsNull() {
            String userId = keycloakService.createUser(createRequest(null));

            assertThat(groupIdsOf(userId)).isEmpty();
        }

        @Test
        void createUser_shouldCreateUserWithoutGroups_whenGroupIdsIsEmpty() {
            String userId = keycloakService.createUser(createRequest(List.of()));

            assertThat(groupIdsOf(userId)).isEmpty();
        }

        @Test
        void createUser_shouldThrowIdentityConflictException_whenUsernameAlreadyExists() {
            keycloakService.createUser(createRequest(List.of()));
            IdentityUserCreateRequestDTO duplicate = createRequest(List.of());
            duplicate.setEmail("another@test.com");

            assertThatThrownBy(() -> keycloakService.createUser(duplicate))
                    .isInstanceOfSatisfying(IdentityConflictException.class, ex ->
                            assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IdentityProvider.CONFLICT));
        }

        @Test
        void createUser_shouldDeleteCreatedUser_whenGroupAssignmentFails() {
            IdentityUserCreateRequestDTO request = createRequest(List.of(firstGroupId, UNKNOWN_ID));

            assertIdentityNotFound(() -> keycloakService.createUser(request));

            assertThat(realm().users().search(USERNAME, true)).isEmpty();
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldReplaceUserDataAndGroups() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId)));

            keycloakService.updateUser(userId, updateRequest(List.of(secondGroupId)));

            UserRepresentation updated = realm().users().get(userId).toRepresentation();
            assertThat(updated)
                    .extracting(UserRepresentation::getUsername, UserRepresentation::getEmail,
                            UserRepresentation::getFirstName, UserRepresentation::getLastName,
                            UserRepresentation::isEnabled)
                    .containsExactly("luigi.verdi", "luigi.verdi@test.com", "Luigi", "Verdi", false);
            assertThat(groupIdsOf(userId)).containsExactly(secondGroupId);
        }

        @Test
        void updateUser_shouldRemoveAllGroups_whenGroupIdsIsNull() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId)));

            keycloakService.updateUser(userId, updateRequest(null));

            assertThat(groupIdsOf(userId)).isEmpty();
        }

        @Test
        void updateUser_shouldRemoveAllGroups_whenGroupIdsIsEmpty() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId)));

            keycloakService.updateUser(userId, updateRequest(List.of()));

            assertThat(groupIdsOf(userId)).isEmpty();
        }

        @Test
        void updateUser_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            IdentityUserUpdateRequestDTO request = updateRequest(List.of());

            assertIdentityNotFound(() -> keycloakService.updateUser(UNKNOWN_ID, request));
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldDeleteUser_whenUserExists() {
            String userId = keycloakService.createUser(createRequest(List.of()));

            keycloakService.deleteUser(userId);

            assertThatThrownBy(() -> realm().users().get(userId).toRepresentation())
                    .isInstanceOf(NotFoundException.class);
        }

        @Test
        void deleteUser_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            assertIdentityNotFound(() -> keycloakService.deleteUser(UNKNOWN_ID));
        }
    }

    @Nested
    class GetUserById {

        @Test
        void getUserById_shouldReturnUserWithGroups_whenUserExists() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId, secondGroupId)));

            IdentityUserResponseDTO user = keycloakService.getUserById(userId);

            assertThat(user)
                    .extracting(IdentityUserResponseDTO::getId, IdentityUserResponseDTO::getUsername,
                            IdentityUserResponseDTO::getEmail, IdentityUserResponseDTO::getFirstName,
                            IdentityUserResponseDTO::getLastName, IdentityUserResponseDTO::getEnabled)
                    .containsExactly(userId, USERNAME, EMAIL, "Mario", "Rossi", true);
            assertThat(user.getGroups())
                    .extracting(IdentityGroupDTO::getId)
                    .containsExactlyInAnyOrder(firstGroupId, secondGroupId);
        }

        @Test
        void getUserById_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            assertIdentityNotFound(() -> keycloakService.getUserById(UNKNOWN_ID));
        }
    }

    @Nested
    class GetGroups {

        @Test
        void getGroups_shouldReturnAllRealmGroupsWithDescription() {
            assertThat(keycloakService.getGroups())
                    .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                    .containsExactlyInAnyOrder(
                            tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION),
                            tuple(secondGroupId, SECOND_GROUP_NAME, SECOND_GROUP_DESCRIPTION));
        }
    }

    @Nested
    class GetUserGroups {

        @Test
        void getUserGroups_shouldReturnOnlyTheGroupsOfTheUser() {
            String userId = keycloakService.createUser(createRequest(List.of(firstGroupId)));

            assertThat(keycloakService.getUserGroups(userId))
                    .extracting(IdentityGroupDTO::getId, IdentityGroupDTO::getName, IdentityGroupDTO::getDescription)
                    .containsExactly(tuple(firstGroupId, FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION));
        }

        @Test
        void getUserGroups_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            assertIdentityNotFound(() -> keycloakService.getUserGroups(UNKNOWN_ID));
        }
    }

    @Nested
    class UpdatePassword {

        @Test
        void updatePassword_shouldStorePasswordCredential() {
            String userId = keycloakService.createUser(createRequest(List.of()));

            keycloakService.updatePassword(userId, "newPassword123");

            assertThat(realm().users().get(userId).credentials())
                    .extracting(CredentialRepresentation::getType)
                    .containsExactly(CredentialRepresentation.PASSWORD);
        }

        @Test
        void updatePassword_shouldRequirePasswordChangeAtNextLogin() {
            String userId = keycloakService.createUser(createRequest(List.of()));

            keycloakService.updatePassword(userId, "newPassword123");

            assertThat(realm().users().get(userId).toRepresentation().getRequiredActions())
                    .containsExactly("UPDATE_PASSWORD");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void updatePassword_shouldThrowIllegalArgumentException_whenPasswordIsBlank(String password) {
            String userId = keycloakService.createUser(createRequest(List.of()));

            assertThatThrownBy(() -> keycloakService.updatePassword(userId, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password must not be blank");
        }

        @Test
        void updatePassword_shouldThrowIdentityInvalidRequestException_whenPasswordViolatesPolicy() {
            String userId = keycloakService.createUser(createRequest(List.of()));
            String shortPassword = "x".repeat(MIN_PASSWORD_LENGTH - 1);

            assertThatThrownBy(() -> keycloakService.updatePassword(userId, shortPassword))
                    .isInstanceOfSatisfying(IdentityInvalidRequestException.class, ex ->
                            assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IdentityProvider.INVALID_REQUEST));
        }

        @Test
        void updatePassword_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            assertIdentityNotFound(() -> keycloakService.updatePassword(UNKNOWN_ID, "newPassword123"));
        }
    }

    @Nested
    class ResetPassword {

        @Test
        void resetPassword_shouldSendUpdatePasswordEmailToUser() throws IOException, InterruptedException {
            String userId = keycloakService.createUser(createRequest(List.of()));

            keycloakService.resetPassword(userId);

            assertThat(mailRecipients()).containsExactly(EMAIL);
        }

        @Test
        void resetPassword_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            assertIdentityNotFound(() -> keycloakService.resetPassword(UNKNOWN_ID));
        }
    }

    private static void assertIdentityNotFound(ThrowingCallable call) {
        assertThatThrownBy(call)
                .isInstanceOfSatisfying(IdentityResourceNotFoundException.class, ex ->
                        assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IdentityProvider.NOT_FOUND));
    }

    private static IdentityUserCreateRequestDTO createRequest(List<String> groupIds) {
        return new IdentityUserCreateRequestDTO(USERNAME, EMAIL, "Mario", "Rossi", true, true, "pa55w0rd", groupIds);
    }

    private static IdentityUserUpdateRequestDTO updateRequest(List<String> groupIds) {
        return new IdentityUserUpdateRequestDTO("luigi.verdi", "luigi.verdi@test.com", "Luigi", "Verdi", false,
                groupIds);
    }
}
