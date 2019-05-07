package pl.edu.agh.papaya.rest.users.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import javax.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.UserDto;
import pl.edu.agh.papaya.api.service.MeApi;
import pl.edu.agh.papaya.api.service.UsersApi;
import pl.edu.agh.papaya.mappers.UserMapper;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.user.UserService;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@RestController
@RequiredArgsConstructor
public class UsersController implements UsersApi, MeApi {

    private final UserService userService;

    private final UserMapper userMapper;

    private final UserContext userContext;

    @Override
    public ResponseEntity<UserDto> getAuthenticatedUser() {
        return getUser(userContext.getUserIdn());
    }

    @Override
    public ResponseEntity<UserDto> getUser(Long userId) {
        User user = userService.getUserById(userId).orElseThrow(ResourceNotFoundException::new);
        return ResponseEntity.ok(userMapper.mapToApi(user));
    }

    @Override
    public Optional<ObjectMapper> getObjectMapper() {
        return Optional.empty();
    }

    @Override
    public Optional<String> getAcceptHeader() {
        return this.getRequest().map(request -> request.getHeader("Accept"));
    }

    @Override
    public Optional<HttpServletRequest> getRequest() {
        return Optional.empty();
    }
}
