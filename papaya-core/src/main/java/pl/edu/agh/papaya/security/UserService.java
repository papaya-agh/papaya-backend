package pl.edu.agh.papaya.security;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import javax.ws.rs.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final KeycloakProvider keycloakProvider;

    public Optional<User> getUserById(String userId) {
        return getUserBy(resource -> resource.get(userId).toRepresentation());
    }

    private Optional<User> getUserBy(Function<UsersResource, UserRepresentation> selector) {
        try {
            UserRepresentation user = selector.apply(keycloakProvider.getRealm().users());

            return Optional.ofNullable(user)
                    .map(User::fromUserRepresentation);
        } catch (NotFoundException e) {
            return Optional.empty();
        }
    }

    public Optional<User> getUserByEmail(String userEmail) {
        return getOnlyOneUserBy(resource -> resource.search(null, null, null, userEmail, 0, 1));
    }

    private Optional<User> getOnlyOneUserBy(Function<UsersResource, List<UserRepresentation>> selector) {
        return getUserBy(resource -> Optional.ofNullable(selector.apply(resource))
                .filter(list -> list.size() == 1)
                .map(list -> list.get(0))
                .orElse(null));
    }

    public Optional<User> getUserByUsername(String username) {
        return getOnlyOneUserBy(resource -> resource.search(username));
    }

    public User nonExistentUser() {
        return new User(null, null, null, null);
    }
}
