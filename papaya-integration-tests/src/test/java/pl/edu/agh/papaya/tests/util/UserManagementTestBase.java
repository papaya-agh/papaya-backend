package pl.edu.agh.papaya.tests.util;

import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.papaya.tests.util.keycloak.KeycloakSupervisor;

public abstract class UserManagementTestBase extends ConcordionSpringTestBase {

    @Autowired
    public KeycloakSupervisor keycloakSupervisor;
    @Autowired
    private ClientApiProvider clientApiProvider;

    public String switchUser(String username) {
        String accessToken = keycloakSupervisor.getAccessToken(username, username);
        clientApiProvider.getApiClient()
                .addDefaultHeader("Authorization", "Bearer " + accessToken);
        return describeUser(username);
    }

    public String describeUser(String username) {
        return getFullName(getUser(username));
    }

    private String getFullName(UserRepresentation user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    public UserRepresentation getUser(String username) {
        return keycloakSupervisor.getKeycloakRealm()
                .users()
                .search(username)
                .get(0);
    }

    public String getUserId(String username) {
        return getUser(username).getId();
    }
}
