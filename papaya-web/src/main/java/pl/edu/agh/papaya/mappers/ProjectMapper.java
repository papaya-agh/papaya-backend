package pl.edu.agh.papaya.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.edu.agh.papaya.api.model.JiraBoardDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.rest.jira.service.JiraRestService;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.NotAcceptableException;

@Component
@RequiredArgsConstructor
public class ProjectMapper implements Mapper<Project, ProjectDto> {

    private final UserContext userContext;

    private final UserRoleMapper userRoleMapper;

    private final JiraRestService jiraRestService;

    @Override
    public ProjectDto mapToApi(Project modelProject) {
        JiraBoardDto jiraBoardDto;
        try {
            jiraBoardDto = jiraRestService.getJiraBoard(modelProject).orElse(null);
        } catch (NotAcceptableException | BadRequestException e) {
            jiraBoardDto = null;
        }

        UserRole userRole = modelProject.getUserRoleInProject(userContext.getUser())
                .orElseThrow(ForbiddenAccessException::new);
        return new ProjectDto()
                .id(modelProject.getId())
                .description(modelProject.getDescription())
                .initialCoefficient(modelProject.getInitialCoefficient())
                .name(modelProject.getName())
                .userRole(userRoleMapper.mapToApi(userRole))
                .jiraBoard(jiraBoardDto);
    }
}
