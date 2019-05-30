package pl.edu.agh.papaya.service.jira;

import com.google.api.client.auth.oauth.OAuthGetTemporaryToken;
import java.net.URL;

class JiraOAuthGetTemporaryToken extends OAuthGetTemporaryToken {

    JiraOAuthGetTemporaryToken(URL authorizationServerUrl) {
        super(authorizationServerUrl.toString());
        this.usePost = true;
    }
}
