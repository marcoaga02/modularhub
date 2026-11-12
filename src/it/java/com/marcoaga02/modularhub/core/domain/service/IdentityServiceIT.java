package com.marcoaga02.modularhub.core.domain.service;

import com.marcoaga02.modularhub.config.BaseKeycloakIT;
import com.marcoaga02.modularhub.core.api.dto.IdentityUserCreateRequestDTO;
import com.marcoaga02.modularhub.core.api.exception.account.SelfDeletionException;
import com.marcoaga02.modularhub.core.api.exception.identity.IdentityInvalidRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static com.marcoaga02.modularhub.fixture.SecurityFixtures.authenticate;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class IdentityServiceIT extends BaseKeycloakIT {

    private static final String USERNAME = "mario.rossi";

    @Autowired
    private IdentityService identityService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Nested
    class CreateUser {

        @Test
        void createUser_shouldCreateUserWithPassword_whenPasswordIsValid() {
            String userId = identityService.createUser(createRequest("pa55w0rd"));

            assertThat(realm().users().get(userId).credentials())
                    .extracting(CredentialRepresentation::getType)
                    .containsExactly(CredentialRepresentation.PASSWORD);
        }

        @Test
        void createUser_shouldCreateUserWithoutPassword_whenPasswordIsMissing() {
            String userId = identityService.createUser(createRequest(null));

            assertThat(realm().users().get(userId).credentials()).isEmpty();
        }

        @Test
        void createUser_shouldDeleteCreatedUser_whenPasswordIsRejectedByPolicy() {
            IdentityUserCreateRequestDTO request = createRequest("x".repeat(MIN_PASSWORD_LENGTH - 1));

            assertThatThrownBy(() -> identityService.createUser(request))
                    .isInstanceOf(IdentityInvalidRequestException.class);

            assertThat(realm().users().search(USERNAME, true)).isEmpty();
        }
    }

    @Nested
    class DeleteUser {

        @Test
        void deleteUser_shouldDeleteUser_whenUserIsNotTheCurrentOne() {
            String userId = identityService.createUser(createRequest(null));
            authenticate("another-identity");

            identityService.deleteUser(userId);

            assertThat(realm().users().search(USERNAME, true)).isEmpty();
        }

        @Test
        void deleteUser_shouldKeepUser_whenUserIsTheCurrentOne() {
            String userId = identityService.createUser(createRequest(null));
            authenticate(userId);

            assertThatThrownBy(() -> identityService.deleteUser(userId))
                    .isInstanceOf(SelfDeletionException.class);

            assertThat(realm().users().search(USERNAME, true)).hasSize(1);
        }
    }

    private static IdentityUserCreateRequestDTO createRequest(String password) {
        return new IdentityUserCreateRequestDTO(USERNAME, "mario.rossi@test.com", "Mario", "Rossi", true, true,
                password, List.of());
    }
}
