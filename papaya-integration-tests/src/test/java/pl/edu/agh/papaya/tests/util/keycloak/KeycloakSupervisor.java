package pl.edu.agh.papaya.tests.util.keycloak;

import java.util.LinkedHashMap;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

@Component
public class KeycloakSupervisor {

    private final GenericContainer keycloakContainer =
            new GenericContainer("kjarosh/papaya-keycloak")
                    .withExposedPorts(8080)
                    .waitingFor(Wait.forHttp("/auth"));
    private String keycloakAuthServerUrl;
    private Keycloak keycloak;

    @PostConstruct
    public void start() {
        keycloakContainer.start();
        Runtime.getRuntime().addShutdownHook(new Thread(keycloakContainer::stop));
        setUp();
    }

    private void setUp() {
        String addr = keycloakContainer.getContainerIpAddress();
        Integer port = keycloakContainer.getMappedPort(8080);
        keycloakAuthServerUrl = "http://" + addr + ":" + port + "/auth";

        keycloak = KeycloakBuilder.builder()
                .realm("papaya")
                .serverUrl(keycloakAuthServerUrl)
                .clientId("admin-cli")
                .username("papaya-cli")
                .password("papaya")
                .build();
    }

    @PreDestroy
    public void stop() {
        keycloakContainer.stop();
    }

    public RealmResource getKeycloakRealm() {
        return keycloak.realm("papaya");
    }

    public String getAccessToken(String username, String password) {
        var restTemplate = new RestTemplate();
        var headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        var map = new LinkedMultiValueMap<>();
        map.add("grant_type", "password");
        map.add("client_id", "admin-cli");
        map.add("username", username);
        map.add("password", password);
        var tokenUrl = getAuthServerUrl() + "/realms/papaya/protocol/openid-connect/token";
        var token = restTemplate.postForObject(tokenUrl, new HttpEntity<>(map, headers), LinkedHashMap.class);

        assert token != null;
        return token.get("access_token").toString();
    }

    public String getAuthServerUrl() {
        return keycloakAuthServerUrl;
    }
}
