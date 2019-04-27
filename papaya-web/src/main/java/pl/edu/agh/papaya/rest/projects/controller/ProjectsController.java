package pl.edu.agh.papaya.rest.projects.controller;

import com.google.common.collect.Lists;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.mappers.AvailabilityMapper;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.mappers.SprintMapper;
import pl.edu.agh.papaya.mappers.SprintStateMapper;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.availability.AvailabilityService;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@RestController
@RequiredArgsConstructor
public class ProjectsController implements ProjectsApi {

    private static final List<SprintStateDto> ALL_SPRINT_STATE_DTOS = Lists.newArrayList(SprintStateDto.values());

    private final SprintService sprintService;

    private final SprintStateMapper sprintStateMapper;

    private final SprintMapper sprintMapper;

    private final UserContext userContext;

    private final ProjectService projectService;

    private final ProjectMapper projectMapper;

    private final AvailabilityService availabilityService;

    private final AvailabilityMapper availabilityMapper;

    @Override
    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        Project created = projectService.newProject()
                .withName(projectDto.getName())
                .withDescription(projectDto.getDescription())
                .withInitialCoefficient(projectDto.getInitialCoefficient())
                .create();
        return ResponseEntity.ok(projectMapper.mapToApi(created));
    }

    @Override
    public ResponseEntity<ProjectDto> getProjectById(Long id) {
        Project project = getValidProject(id);

        return ResponseEntity.ok(projectMapper.mapToApi(project));
    }

    private Project getValidProject(Long id) {
        Project project = projectService.getProjectById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!projectService.isUserInProject(project, userContext.getUserId())) {
            throw new ForbiddenAccessException();
        }
        return project;
    }

    @Override
    public ResponseEntity<List<ProjectDto>> getProjects() {
        List<Project> projects = projectService.getUserProjects(userContext.getUserId());
        return ResponseEntity.ok(projectMapper.mapToApi(projects));
    }

    @Override
    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, @Valid List<SprintStateDto> sprintStateDtos) {
        Project project = getValidProject(projectId);

        final LocalDateTime currentTime = LocalDateTime.now();

        List<SprintState> sprintStates = sprintStateMapper.mapFromApi(
                Optional.ofNullable(sprintStateDtos).orElse(ALL_SPRINT_STATE_DTOS));

        List<SprintDto> sprints = sprintService.getByStatesInProject(sprintStates, project.getId(), currentTime)
                .stream()
                .sorted(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()))
                .map(sprint -> sprintMapper.mapToApi(sprint, currentTime))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sprints);
    }

    @Override
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

    @Override
    public ResponseEntity<AvailabilityDto> updateUserAvailability(@Valid AvailabilityDto availabilityDto,
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

    private Sprint getValidSprint(Long sprintId) {
        return sprintService.getById(sprintId)
                .orElseThrow(ResourceNotFoundException::new);
    }

    private boolean isSprintInProject(Sprint sprint, Project project) {
        return sprint.getProject().getId().equals(project.getId());
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
