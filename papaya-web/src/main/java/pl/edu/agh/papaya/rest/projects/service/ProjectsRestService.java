package pl.edu.agh.papaya.rest.projects.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.mappers.UserInProjectMapper;
import pl.edu.agh.papaya.mappers.UserMapper;
import pl.edu.agh.papaya.mappers.UserRoleMapper;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.rest.common.UserIdentificationService;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.security.UserNotAuthorizedException;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.service.user.UserService;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@Service
@SuppressWarnings("checkstyle:ClassFanOutComplexity")
@RequiredArgsConstructor
public class ProjectsRestService {

    private final UserContext userContext;

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    private final UserService userService;

    private final UserIdentificationService userIdentificationService;

    private final UserRoleMapper userRoleMapper;

    private final UserInProjectMapper userInProjectMapper;

    private final UserMapper userMapper;

    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        Project created = projectService.newProject()
                .withName(projectDto.getName())
                .withDescription(projectDto.getDescription())
                .withInitialCoefficient(projectDto.getInitialCoefficient())
                .withWebhook(projectDto.getWebhookUrl())
                .withChannelName(projectDto.getChannelName())
                .create();
        return ResponseEntity.ok(projectMapper.mapToApi(created));
    }

    public ResponseEntity<ProjectMemberDto> addUserToProject(UserIdentificationDto userIdentification, Long projectId) {
        User user = userIdentificationService.identify(userIdentification)
                .orElseThrow(BadRequestException::new);

        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        try {
            projectService.setUserRole(project, user, UserRole.MEMBER);
        } catch (UserNotAuthorizedException e) {
            throw new ForbiddenAccessException(e);
        }

        return ResponseEntity.ok(new ProjectMemberDto()
                .user(userMapper.mapToApi(user))
                .role(UserRoleDto.MEMBER));
    }

    public ResponseEntity<ProjectDto> getProjectById(Long id) {
        Project project = getValidProject(id);

        return ResponseEntity.ok(projectMapper.mapToApi(project));
    }

    public Project getValidProject(Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!projectService.isUserInProject(project, userContext.getUserId())) {
            throw new ForbiddenAccessException();
        }

        return project;
    }

    public ResponseEntity<List<ProjectDto>> getProjects() {
        List<Project> projects = projectService.getUserProjects(userContext.getUserId());
        return ResponseEntity.ok(projectMapper.mapToApi(projects));
    }

    public ResponseEntity<List<ProjectMemberDto>> getUsersFromProject(Long projectId) {
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        List<UserInProject> usersInProject = projectService.getUsersInProject(project);

        List<ProjectMemberDto> usersInProjectDto = usersInProject.stream()
                .map(userInProjectMapper::mapToApi)
                .collect(Collectors.toList());
        return ResponseEntity.ok(usersInProjectDto);
    }

    public ResponseEntity<Void> removeUserFromProject(Long projectId, Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(BadRequestException::new);

        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        if (project.isAdmin(user)) {
            throw new BadRequestException("Cannot remove an admin");
        }

        try {
            projectService.removeUser(project, user);
        } catch (UserNotAuthorizedException e) {
            throw new ForbiddenAccessException(e);
        }

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    public void setUserRole(UserRoleDto userRole, Long projectId, Long userId) {
        User user = userService.getUserById(userId)
                .orElseThrow(BadRequestException::new);

        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        UserRole role = userRoleMapper.mapFromApi(userRole);

        try {
            projectService.setUserRole(project, user, role);
        } catch (UserNotAuthorizedException e) {
            throw new ForbiddenAccessException(e);
        }
    }
}
