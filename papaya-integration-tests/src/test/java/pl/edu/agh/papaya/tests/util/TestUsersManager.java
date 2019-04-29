package pl.edu.agh.papaya.tests.util;

import java.util.HashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.model.LoginRequest;
import pl.edu.agh.papaya.api.client.model.LoginResult;
import pl.edu.agh.papaya.api.client.service.LoginApi;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.repository.UserRepository;

@Component
public class TestUsersManager {

    private transient final Map<String, User> testUsers = new HashMap<>();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientApiProvider clientApiProvider;

    @Autowired
    private LoginApi loginApi;

    private boolean initialized;

    public String switchUser(String username) throws ApiException {
        initialize();

        User testUser = testUsers.get(username);
        if (testUser == null) {
            throw new IllegalArgumentException("Invalid user: " + username);
        }

        LoginResult loginResult = loginApi.requestLogin(new LoginRequest()
                .username(testUser.getEmail()));

        clientApiProvider.getApiClient()
                .addDefaultHeader("Authorization", "Bearer " + loginResult.getToken());
        return getFullName(testUser);
    }

    public String describeUser(String username) {
        initialize();
        return getFullName(testUsers.get(username));
    }

    public long getUserId(String username) {
        initialize();
        return testUsers.get(username).getId();
    }

    public User getUser(String username) {
        initialize();
        return testUsers.get(username);
    }

    @PostConstruct
    public void initialize() {
        if (initialized) {
            return;
        }

        initialized = true;

        addTestUser("heinz", "Heinz", "Doofenshmirtz", "heinz.d@evil.inc");
        addTestUser("rick", "Rick", "Sanchez", "rick.c137@sanchez.es");
        addTestUser("peter", "Peter", "Griffin", "peter.griffin@hotmail.com");
    }

    private void addTestUser(String username, String firstName, String lastName, String email) {
        User testUser = new User();
        testUser.setEmail(email);
        testUser.setFirstName(firstName);
        testUser.setLastName(lastName);
        userRepository.save(testUser);
        testUsers.put(username, testUser);
    }

    private String getFullName(User user) {
        return user.getFirstName() + " " + user.getLastName();
    }
}
