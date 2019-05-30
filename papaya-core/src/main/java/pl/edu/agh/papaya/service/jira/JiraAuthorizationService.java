package pl.edu.agh.papaya.service.jira;

import java.net.URL;
import java.util.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Getter
@Setter
@NoArgsConstructor
public class JiraAuthorizationService {

    @Value("${jira.auth.private.key}")
    private String privateKey;

    public Optional<String> getTemporaryToken(URL jiraHome) {
        Optional<JiraOAuthClient> jiraOAuthClient = JiraOAuthClient.getJiraOAuthClient(jiraHome);
        return jiraOAuthClient.isPresent() ? jiraOAuthClient.get().getAuthorizedTemporaryToken(privateKey)
                : Optional.empty();
    }

    public Optional<String> getVerificationCodeUrl(URL jiraHome, String temporaryToken) {
        Optional<JiraOAuthClient> jiraOAuthClient = JiraOAuthClient.getJiraOAuthClient(jiraHome);
        return jiraOAuthClient.isPresent() ? jiraOAuthClient.get().getVerificationCodeUrl(temporaryToken)
                : Optional.empty();
    }

    public Optional<String> getAccessToken(URL jiraHome, String jiraVerificationCode, String tempToken) {
        Optional<JiraOAuthClient> jiraOAuthClient = JiraOAuthClient.getJiraOAuthClient(jiraHome);
        return jiraOAuthClient.isPresent() ? jiraOAuthClient.get()
                .getAccessToken(jiraVerificationCode, tempToken, privateKey)
                : Optional.empty();
    }

    public Optional<JSONArray> makeAuthorizedGetRequest(URL jiraHome, String jiraVerificationCode,
            String accessToken, String endPoint) {
        Optional<JiraOAuthClient> jiraOAuthClient = JiraOAuthClient.getJiraOAuthClient(jiraHome);

        return jiraOAuthClient.isPresent() ? jiraOAuthClient.get()
                .makeAuthorizedGetRequest(jiraVerificationCode, accessToken, endPoint, privateKey)
                : Optional.empty();
    }
}
