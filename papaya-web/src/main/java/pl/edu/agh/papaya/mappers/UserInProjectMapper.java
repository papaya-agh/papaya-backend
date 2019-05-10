package pl.edu.agh.papaya.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.model.UserInProject;

@Component
@RequiredArgsConstructor
public class UserInProjectMapper implements Mapper<UserInProject, ProjectMemberDto> {

    private final UserRoleMapper userRoleMapper;

    private final UserMapper userMapper;

    @Override
    public ProjectMemberDto mapToApi(UserInProject userInProject) {
        return new ProjectMemberDto()
                .user(userMapper.mapToApi(userInProject.getUser()))
                .role(userRoleMapper.mapToApi(userInProject.getUserRole()));
    }
}
