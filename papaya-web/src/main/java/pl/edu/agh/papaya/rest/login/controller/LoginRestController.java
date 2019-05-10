package pl.edu.agh.papaya.rest.login.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.LoginRequest;
import pl.edu.agh.papaya.api.model.LoginResult;
import pl.edu.agh.papaya.api.service.LoginApi;
import pl.edu.agh.papaya.rest.login.service.LoginRestService;

@RestController
public class LoginRestController implements LoginApi {

    @Autowired
    private LoginRestService loginRestService;

    @Override
    public ResponseEntity<LoginResult> requestLogin(LoginRequest request) {
        return loginRestService.requestLogin(request);
    }
}
