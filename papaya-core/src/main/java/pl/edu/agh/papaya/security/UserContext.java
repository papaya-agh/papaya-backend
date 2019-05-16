package pl.edu.agh.papaya.security;

import lombok.RequiredArgsConstructor;
import org.keycloak.KeycloakPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserContext {

    private final UserService userService;

    public String getUserId() {
        return getUser().getId();
    }

    public User getUser() {
        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof KeycloakPrincipal) {
            return mapToUser((KeycloakPrincipal) principal);
        }

        throw new UserNotAuthenticatedException("Invalid principal: " + principal + " (" + principal.getClass() + ")");
    }

    private User mapToUser(KeycloakPrincipal principal) {
        String userId = principal.toString();
        return userService.getUserById(userId)
                .orElseThrow(AssertionError::new);
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserNotAuthenticatedException();
        }

        return authentication;
    }
}
