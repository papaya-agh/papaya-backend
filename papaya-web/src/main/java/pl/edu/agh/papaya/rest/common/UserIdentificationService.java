package pl.edu.agh.papaya.rest.common;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.service.user.UserService;

@Component
@RequiredArgsConstructor
public class UserIdentificationService {

    private final UserService userService;

    public Optional<User> identify(UserIdentificationDto userIdentification) {
        Long userId = userIdentification.getId();
        String userEmail = userIdentification.getEmail();

        if (userId != null && userEmail != null) {
            boolean validEmail = userService.getUserById(userId)
                    .map(User::getEmail)
                    .map(userEmail::equals)
                    .orElse(false);
            if (!validEmail) {
                return Optional.empty();
            }
        }

        if (userId != null) {
            return userService.getUserById(userId);
        }

        if (userEmail != null) {
            return userService.getUserByEmail(userEmail);
        }

        return Optional.empty();
    }
}
