package pl.edu.agh.papaya.rest.projects.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.mappers.ProjectMapper;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.security.UserContext;
import pl.edu.agh.papaya.service.project.ProjectService;
import pl.edu.agh.papaya.util.ForbiddenAccessException;
import pl.edu.agh.papaya.util.ResourceNotFoundException;

@RestController
public class ProjectsController implements ProjectsApi {

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
        Project project = projectService.getProjectById(id)
                .orElseThrow(ResourceNotFoundException::new);

        if (!projectService.isUserInProject(project, userContext.getUserId())) {
            throw new ForbiddenAccessException();
        }

        return ResponseEntity.ok(projectMapper.mapToApi(project));
    }

    @Override
    public ResponseEntity<List<ProjectDto>> getProjects() {
        List<Project> projects = projectService.getUserProjects(userContext.getUserId());
        return ResponseEntity.ok(projectMapper.mapToApi(projects));
    }
}
