package pl.edu.agh.papaya.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.model.User;

@Component
public class UserContext {

    public String getUserId() {
        return getUser().getId().toString();
    }

    public Long getUserIdn() {
        return getUser().getId();
    }

    public User getUser() {
        return getPrincipal().getUser();
    }

    private UserPrincipal getPrincipal() {
        Object principal = getAuthentication().getPrincipal();

        if (principal instanceof UserPrincipal) {
            return (UserPrincipal) principal;
        }

        throw new UserNotAuthenticatedException("Invalid principal: " + principal + " (" + principal.getClass() + ")");
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new UserNotAuthenticatedException();
        }

        return authentication;
    }
}
