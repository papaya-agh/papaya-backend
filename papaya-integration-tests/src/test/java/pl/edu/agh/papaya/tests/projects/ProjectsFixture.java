package pl.edu.agh.papaya.tests.projects;

import java.util.List;
import org.concordion.api.FullOGNL;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.papaya.api.client.ApiException;
import pl.edu.agh.papaya.api.client.ApiResponse;
import pl.edu.agh.papaya.api.client.model.ProjectDto;
import pl.edu.agh.papaya.api.client.model.UserInProjectDto;
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

    public void addPeterToProject(long id) throws ApiException {
        UserInProjectDto userInProject = new UserInProjectDto()
                .userId(getUserId("peter"))
                .role(UserRoleDto.MEMBER);
        projectsApi.addUserToProject(userInProject, id);
    }
}
