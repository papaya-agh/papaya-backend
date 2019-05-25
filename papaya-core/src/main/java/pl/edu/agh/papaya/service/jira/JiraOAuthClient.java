package pl.edu.agh.papaya.service.jira;

import com.google.api.client.auth.oauth.OAuthAuthorizeTemporaryTokenUrl;
import com.google.api.client.auth.oauth.OAuthCredentialsResponse;
import com.google.api.client.auth.oauth.OAuthParameters;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Optional;
import java.util.Scanner;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;

@Slf4j
@SuppressWarnings({"ClassFanOutComplexity", "PMD.BeanMembersShouldSerialize", "MultipleStringLiterals"})
final class JiraOAuthClient {

    private final JiraOAuthTokenFactory oAuthTokenFactory;
    private final URL jiraHome;

    private JiraOAuthClient(URL jiraHome) throws MalformedURLException {
        this.jiraHome = jiraHome;
        this.oAuthTokenFactory = new JiraOAuthTokenFactory(jiraHome);
    }

    static Optional<JiraOAuthClient> getJiraOAuthClient(URL jiraHome) {
        try {
            URL jiraHomeWithTrailingSlash = jiraHome.toString().endsWith("/") ? jiraHome :
                    new URL(jiraHome.toString() + "/");
            JiraOAuthClient jiraOAuthClient = new JiraOAuthClient(jiraHomeWithTrailingSlash);
            return Optional.of(jiraOAuthClient);
        } catch (MalformedURLException e) {
            log.error("Could not initialize JiraOAuthClient object. Check jiraHome URL", e);
            return Optional.empty();
        }
    }

    Optional<String> getAuthorizedTemporaryToken(String privateKey) {
        try {
            JiraOAuthGetTemporaryToken temporaryToken =
                    oAuthTokenFactory.getTemporaryToken(privateKey);
            OAuthCredentialsResponse response = temporaryToken.execute();

            return Optional.of(response.token);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            log.error("getAuthorizedTemporaryToken failed", e);
            return Optional.empty();
        }
    }

    Optional<String> getVerificationCodeUrl(String tmpToken) {
        try {
            URL authorizationUrl = new URL(jiraHome, "plugins/servlet/oauth/authorize");

            OAuthAuthorizeTemporaryTokenUrl authorizationURL =
                    new OAuthAuthorizeTemporaryTokenUrl(authorizationUrl.toString());
            authorizationURL.temporaryToken = tmpToken;

            return Optional.of(authorizationURL.toString());
        } catch (MalformedURLException e) {
            log.error("authorizationUrl is incorrect", e);
            return Optional.empty();
        }
    }

    Optional<String> getAccessToken(String jiraVerCode, String tmpToken, String privateKey) {
        try {
            JiraOAuthGetAccessToken oAuthAccessToken =
                    oAuthTokenFactory.getJiraOAuthGetAccessToken(jiraVerCode, tmpToken, privateKey);
            OAuthCredentialsResponse response = oAuthAccessToken.execute();
            return Optional.of(response.token);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | IOException e) {
            log.error("getAccessToken failed", e);
            return Optional.empty();
        }
    }

    Optional<JSONArray> makeAuthorizedGetRequest(String jiraVerificationCode,
            String accessToken, String endPoint, String privateKey) {
        try {
            OAuthParameters parameters = getParameters(jiraVerificationCode, accessToken, privateKey);
            String endPointWithoutLeadingSlash = endPoint.startsWith("/") ? endPoint.substring(1) : endPoint;
            HttpResponse response = getResponseFromUrl(
                    new GenericUrl(new URL(jiraHome, endPointWithoutLeadingSlash).toString()), parameters);
            return Optional.of(parseResponse(response));
        } catch (NoSuchAlgorithmException | IOException | InvalidKeySpecException e) {
            log.error("makeAuthorizedGetRequest failed", e);
            return Optional.empty();
        }
    }

    private OAuthParameters getParameters(String jiraVerCode, String tmpToken, String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        JiraOAuthGetAccessToken oAuthAccessToken =
                oAuthTokenFactory.getJiraOAuthGetAccessToken(jiraVerCode, tmpToken, privateKey);
        oAuthAccessToken.verifier = jiraVerCode;
        return oAuthAccessToken.createParameters();
    }

    private HttpResponse getResponseFromUrl(GenericUrl jiraUrl, OAuthParameters parameters) throws IOException {
        HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory(parameters);
        HttpRequest request = requestFactory.buildGetRequest(jiraUrl);
        return request.execute();
    }

    private JSONArray parseResponse(HttpResponse response) throws IOException {
        Scanner scanner = new Scanner(response.getContent(), StandardCharsets.UTF_8);
        StringBuilder result = new StringBuilder();
        while (scanner.hasNext()) {
            result.append(scanner.next());
        }

        if (result.charAt(0) == '{') {
            result.insert(0, "[");
            result.append("]");
        }

        return new JSONArray(result.toString());
    }
}
