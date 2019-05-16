package pl.edu.agh.papaya.tests.util.keycloak;

import lombok.RequiredArgsConstructor;
import org.keycloak.adapters.springboot.KeycloakSpringBootProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.security.KeycloakClientConfiguration;

@Component
@RequiredArgsConstructor
public class KeycloakConfigurationProvider {

    private final KeycloakSupervisor keycloakSupervisor;

    @Primary
    @Bean
    public KeycloakSpringBootProperties keycloakSpringBootProperties() {
        var ret = new KeycloakSpringBootProperties();
        ret.setRealm("papaya");
        ret.setAuthServerUrl(keycloakSupervisor.getAuthServerUrl());
        ret.setResource("papaya-web");
        ret.setPublicClient(true);
        ret.setBearerOnly(true);
        ret.setDisableTrustManager(true);
        ret.setSslRequired("none");
        return ret;
    }

    @Primary
    @Bean
    public KeycloakClientConfiguration keycloakClientConfiguration() {
        var ret = new KeycloakClientConfiguration();
        ret.setId("admin-cli");
        ret.setUsername("papaya-cli");
        ret.setPassword("papaya");
        return ret;
    }
}
