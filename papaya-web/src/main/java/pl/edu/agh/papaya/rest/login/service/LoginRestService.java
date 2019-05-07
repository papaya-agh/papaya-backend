package pl.edu.agh.papaya.rest.login.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.LoginRequest;
import pl.edu.agh.papaya.api.model.LoginResult;
import pl.edu.agh.papaya.security.AuthenticationException;
import pl.edu.agh.papaya.security.AuthenticationService;

@Service
@RequiredArgsConstructor
public class LoginRestService {

    private final AuthenticationService authenticationService;

    public ResponseEntity<LoginResult> requestLogin(LoginRequest request) {
        LoginResult result;
        try {
            result = authenticationService.logIn(request.getUsername());
        } catch (AuthenticationException e) {
            result = new LoginResult().valid(false)
                    .errorMessage(e.getLocalizedMessage());
            return new ResponseEntity<>(result, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
