package com.marcoaga02.modularhub.config;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.MountableFile;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@ActiveProfiles("keycloak")
public abstract class BaseKeycloakIT extends BaseIT {

    protected static final String REALM = "test-realm";
    protected static final String FRONTEND_CLIENT_ID = "test-frontend-client";
    protected static final String APP_URL = "http://localhost:4200";
    protected static final String FIRST_GROUP_NAME = "first-group";
    protected static final String FIRST_GROUP_DESCRIPTION = "first group description";
    protected static final String SECOND_GROUP_NAME = "second-group";
    protected static final String SECOND_GROUP_DESCRIPTION = "second group description";
    protected static final int MIN_PASSWORD_LENGTH = 8;
    protected static final String MODULARHUB_REALM = "modularhub";
    protected static final String BACKEND_CLIENT_SECRET = "test-backend-client-secret";
    protected static final String MAILPIT_HOST = "mailpit";
    protected static final int MAILPIT_SMTP_PORT = 1025;

    private static final String ADMIN = "admin";
    private static final Path MODULARHUB_REALM_FILE = Path.of("keycloak", "import", "modularhub-realm.json");
    private static final String KEYCLOAK_IMPORT_DIR = "/opt/keycloak/data/import/";
    private static final int MAILPIT_API_PORT = 8025;

    private static final Network NETWORK = Network.newNetwork();

    private static final GenericContainer<?> MAILPIT = new GenericContainer<>(TestContainersImages.MAILPIT)
            .withNetwork(NETWORK)
            .withNetworkAliases(MAILPIT_HOST)
            .withExposedPorts(MAILPIT_API_PORT);

    private static final KeycloakContainer KEYCLOAK = new KeycloakContainer(TestContainersImages.KEYCLOAK)
            .withNetwork(NETWORK)
            .withAdminUsername(ADMIN)
            .withAdminPassword(ADMIN)
            .withCopyFileToContainer(MountableFile.forHostPath(MODULARHUB_REALM_FILE),
                    KEYCLOAK_IMPORT_DIR + MODULARHUB_REALM_FILE.getFileName())
            .withEnv(Map.of(
                    "MODULARHUB_BACKEND_CLIENT_SECRET", BACKEND_CLIENT_SECRET,
                    "MODULARHUB_SMTP_HOST", MAILPIT_HOST,
                    "MODULARHUB_SMTP_PORT", String.valueOf(MAILPIT_SMTP_PORT),
                    "MODULARHUB_SMTP_FROM", "noreply@modularhub.test",
                    "MODULARHUB_SMTP_AUTH", "false",
                    "MODULARHUB_SMTP_USER", "",
                    "MODULARHUB_SMTP_PASSWORD", "",
                    "MODULARHUB_SMTP_STARTTLS", "false",
                    "MODULARHUB_SMTP_SSL", "false"));

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    private static final JsonMapper JSON_MAPPER = JsonMapper.builder().build();

    static {
        MAILPIT.start();
        KEYCLOAK.start();
        try (Keycloak client = newAdminClient()) {
            client.realms().create(testRealm());
        }
    }

    protected Keycloak adminClient;

    @DynamicPropertySource
    static void keycloakProperties(DynamicPropertyRegistry registry) {
        registry.add("keycloak.server-url", KEYCLOAK::getAuthServerUrl);
        registry.add("keycloak.realm", () -> REALM);
        registry.add("keycloak.admin-username", () -> ADMIN);
        registry.add("keycloak.admin-password", () -> ADMIN);
        registry.add("keycloak.frontend-client-id", () -> FRONTEND_CLIENT_ID);
        registry.add("keycloak.app-url", () -> APP_URL);
    }

    @BeforeEach
    void openAdminClient() {
        adminClient = newAdminClient();
    }

    @AfterEach
    void deleteUsersAndMails() throws IOException, InterruptedException {
        realm().users().list().forEach(user -> realm().users().delete(user.getId()).close());
        adminClient.close();
        HTTP_CLIENT.send(mailpitRequest().DELETE().build(), HttpResponse.BodyHandlers.discarding());
    }

    protected static String keycloakUrl() {
        return KEYCLOAK.getAuthServerUrl();
    }

    protected RealmResource realm() {
        return adminClient.realm(REALM);
    }

    protected String groupId(String name) {
        return realm().groups().groups().stream()
                .filter(group -> group.getName().equals(name))
                .map(GroupRepresentation::getId)
                .findFirst()
                .orElseThrow();
    }

    protected List<String> groupIdsOf(String userId) {
        return realm().users().get(userId).groups().stream()
                .map(GroupRepresentation::getId)
                .toList();
    }

    protected List<String> mailRecipients() throws IOException, InterruptedException {
        String body = HTTP_CLIENT.send(mailpitRequest().GET().build(), HttpResponse.BodyHandlers.ofString()).body();

        List<String> recipients = new ArrayList<>();
        for (JsonNode message : JSON_MAPPER.readTree(body).path("messages")) {
            for (JsonNode to : message.path("To")) {
                recipients.add(to.path("Address").asString());
            }
        }
        return recipients;
    }

    private static HttpRequest.Builder mailpitRequest() {
        return HttpRequest.newBuilder(URI.create("http://%s:%d/api/v1/messages"
                .formatted(MAILPIT.getHost(), MAILPIT.getMappedPort(MAILPIT_API_PORT))));
    }

    private static Keycloak newAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(KEYCLOAK.getAuthServerUrl())
                .realm("master")
                .clientId("admin-cli")
                .username(ADMIN)
                .password(ADMIN)
                .build();
    }

    private static RealmRepresentation testRealm() {
        ClientRepresentation frontendClient = new ClientRepresentation();
        frontendClient.setClientId(FRONTEND_CLIENT_ID);
        frontendClient.setPublicClient(true);
        frontendClient.setEnabled(true);
        frontendClient.setRedirectUris(List.of(APP_URL + "/*"));

        RealmRepresentation realm = new RealmRepresentation();
        realm.setRealm(REALM);
        realm.setEnabled(true);
        realm.setEditUsernameAllowed(true);
        realm.setPasswordPolicy("length(" + MIN_PASSWORD_LENGTH + ")");
        realm.setSmtpServer(Map.of(
                "host", MAILPIT_HOST,
                "port", String.valueOf(MAILPIT_SMTP_PORT),
                "from", "noreply@modularhub.test"));
        realm.setClients(List.of(frontendClient));
        realm.setGroups(List.of(
                group(FIRST_GROUP_NAME, FIRST_GROUP_DESCRIPTION),
                group(SECOND_GROUP_NAME, SECOND_GROUP_DESCRIPTION)));
        return realm;
    }

    private static GroupRepresentation group(String name, String description) {
        GroupRepresentation group = new GroupRepresentation();
        group.setName(name);
        group.setDescription(description);
        return group;
    }
}
