package pl.edu.agh.papaya.tests.projects;

import java.util.List;
import org.concordion.api.FullOGNL;
import org.springframework.beans.factory.annotation.Autowired;
import pl.edu.agh.papaya.api.model.ProjectDto;
import pl.edu.agh.papaya.rest.projects.controller.ProjectsController;
import pl.edu.agh.papaya.tests.util.ConcordionSpringTestBase;

@FullOGNL
public class ProjectsFixture extends ConcordionSpringTestBase {

    @Autowired
    private ProjectsController projectsController;

    public ProjectDto createExampleProject() {
        return projectsController.addProject(new ProjectDto()
                .name("Example Project")
                .description("Example Project Description")
                .initialCoefficient(0.7)).getBody();
    }

    public ProjectDto getProjectById(Long id) {
        return projectsController.getProjectById(id).getBody();
    }

    public List<ProjectDto> getProjects() {
        return projectsController.getProjects().getBody();
    }
}
