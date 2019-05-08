package pl.edu.agh.papaya.rest.projects.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.model.SprintSummaryDto;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.rest.projects.service.AvailabilityRestService;
import pl.edu.agh.papaya.rest.projects.service.ProjectsRestService;
import pl.edu.agh.papaya.rest.projects.service.SprintsRestService;
import pl.edu.agh.papaya.util.BadRequestException;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "ClassFanOutComplexity"})
@RestController
@RequiredArgsConstructor
public class ProjectsRestController implements ProjectsApi {

    private final ProjectsRestService projectsRestService;

    private final AvailabilityRestService availabilityRestService;

    private final SprintsRestService sprintsRestService;

    @Override
    public ResponseEntity<ProjectDto> addProject(ProjectDto projectDto) {
        return projectsRestService.addProject(projectDto);
    }

    @Override
    public ResponseEntity<ProjectMemberDto> addUserToProject(UserIdentificationDto userIdentification, Long projectId) {
        return projectsRestService.addUserToProject(userIdentification, projectId);
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
    public ResponseEntity<Void> removeUserFromProject(Long projectId, Long userId) {
        return projectsRestService.removeUserFromProject(projectId, userId);
    }

    @Override
    public ResponseEntity<Void> setUserRole(ProjectMemberDto projectMember, Long projectId, Long userId) {
        if (projectMember.getUser() != null) {
            throw new BadRequestException();
        }

        UserRoleDto userRoleDto = projectMember.getRole();
        if (userRoleDto == null) {
            throw new BadRequestException();
        }
        projectsRestService.setUserRole(userRoleDto, projectId, userId);
        return ResponseEntity.noContent().build();
    }

    @Override
    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, List<SprintStateDto> sprintStateDtos) {
        return sprintsRestService.getSprints(projectId, sprintStateDtos);
    }

    @Override
    public ResponseEntity<AvailabilityDto> getUserAvailability(Long projectId, Long sprintId) {
        return availabilityRestService.getUserAvailability(projectId, sprintId);
    }

    @Override
    public ResponseEntity<AvailabilityDto> updateUserAvailability(AvailabilityDto body, Long projectId, Long sprintId) {
        return availabilityRestService.updateUserAvailability(body, projectId, sprintId);
    }

    @Override
    public ResponseEntity<SprintDto> addSprint(SprintDto sprintDto, Long projectId) {
        return sprintsRestService.addSprint(sprintDto, projectId);
    }

    @Override
    public ResponseEntity<SprintDto> modifySprint(SprintDto sprintDto, Long projectId, Long sprintId) {
        return sprintsRestService.modifySprint(sprintDto, projectId, sprintId);
    }

    public ResponseEntity<List<ProjectMemberDto>> getUsersFromProject(Long projectId) {
        return projectsRestService.getUsersFromProject(projectId);
    }

    @Override
    public ResponseEntity<SprintSummaryDto> getSprintSummary(Long projectId, Long sprintId) {
        return sprintsRestService.getSprintSummary(projectId, sprintId);
    }
}
