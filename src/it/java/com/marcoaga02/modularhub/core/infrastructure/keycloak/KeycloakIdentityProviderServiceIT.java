package com.marcoaga02.modularhub.core.infrastructure.keycloak;

import com.marcoaga02.modularhub.config.BaseKeycloakIT;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserUpdateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.ExceptionCodes;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityConflictException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityInvalidRequestException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityResourceNotFoundException;
import jakarta.ws.rs.NotFoundException;
import org.assertj.core.api.ThrowableAssert.ThrowingCallable;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KeycloakIdentityProviderServiceIT extends BaseKeycloakIT {

    private static final String UNKNOWN_ID = "unknown-id";
    private static final String USERNAME = "mario.rossi";
    private static final String EMAIL = "mario.rossi@test.com";

    @Autowired
    private KeycloakIdentityProviderService keycloakService;

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateUserWithRequestData() {
            String userId = keycloakService.createUser(createRequest());

            UserRepresentation created = realm().users().get(userId).toRepresentation();
            assertThat(created)
                    .extracting(UserRepresentation::getUsername, UserRepresentation::getEmail,
                            UserRepresentation::getFirstName, UserRepresentation::getLastName,
                            UserRepresentation::isEnabled, UserRepresentation::isEmailVerified)
                    .containsExactly(USERNAME, EMAIL, "Mario", "Rossi", true, true);
        }

        @Test
        void createUser_shouldThrowIdentityConflictException_whenUsernameAlreadyExists() {
            keycloakService.createUser(createRequest());
            IdentityUserCreateRequestDTO duplicate = createRequest();
            duplicate.setEmail("another@test.com");

            assertThatThrownBy(() -> keycloakService.createUser(duplicate))
                    .isInstanceOfSatisfying(IdentityConflictException.class, ex ->
                            assertThat(ex.getErrorCode()).isEqualTo(ExceptionCodes.IdentityProvider.CONFLICT));
        }
    }

    @Nested
    class UpdateUser {

        @Test
        void updateUser_shouldReplaceUserData() {
            String userId = keycloakService.createUser(createRequest());

            keycloakService.updateUser(userId, updateRequest());

            UserRepresentation updated = realm().users().get(userId).toRepresentation();
            assertThat(updated)
                    .extracting(UserRepresentation::getUsername, UserRepresentation::getEmail,
                            UserRepresentation::getFirstName, UserRepresentation::getLastName,
                            UserRepresentation::isEnabled)
                    .containsExactly("luigi.verdi", "luigi.verdi@test.com", "Luigi", "Verdi", false);
        }

        @Test
        void updateUser_shouldThrowIdentityResourceNotFoundException_whenUserDoesNotExist() {
            IdentityUserUpdateRequestDTO request = updateRequest();

            assertIdentityNotFound(() -> keycloakService.updateUser(UNKNOWN_ID, request));
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldDeleteUser_whenUserExists() {
            String userId = keycloakService.createUser(createRequest());

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
    class UpdatePassword {

        @Test
        void updatePassword_shouldStorePasswordCredential() {
            String userId = keycloakService.createUser(createRequest());

            keycloakService.updatePassword(userId, "newPassword123");

            assertThat(realm().users().get(userId).credentials())
                    .extracting(CredentialRepresentation::getType)
                    .containsExactly(CredentialRepresentation.PASSWORD);
        }

        @Test
        void updatePassword_shouldRequirePasswordChangeAtNextLogin() {
            String userId = keycloakService.createUser(createRequest());

            keycloakService.updatePassword(userId, "newPassword123");

            assertThat(realm().users().get(userId).toRepresentation().getRequiredActions())
                    .containsExactly("UPDATE_PASSWORD");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @ValueSource(strings = {" ", "\t"})
        void updatePassword_shouldThrowIllegalArgumentException_whenPasswordIsBlank(String password) {
            String userId = keycloakService.createUser(createRequest());

            assertThatThrownBy(() -> keycloakService.updatePassword(userId, password))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Password must not be blank");
        }

        @Test
        void updatePassword_shouldThrowIdentityInvalidRequestException_whenPasswordViolatesPolicy() {
            String userId = keycloakService.createUser(createRequest());
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
            String userId = keycloakService.createUser(createRequest());

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

    private static IdentityUserCreateRequestDTO createRequest() {
        return new IdentityUserCreateRequestDTO(USERNAME, EMAIL, "Mario", "Rossi", true, true, "pa55w0rd");
    }

    private static IdentityUserUpdateRequestDTO updateRequest() {
        return new IdentityUserUpdateRequestDTO("luigi.verdi", "luigi.verdi@test.com", "Luigi", "Verdi", false);
    }
}
