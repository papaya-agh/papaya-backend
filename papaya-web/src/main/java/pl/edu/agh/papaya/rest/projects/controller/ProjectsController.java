package pl.edu.agh.papaya.rest.projects.controller;

import com.google.common.collect.Lists;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.mappers.SprintMapper;
import pl.edu.agh.papaya.mappers.SprintStateMapper;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@RestController
public class ProjectsController implements ProjectsApi {

    private static final List<SprintStateDto> ALL_SPRINT_STATE_DTOS = Lists.newArrayList(SprintStateDto.values());

    @Autowired
    private SprintService sprintService;

    @Autowired
    private SprintStateMapper sprintStateMapper;

    @Autowired
    private SprintMapper sprintMapper;

    @Autowired
    private UserContext userContext;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private ProjectMapper projectMapper;

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

        List<SprintDto> sprints = sprintService.findByStatesInProject(sprintStates, project.getId(), currentTime)
                .stream()
                .sorted(Comparator.comparing(sprint -> sprint.getEnrollmentPeriod().getStart()))
                .map(sprint -> sprintMapper.mapToApi(sprint, currentTime))
                .collect(Collectors.toList());

        return ResponseEntity.ok(sprints);
    }
}
