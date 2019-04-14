package pl.edu.agh.papaya.security;

import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import java.util.Collections;
import java.util.List;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.repository.UserRepository;

@Service
public class AuthenticationService {

    @Autowired
    private TokenRepository tokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Upon successful login, this method returns a generated bearer token, which shall be used as an authorization
     * method for subsequent requests.
     * <p>
     * The returned token unambiguously identifies the authenticated user and his roles.
     *
     * @return the generated token
     *
     * @throws pl.edu.agh.papaya.security.AuthenticationException when authentication fails
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public String logIn(String username) throws AuthenticationException {
        validateEmail(username);

        User user = userRepository.findByEmail(username).orElseGet(() -> {
            User newUser = generateRandomUser(username);
            userRepository.save(newUser);
            return newUser;
        });

        return tokenRepository.newToken(toSpringUser(user));
    }

    private User generateRandomUser(String username) {
        User user = new User();
        user.setEmail(username);

        Name name = new Faker().name();
        user.setFirstName(name.firstName());
        user.setLastName(name.lastName());

        return user;
    }

    private void validateEmail(String email) throws AuthenticationException {
        try {
            new InternetAddress(email).validate();
        } catch (AddressException e) {
            throw new AuthenticationException("Invalid email address", e);
        }
    }

    private org.springframework.security.core.userdetails.User toSpringUser(User user) {
        List<GrantedAuthority> authorities = Collections.emptyList();
        return new org.springframework.security.core.userdetails.User(user.getEmail(), "", authorities);
    }
}
