package pl.edu.agh.papaya.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.model.UserInProject;

@Component
public class UserInProjectMapper implements Mapper<UserInProject, ProjectMemberDto> {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Autowired
    private UserMapper userMapper;

    @Override
    public ProjectMemberDto mapToApi(UserInProject userInProject) {
        return new ProjectMemberDto()
                .user(userMapper.mapToApi(userInProject.getUser()))
                .role(userRoleMapper.mapToApi(userInProject.getUserRole()));
    }
}
