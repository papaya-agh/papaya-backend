package pl.edu.agh.papaya.rest.login.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.LoginRequest;
import pl.edu.agh.papaya.api.model.LoginResult;
import pl.edu.agh.papaya.api.service.LoginApi;
import pl.edu.agh.papaya.rest.login.service.LoginRestService;

@RestController
@RequiredArgsConstructor
public class LoginRestController implements LoginApi {

    private final LoginRestService loginRestService;

    @Override
    public ResponseEntity<LoginResult> requestLogin(LoginRequest request) {
        return loginRestService.requestLogin(request);
    }
}
