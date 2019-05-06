package pl.edu.agh.papaya.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.util.ForbiddenAccessException;

@Component
@RequiredArgsConstructor
public class ProjectMapper implements Mapper<Project, ProjectDto> {

    private final UserContext userContext;

    private final UserRoleMapper userRoleMapper;

    @Override
    public ProjectDto mapToApi(Project modelProject) {
        UserRole userRole = modelProject.getUserRole(userContext.getUser())
                .orElseThrow(ForbiddenAccessException::new);
        return new ProjectDto()
                .id(modelProject.getId())
                .description(modelProject.getDescription())
                .initialCoefficient(modelProject.getInitialCoefficient())
                .name(modelProject.getName())
                .userRole(userRoleMapper.mapToApi(userRole));
    }
}
