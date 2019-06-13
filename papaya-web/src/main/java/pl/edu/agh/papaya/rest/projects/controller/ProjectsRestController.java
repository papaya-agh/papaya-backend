package pl.edu.agh.papaya.rest.projects.controller;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import pl.edu.agh.papaya.api.model.AvailabilityDto;
import pl.edu.agh.papaya.api.model.CoefficientGraphDto;
import pl.edu.agh.papaya.api.model.JiraBoardDto;
import pl.edu.agh.papaya.api.model.JiraConfigDto;
import pl.edu.agh.papaya.api.model.JiraSprintDto;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.api.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.model.SortingDirection;
import pl.edu.agh.papaya.api.model.SprintDto;
import pl.edu.agh.papaya.api.model.SprintStateDto;
import pl.edu.agh.papaya.api.model.SprintSummaryDto;
import pl.edu.agh.papaya.api.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.model.UserRoleDto;
import pl.edu.agh.papaya.api.service.ProjectsApi;
import pl.edu.agh.papaya.rest.availability.service.AvailabilityRestService;
import pl.edu.agh.papaya.rest.projects.service.ProjectsRestService;
import pl.edu.agh.papaya.rest.sprints.service.SprintsRestService;
import pl.edu.agh.papaya.util.BadRequestException;

@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "ClassFanOutComplexity", "MethodCount"})
@RestController
@RequiredArgsConstructor
public class ProjectsRestController implements ProjectsApi {

    private final ProjectsRestService projectsRestService;

    private final AvailabilityRestService availabilityRestService;

    private final SprintsRestService sprintsRestService;

    @Override
    public ResponseEntity<CoefficientGraphDto> getCoefficientGraph(Long projectId, Long from, Long to) {
        return projectsRestService.getCoefficientGraph(
                projectId,
                localDateFromTimestamp(from),
                localDateFromTimestamp(to));
    }

    private LocalDate localDateFromTimestamp(Long from) {
        return Instant.ofEpochSecond(from).atZone(ZoneId.systemDefault()).toLocalDate();
    }

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
    public ResponseEntity<Void> removeUserFromProject(Long projectId, String userId) {
        return projectsRestService.removeUserFromProject(projectId, userId);
    }

    @Override
    public ResponseEntity<Void> setUserRole(ProjectMemberDto projectMember, Long projectId, String userId) {
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
    public ResponseEntity<List<SprintDto>> getSprints(Long projectId, List<SprintStateDto> sprintStates,
            SortingDirection sortingDirection, Long limit) {
        return sprintsRestService.getSprints(projectId, sprintStates, sortingDirection, limit);
    }

    @Override
    public ResponseEntity<SprintDto> getNextSprint(Long projectId, Long sprintId, List<SprintStateDto> sprintStates) {
        return sprintsRestService.getNextSprint(projectId, sprintId, sprintStates);
    }

    @Override
    public ResponseEntity<SprintDto> getPreviousSprint(Long projectId, Long sprintId,
            List<SprintStateDto> sprintStates) {
        return sprintsRestService.getPreviousSprint(projectId, sprintId, sprintStates);
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
    public ResponseEntity<SprintSummaryDto> getSprintSummary(Long projectId, Long sprintId, Long jiraSprintId) {
        return sprintsRestService.getSprintSummary(projectId, sprintId, jiraSprintId);
    }

    @Override
    public ResponseEntity<List<JiraSprintDto>> getAvailableJiraSprints(Long projectId, Long sprintId) {
        return sprintsRestService.getAvailableJiraSprints(projectId, sprintId);
    }

    @Override
    public ResponseEntity<JiraConfigDto> getJiraAuthorizationLink(Long projectId) {
        return projectsRestService.getJiraAuthorizationLink(projectId);
    }

    @Override
    public ResponseEntity<List<JiraBoardDto>> getJiraBoards(Long projectId) {
        return projectsRestService.getJiraBoards(projectId);
    }

    @Override
    public ResponseEntity<Void> setJiraBoard(JiraBoardDto jiraBoardDto, Long projectId) {
        return projectsRestService.setJiraBoard(jiraBoardDto, projectId);
    }

    @Override
    public ResponseEntity<Void> setJiraSecret(JiraConfigDto jiraConfigDto, Long projectId) {
        return projectsRestService.setJiraSecret(jiraConfigDto, projectId);
    }

    @Override
    public ResponseEntity<Void> removeSprint(Long projectId, Long sprintId) {
        return sprintsRestService.removeSprint(projectId, sprintId);
    }
}
