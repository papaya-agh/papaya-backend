package pl.edu.agh.papaya.mappers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.UserInProjectDto;
import pl.edu.agh.papaya.model.UserInProject;

@Component
public class UserInProjectMapper implements Mapper<UserInProject, UserInProjectDto> {

    @Autowired
    private UserRoleMapper userRoleMapper;

    @Override
    public UserInProjectDto mapToApi(UserInProject userInProject) {
        return new UserInProjectDto()
                .userId(userInProject.getUser().getId().toString())
                .role(userRoleMapper.mapToApi(userInProject.getUserRole()));
    }
}
