package pl.edu.agh.papaya.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import pl.edu.agh.papaya.model.User;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserPrincipal {

    private User user;

    public String toString() {
        return user.getEmail();
    }
}
