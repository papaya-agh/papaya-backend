package pl.edu.agh.papaya.tests.util;

import org.concordion.integration.junit4.ConcordionRunner;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.rules.SpringClassRule;
import org.springframework.test.context.junit4.rules.SpringMethodRule;
import pl.edu.agh.papaya.Papaya;
import pl.edu.agh.papaya.api.client.ApiException;

@RunWith(ConcordionRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@ContextConfiguration(classes = {Papaya.class})
@ActiveProfiles(profiles = "test")
public abstract class ConcordionSpringTestBase extends ConcordionTestUtils {

    @ClassRule
    public static final SpringClassRule SPRING_CLASS_RULE = new SpringClassRule();

    @Rule
    public final SpringMethodRule springMethodRule = new SpringMethodRule();

    @Autowired
    private TestUsersManager testUsersManager;

    public String switchUser(String username) throws ApiException {
        return testUsersManager.switchUser(username);
    }

    public String describeUser(String username) {
        return testUsersManager.describeUser(username);
    }

    public String getUserId(String username) {
        return testUsersManager.getUserId(username);
    }
}
