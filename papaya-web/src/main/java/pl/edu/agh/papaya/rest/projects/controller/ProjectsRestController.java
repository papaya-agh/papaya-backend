package pl.edu.agh.papaya.rest.projects.controller;

import java.util.List;
import javax.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.model.UserInProjectDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.rest.projects.service.ProjectsRestService;
import pl.edu.agh.papaya.rest.projects.service.SprintsRestService;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "ClassFanOutComplexity"})
@RestController
@RequiredArgsConstructor
public class ProjectsRestController implements ProjectsApi {

    private final ProjectsRestService projectsRestService;

    private final SprintsRestService sprintsRestService;

    @Override
    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        return projectsRestService.addProject(projectDto);
    }

    @Override
    public ResponseEntity<Void> addUserToProject(UserInProjectDto userInProject, Long projectId) {
        return projectsRestService.addUserToProject(userInProject, projectId);
    }

    @Override
    public ResponseEntity<ProjectDto> getProjectById(Long id) {
        return projectsRestService.getProjectById(id);
    }

    @Override
    public ResponseEntity<List<ProjectDto>> getProjects() {
        return projectsRestService.getProjects();
    }

    @Override
    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, @Valid List<SprintStateDto> sprintStateDtos) {
        return projectsRestService.getSprints(projectId, sprintStateDtos);
    }

    @Override
    public ResponseEntity<SprintDto> addSprint(@Valid SprintDto sprintDto, Long projectId) {
        return projectsRestService.addSprint(sprintDto, projectId);
    }

    @Override
    public ResponseEntity<SprintDto> modifySprint(@Valid SprintDto sprintDto, Long projectId, Long sprintId) {
        return projectsRestService.modifySprint(sprintDto, projectId, sprintId);
    }

    @Override
    public ResponseEntity<AvailabilityDto> getUserAvailability(Long projectId, Long sprintId) {
        return projectsRestService.getUserAvailability(projectId, sprintId);
    }

    @Override
    public ResponseEntity<AvailabilityDto> updateUserAvailability(@Valid AvailabilityDto availabilityDto,
            Long projectId, Long sprintId) {
        return projectsRestService.updateUserAvailability(availabilityDto, projectId, sprintId);

        public ResponseEntity<List<UserInProjectDto>> getUsersFromProject (Long projectId){
            return projectsRestService.getUsersFromProject(projectId);
        }

        @Override
        public ResponseEntity<Void> removeUser (Long projectId, String userId){
            return projectsRestService.removeUser(projectId, userId);
        }

        @Override
        public ResponseEntity<UserInProjectDto> setUserRole (UserInProjectDto userInProject, Long projectId, String
        userId){
            return projectsRestService.setUserRole(userInProject, projectId, userId);
        }

        @Override
        public ResponseEntity<List<SprintDto>> getSprints (Long
        projectId, @Valid List < SprintStateDto > sprintStateDtos){
            return sprintsRestService.getSprints(projectId, sprintStateDtos);
        }
    }
