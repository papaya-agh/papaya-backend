package pl.edu.agh.papaya.security;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.model.User;

@Component
public class UserContext {

    private UserPrincipal getPrincipal() {
        return (UserPrincipal) SecurityContextHolder.getContext()
                .getAuthentication()
                .getPrincipal();
    }

    public User getUser() {
        return getPrincipal().getUser();
    }

    public String getUserId() {
        return getUser().getId().toString();
    }
}
