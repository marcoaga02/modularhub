package com.marcoaga02.modularhub.modules.usermanagement.domain.service;

import com.marcoaga02.modularhub.config.BaseIT;
import com.marcoaga02.modularhub.config.TestContainersImages;
import com.marcoaga02.modularhub.core.domain.model.Gender;
import com.marcoaga02.modularhub.core.domain.model.Language;
import com.marcoaga02.modularhub.core.domain.repository.LanguageRepository;
import com.marcoaga02.modularhub.core.domain.repository.UserRepository;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserRequestDTO;
import com.marcoaga02.modularhub.modules.usermanagement.api.dto.UserResponseDTO;
import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.RealmRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@ActiveProfiles("keycloak")
class UserServiceRollbackIT extends BaseIT {

    private static final String REALM = "rollback-realm";

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
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private LanguageRepository languageRepository;

    private Keycloak adminClient;

    private Language language;

    @BeforeAll
    static void setupRealm() {
        try (Keycloak client = buildAdminClient()) {
            RealmRepresentation realm = new RealmRepresentation();
            realm.setRealm(REALM);
            realm.setEnabled(true);
            client.realms().create(realm);
        }
    }

    @BeforeEach
    void setUp() {
        adminClient = buildAdminClient();

        language = new Language();
        language.setCode("it-IT");
        language.setLabel("Italiano");
        language.setIsDefault(true);
        languageRepository.save(language);
    }

    @AfterEach
    void cleanup() {
        adminClient.realm(REALM).users().list()
                .forEach(u -> adminClient.realm(REALM).users().delete(u.getId()));
        adminClient.close();

        userRepository.deleteAll();
        languageRepository.deleteAll();
    }

    @Test
    void createUser_shouldKeepIdentityUser_whenTransactionCommits() {
        UserResponseDTO result = userService.createUser(buildUserRequestDTO("committed.user", "TAX_ID"));

        assertThat(userRepository.findByUuidAndDeletedOnIsNull(result.getId())).isPresent();

        List<UserRepresentation> identityUsers = adminClient.realm(REALM).users().search("committed.user", true);
        assertThat(identityUsers).hasSize(1);
    }

    @Test
    void createUser_shouldDeleteIdentityUser_whenDatabaseSaveFails() {
        UserRequestDTO dto = buildUserRequestDTO("rolledback.user", "X".repeat(300));

        assertThatThrownBy(() -> userService.createUser(dto))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(userRepository.findAll()).isEmpty();
        assertThat(adminClient.realm(REALM).users().search("rolledback.user", true)).isEmpty();
    }

    private UserRequestDTO buildUserRequestDTO(String username, String taxIdNumber) {
        UserRequestDTO dto = new UserRequestDTO();
        dto.setFirstname("Mario");
        dto.setLastname("Rossi");
        dto.setGender(Gender.M);
        dto.setLanguageId(language.getUuid());
        dto.setMobileNumber("0000");
        dto.setTaxIdNumber(taxIdNumber);
        dto.setEmail(username + "@example.com");
        dto.setUsername(username);
        dto.setPassword("pa55w0rd");
        dto.setEnabled(true);
        return dto;
    }

    private static Keycloak buildAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(keycloak.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username("admin")
                .password("admin")
                .build();
    }
}
