package pl.edu.agh.papaya.tests.util;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import org.springframework.test.context.web.WebAppConfiguration;
import pl.edu.agh.papaya.Papaya;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.repository.UserRepository;
import pl.edu.agh.papaya.security.UserPrincipal;

@RunWith(ConcordionRunner.class)
@WebAppConfiguration
@ContextConfiguration(classes = {Papaya.class})
@ActiveProfiles(profiles = "test")
public abstract class ConcordionSpringTestBase {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    private final Map<String, User> testUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Before
    public void setUpAuthentication() {
        addTestUser("heinz", "Heinz", "Doofenshmirtz");
        addTestUser("rick", "Rick", "Sanchez");
        addTestUser("peter", "Peter", "Griffin");
    }

    private void addTestUser(String username, String firstName, String lastName) {
        User testUser = new User();
        testUser.setEmail(username + "@example.com");
        testUser.setFirstName(firstName);
        testUser.setLastName(lastName);
        userRepository.save(testUser);
        testUsers.put(username, testUser);
    }

    public String switchUser(String username) {
        UserPrincipal principal = createPrincipal(username);
        Authentication authentication = createAuthentication(principal);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return getFullName(principal.getUser());
    }

    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }

    private UserPrincipal createPrincipal(String username) {
        User testUser = testUsers.get(username);
        if (testUser == null) {
            throw new IllegalArgumentException("Invalid user: " + username);
        }

        return new UserPrincipal(testUser);
    }

    private Authentication createAuthentication(UserPrincipal principal) {
        return new UsernamePasswordAuthenticationToken(
                principal, "", Collections.emptyList());
    }
}
