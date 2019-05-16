package pl.edu.agh.papaya.security;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "keycloak-client", ignoreUnknownFields = false)
@Getter
@Setter
@EqualsAndHashCode
public class KeycloakClientConfiguration {

    private String id;

    private String secret;

    private String username;

    private String password;
}
