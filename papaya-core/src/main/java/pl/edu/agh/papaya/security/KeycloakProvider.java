package pl.edu.agh.papaya.security;

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.admin.client.resource.RealmResource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class KeycloakProvider {

    private final KeycloakClientConfiguration config;

    private final KeycloakSpringBootProperties props;

    // it needs to be populated lazily because
    // in tests we override the configuration above
    // and somehow it doesn't work
    @SuppressWarnings("PMD.AvoidUsingVolatile")
    private transient volatile Keycloak keycloak;

    public RealmResource getRealm() {
        return getKeycloak().realm(props.getRealm());
    }

    public Keycloak getKeycloak() {
        ensureInitialized();
        return keycloak;
    }

    private void ensureInitialized() {
        if (keycloak == null) {
            synchronized (KeycloakProvider.class) {
                if (keycloak == null) {
                    initialize();
                }
            }
        }
    }

    private void initialize() {
        keycloak = KeycloakBuilder.builder()
                .realm(props.getRealm())
                .serverUrl(props.getAuthServerUrl())
                .clientId(config.getId())
                .clientSecret(Strings.emptyToNull(config.getSecret()))
                .username(config.getUsername())
                .password(config.getPassword())
                .build();
    }
}
