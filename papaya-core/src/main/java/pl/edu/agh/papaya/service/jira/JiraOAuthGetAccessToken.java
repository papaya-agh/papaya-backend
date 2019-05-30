package pl.edu.agh.papaya.service.jira;

import com.google.api.client.auth.oauth.OAuthGetAccessToken;
import java.net.URL;

class JiraOAuthGetAccessToken extends OAuthGetAccessToken {

    JiraOAuthGetAccessToken(URL authorizationServerUrl) {
        super(authorizationServerUrl.toString());
        this.usePost = true;
    }
}
