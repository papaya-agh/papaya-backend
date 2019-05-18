package pl.edu.agh.papaya.tests.projects;

import java.util.List;
import org.concordion.api.FullOGNL;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.ApiResponse;
import pl.edu.agh.papaya.api.client.model.ProjectDto;
import pl.edu.agh.papaya.api.client.model.ProjectMemberDto;
import pl.edu.agh.papaya.api.client.model.UserIdentificationDto;
import pl.edu.agh.papaya.api.client.model.UserRoleDto;
import pl.edu.agh.papaya.api.client.service.ProjectsApi;
import pl.edu.agh.papaya.tests.util.ConcordionSpringTestBase;

@FullOGNL
public class ProjectsFixture extends ConcordionSpringTestBase {

    @Autowired
    private ProjectsApi projectsApi;

    public ProjectDto createExampleProject() throws ApiException {
        return projectsApi.addProject(new ProjectDto()
                .name("Example Project")
                .description("Example Project Description")
                .initialCoefficient(0.7));
    }

    public ProjectDto getProjectById(Long id) throws ApiException {
        return projectsApi.getProjectById(id);
    }

    public ApiResponse<ProjectDto> tryGetProjectById(Long id) {
        return failSilently(() -> projectsApi.getProjectByIdWithHttpInfo(id));
    }

    public List<ProjectDto> getProjects() throws ApiException {
        return projectsApi.getProjects();
    }

    public void addToProject(long projectId, String username) throws ApiException {
        String email = getUser(username).getEmail();
        UserIdentificationDto userIdentification = new UserIdentificationDto().email(email);
        projectsApi.addUserToProject(userIdentification, projectId);
    }

    private ApiResponse<ProjectMemberDto> tryToAddUserToProject(long projectId, String email) {
        UserIdentificationDto userIdentification = new UserIdentificationDto().email(email);
        return failSilently(() -> projectsApi.addUserToProjectWithHttpInfo(userIdentification, projectId));
    }

    public ApiResponse<ProjectMemberDto> tryToAddExistingUserToProject(long projectId, String username) {
        return tryToAddUserToProject(projectId, getUser(username).getEmail());
    }

    public ApiResponse<ProjectMemberDto> tryToAddNonExistentUserToProject(long projectId) {
        return tryToAddUserToProject(projectId, "i.do.not.exist@example.com");
    }

    public void removeFromProject(long projectId, String username) throws ApiException {
        projectsApi.removeUserFromProject(projectId, getUserId(username));
    }

    public ApiResponse<Void> tryRemoveFromProject(long projectId, String username) {
        return failSilently(() -> projectsApi.removeUserFromProjectWithHttpInfo(projectId, getUserId(username)));
    }

    public ApiResponse<Void> trySetRoleInProject(long projectId, String username, String newRole) {
        ProjectMemberDto pm = new ProjectMemberDto().role(UserRoleDto.valueOf(newRole));
        return failSilently(() -> projectsApi.setUserRoleWithHttpInfo(pm, projectId, getUserId(username)));
    }
}
