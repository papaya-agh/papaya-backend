package pl.edu.agh.papaya.security;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {

    @Autowired
    private TokenRepository tokenRepository;

    /**
     * Upon successful login, this method returns a generated bearer token, which shall be used as an authorization
     * method for subsequent requests.
     * <p>
     * The returned token unambiguously identifies the authenticated user and his roles.
     *
     * @return the generated token
     */
    public String logIn(String username) {
        List<GrantedAuthority> authorities = Collections.emptyList();
        return tokenRepository.newToken(new User(username, "", authorities));
    }
}
