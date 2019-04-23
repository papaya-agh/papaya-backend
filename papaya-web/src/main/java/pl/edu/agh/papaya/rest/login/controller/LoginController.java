package pl.edu.agh.papaya.rest.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.LoginRequest;
import pl.edu.agh.papaya.api.model.LoginResult;
import pl.edu.agh.papaya.api.service.LoginApi;
import pl.edu.agh.papaya.security.AuthenticationException;
import pl.edu.agh.papaya.security.AuthenticationService;

@RestController
public class LoginController implements LoginApi {

    @Autowired
    private AuthenticationService authenticationService;

    @Override
    public ResponseEntity<LoginResult> requestLogin(LoginRequest request) {
        String token;
        LoginResult result = new LoginResult();
        try {
            token = authenticationService.logIn(request.getUsername());
        } catch (AuthenticationException e) {
            result.setValid(false);
            result.setErrorMessage(e.getLocalizedMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }

        result.setValid(true);
        result.setToken(token);
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
