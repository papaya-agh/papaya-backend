package pl.edu.agh.papaya.mappers;

import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserDto;
import pl.edu.agh.papaya.model.User;

@Component
public class UserMapper implements Mapper<User, UserDto> {

    @Override
    public UserDto mapToApi(User user) {
        return new UserDto()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName());
    }
}
