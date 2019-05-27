package pl.edu.agh.papaya.mappers;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.security.User;
import pl.edu.agh.papaya.security.UserService;

@Component
@RequiredArgsConstructor
public class UserInProjectMapper implements Mapper<UserInProject, ProjectMemberDto> {

    private final UserRoleMapper userRoleMapper;

    private final UserMapper userMapper;

    private final UserService userService;

    @Override
    public ProjectMemberDto mapToApi(UserInProject userInProject) {
        Optional<User> user = userService.getUserById(userInProject.getUserId());
        return new ProjectMemberDto()
                .user(userMapper.mapToApi(user.orElse(userService.nonExistentUser())))
                .role(userRoleMapper.mapToApi(userInProject.getUserRole()));
    }
}
