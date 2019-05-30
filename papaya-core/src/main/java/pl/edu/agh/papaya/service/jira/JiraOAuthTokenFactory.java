package pl.edu.agh.papaya.service.jira;

import com.google.api.client.auth.oauth.OAuthRsaSigner;
import com.google.api.client.http.apache.ApacheHttpTransport;
import com.google.api.client.util.Base64;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize"})
class JiraOAuthTokenFactory {

    private static final String CONSUMER_KEY = "OauthKey";

    private final URL accessTokenUrl;
    private final URL temporaryTokenUrl;

    JiraOAuthTokenFactory(URL jiraBaseUrl) throws MalformedURLException {
        this.accessTokenUrl = new URL(jiraBaseUrl, "plugins/servlet/oauth/access-token");
        this.temporaryTokenUrl = new URL(jiraBaseUrl, "plugins/servlet/oauth/request-token");
    }

    JiraOAuthGetAccessToken getJiraOAuthGetAccessToken(String jiraVerCode, String tmpToken, String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        JiraOAuthGetAccessToken accessToken = new JiraOAuthGetAccessToken(accessTokenUrl);
        accessToken.consumerKey = CONSUMER_KEY;
        accessToken.signer = getOAuthRsaSigner(privateKey);
        accessToken.transport = new ApacheHttpTransport();
        accessToken.temporaryToken = tmpToken;
        accessToken.verifier = jiraVerCode;
        return accessToken;
    }

    private OAuthRsaSigner getOAuthRsaSigner(String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        OAuthRsaSigner oAuthRsaSigner = new OAuthRsaSigner();
        oAuthRsaSigner.privateKey = getPrivateKey(privateKey);
        return oAuthRsaSigner;
    }

    private PrivateKey getPrivateKey(String privateKey) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] privateBytes = Base64.decodeBase64(privateKey);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(privateBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(keySpec);
    }

    JiraOAuthGetTemporaryToken getTemporaryToken(String privateKey)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        JiraOAuthGetTemporaryToken oAuthGetTemporaryToken = new JiraOAuthGetTemporaryToken(temporaryTokenUrl);
        oAuthGetTemporaryToken.consumerKey = CONSUMER_KEY;
        oAuthGetTemporaryToken.signer = getOAuthRsaSigner(privateKey);
        oAuthGetTemporaryToken.transport = new ApacheHttpTransport();
        oAuthGetTemporaryToken.callback = "oob";
        return oAuthGetTemporaryToken;
    }
}
