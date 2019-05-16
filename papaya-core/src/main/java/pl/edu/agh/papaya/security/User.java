package pl.edu.agh.papaya.security;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.keycloak.representations.idm.UserRepresentation;

@Getter
@AllArgsConstructor
@EqualsAndHashCode
public class User {

    private String id;

    private String email;

    private String firstName;

    private String lastName;

    public static User fromUserRepresentation(UserRepresentation repr) {
        return new User(
                repr.getId(),
                repr.getEmail(),
                repr.getFirstName(),
                repr.getLastName());
    }
}
