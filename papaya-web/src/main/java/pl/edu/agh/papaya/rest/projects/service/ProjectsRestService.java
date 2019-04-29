package pl.edu.agh.papaya.rest.projects.service;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.model.UserInProjectDto;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.mappers.AvailabilityMapper;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.mappers.UserInProjectMapper;
import pl.edu.agh.papaya.mappers.UserMapper;
import pl.edu.agh.papaya.mappers.UserRoleMapper;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;
import pl.edu.agh.papaya.rest.common.UserIdentificationService;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.security.UserNotAuthorizedException;
import pl.edu.agh.papaya.service.availability.AvailabilityService;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.service.user.UserService;
import pl.edu.agh.papaya.util.BadRequestException;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@Service
@SuppressWarnings("checkstyle:ClassFanOutComplexity") // TODO refactor
@RequiredArgsConstructor
public class ProjectsRestService {

    private final SprintService sprintService;

    private final SprintsRestService sprintsRestService;

    private final UserContext userContext;

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    private final UserService userService;

    private final UserIdentificationService userIdentificationService;

    private final AvailabilityService availabilityService;

    private final AvailabilityMapper availabilityMapper;

    private final UserRoleMapper userRoleMapper;

    private final UserInProjectMapper userInProjectMapper;

    private final UserMapper userMapper;

    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        Project created = projectService.newProject()
                .withName(projectDto.getName())
                .withDescription(projectDto.getDescription())
                .withInitialCoefficient(projectDto.getInitialCoefficient())
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

        return ResponseEntity.ok(new UserInProjectDto()
                .userId(userId)
                .role(userInProject.getRole()));
    }

    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, @Valid List<SprintStateDto> sprintStateDtos) {
        Project project = getValidProject(projectId);

        return sprintsRestService.getSprints(project, sprintStateDtos);
    }

    public ResponseEntity<SprintDto> addSprint(@Valid SprintDto sprintDto, Long projectId) {
        Project project = getValidProject(projectId);

        if (!projectService.isAdmin(project, userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        return sprintsRestService.addSprint(project, sprintDto);
    }

    public ResponseEntity<SprintDto> modifySprint(@Valid SprintDto sprintDto, Long projectId, Long sprintId) {
        Project project = getValidProject(projectId);

        if (!projectService.isAdmin(project, userContext.getUser())) {
            throw new ForbiddenAccessException();
        }

        return sprintsRestService.modifySprint(sprintId, sprintDto);
    }

    public ResponseEntity<AvailabilityDto> getUserAvailability(Long projectId, Long sprintId) {
        Project project = getValidProject(projectId);
        Sprint sprint = getValidSprint(sprintId);

        if (!isSprintInProject(sprint, project)) {
            throw new ForbiddenAccessException();
        }

        AvailabilityDto availability = availabilityService
                .getBySprintIdAndUserId(sprintId, userContext.getUserIdn())
                .map(availabilityMapper::mapToApi)
                .orElseGet(() -> new AvailabilityDto().timeAvailable(0L).timeRemaining(0L).notes(""));

        return ResponseEntity.ok(availability);
    }

    private Sprint getValidSprint(Long sprintId) {
        return sprintService.getById(sprintId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private boolean isSprintInProject(Sprint sprint, Project project) {
        return sprint.getProject().getId().equals(project.getId());
    }

    public ResponseEntity<AvailabilityDto> updateUserAvailability(AvailabilityDto availabilityDto,
            Long projectId, Long sprintId) {
        Project project = getValidProject(projectId);
        Sprint sprint = getValidSprint(sprintId);
        if (!isSprintInProject(sprint, project) || isAvailabilityDeclarationNotAllowed(sprint)) {
            throw new ForbiddenAccessException();
        }

        if (availabilityDto.getTimeAvailable() < 0 || availabilityDto.getTimeRemaining() < 0) {
            throw new ForbiddenAccessException();
        }

        Availability availability = availabilityService.getBySprintIdAndUserId(sprintId, userContext.getUserIdn())
                .map(persistedAvailability -> updatePersistedAvailability(persistedAvailability, availabilityDto))
                .orElseGet(() -> createAvailability(availabilityDto, sprint));

        return ResponseEntity.ok(availabilityMapper.mapToApi(availability));
    }

    private boolean isAvailabilityDeclarationNotAllowed(Sprint sprint) {
        return sprint.getSprintState() != SprintState.DECLARABLE;
    }

    private Availability createAvailability(AvailabilityDto availabilityDto, Sprint sprint) {
        return availabilityService.newAvailability()
                .withSprint(sprint)
                .withUser(userContext.getUser())
                .withTimeAvailable(Duration.ofMinutes(availabilityDto.getTimeAvailable()))
                .withPastSprintRemainingTime(Duration.ofMinutes(availabilityDto.getTimeRemaining()))
                .withNotes(Optional.ofNullable(availabilityDto.getNotes()).orElse(""))
                .create();
    }

    private Availability updatePersistedAvailability(Availability availability, AvailabilityDto availabilityDto) {
        if (availabilityDto.getNotes() != null) {
            availability.setNotes(availabilityDto.getNotes());
        }
        availability.setTimeAvailable(Duration.ofMinutes(availabilityDto.getTimeAvailable()));
        availability.setPastSprintRemainingTime(Duration.ofMinutes(availabilityDto.getTimeRemaining()));
        return availabilityService.save(availability);
    }
}
