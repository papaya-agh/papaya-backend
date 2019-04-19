package pl.edu.agh.papaya.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.model.User;

@Component
public class UserContext {

    private UserPrincipal getPrincipal() {
        return (UserPrincipal) getAuthentication()
                .getPrincipal();
    }

    private Authentication getAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext()
                .getAuthentication();
        if (authentication == null) {
            throw new UserNotAuthenticatedException();
        }
        return authentication;
    }

    public User getUser() {
        return getPrincipal().getUser();
    }

    public String getUserId() {
        return getUser().getId().toString();
    }
}
