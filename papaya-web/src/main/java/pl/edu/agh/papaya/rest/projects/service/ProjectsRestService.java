package pl.edu.agh.papaya.rest.projects.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.CoefficientGraphDto;
import pl.edu.agh.papaya.api.model.CoefficientGraphPointDto;
import pl.edu.agh.papaya.api.model.JiraBoardDto;
import pl.edu.agh.papaya.api.model.JiraConfigDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.mappers.UserInProjectMapper;
import pl.edu.agh.papaya.mappers.UserMapper;
import pl.edu.agh.papaya.mappers.UserRoleMapper;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.rest.common.UserIdentificationService;
import pl.edu.agh.papaya.rest.jira.service.JiraRestService;
import pl.edu.agh.papaya.security.User;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.security.UserNotAuthorizedException;
import pl.edu.agh.papaya.security.UserService;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.ConflictException;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.NotAcceptableException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@Service
@SuppressWarnings({"checkstyle:ClassFanOutComplexity", "checkstyle:ClassDataAbstractionCoupling"})
@RequiredArgsConstructor
public class ProjectsRestService {

    private final UserContext userContext;

    private final ProjectService projectService;

    private final SprintService sprintService;

    private final ProjectMapper projectMapper;

    private final UserService userService;

    private final UserIdentificationService userIdentificationService;

    private final UserRoleMapper userRoleMapper;

    private final UserInProjectMapper userInProjectMapper;

    private final UserMapper userMapper;

    private final JiraRestService jiraRestService;

    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        try {
            Project created = projectService.newProject()
                    .withName(projectDto.getName())
                    .withDescription(projectDto.getDescription())
                    .withInitialCoefficient(projectDto.getInitialCoefficient())
                    .withWebhook(projectDto.getWebhookUrl())
                    .withChannelName(projectDto.getChannelName())
                    .withJiraUrl(projectDto.getJiraUrl())
                    .create();
            return ResponseEntity.ok(projectMapper.mapToApi(created));
        } catch (IllegalArgumentException e) {
            throw new NotAcceptableException(e);
        }
    }

    public ResponseEntity<ProjectMemberDto> addUserToProject(UserIdentificationDto userIdentification, Long projectId) {
        User user = userIdentificationService.identify(userIdentification)
                .orElseThrow(BadRequestException::new);

        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        if (projectService.isUserInProject(project, user.getId())) {
            throw new ConflictException("Requested user is already in project");
        }

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

        if (!project.isUserInProject(userContext.getUser())) {
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

    public ResponseEntity<Void> removeUserFromProject(Long projectId, String userId) {
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

    public void setUserRole(UserRoleDto userRole, Long projectId, String userId) {
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

    public ResponseEntity<JiraConfigDto> getJiraAuthorizationLink(Long projectId) {
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        return ResponseEntity.ok(new JiraConfigDto().url(jiraRestService.getJiraAuthorizationLink(project)));
    }

    public ResponseEntity<Void> setJiraSecret(JiraConfigDto jiraConfigDto, Long projectId) {
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        jiraRestService.setJiraSecret(project, jiraConfigDto.getSecret());

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<List<JiraBoardDto>> getJiraBoards(Long projectId) {
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        return ResponseEntity.ok(jiraRestService.getJiraBoards(project));
    }

    public ResponseEntity<Void> setJiraBoard(JiraBoardDto jiraBoardDto, Long projectId) {
        Project project = projectService.getProjectById(projectId)
                .orElseThrow(ResourceNotFoundException::new);

        jiraRestService.setJiraBoard(project, jiraBoardDto);

        return ResponseEntity.noContent().build();
    }

    public ResponseEntity<CoefficientGraphDto> getCoefficientGraph(Long projectId, LocalDate from, LocalDate to) {
        getValidProject(projectId);

        Stream<CoefficientGraphPointDto> points =
                sprintService.getByStateInProject(SprintState.CLOSED, projectId, LocalDateTime.now())
                        .stream()
                        .filter(sprint -> from == null || sprint.getDateClosed().isAfter(from.atStartOfDay()))
                        .filter(sprint -> to == null || sprint.getDateClosed().isBefore(to.plusDays(1).atStartOfDay()))
                        .map(sprint -> new CoefficientGraphPointDto()
                                .time(sprint.getDateClosed().atZone(ZoneId.systemDefault()).toEpochSecond())
                                .coefficient(sprint.getCoefficient())
                                .averageCoefficient(sprintService.getAverageCoefficient(sprint)));

        return ResponseEntity.ok(new CoefficientGraphDto()
                .points(points.collect(Collectors.toList())));
    }
}
