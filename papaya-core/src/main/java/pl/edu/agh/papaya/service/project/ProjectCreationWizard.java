package pl.edu.agh.papaya.service.project;

import java.util.Objects;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.util.AssertionUtil;

@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class ProjectCreationWizard {

    private final ProjectService projectService;

    private String name;
    private String description;
    private Double initialCoefficient;

    ProjectCreationWizard(ProjectService projectService) {
        this.projectService = Objects.requireNonNull(projectService);
    }

    public ProjectCreationWizard withName(String name) {
        this.name = Objects.requireNonNull(name);
        return this;
    }

    public ProjectCreationWizard withDescription(String description) {
        this.description = Objects.requireNonNull(description);
        return this;
    }

    public ProjectCreationWizard withInitialCoefficient(double initialCoefficient) {
        this.initialCoefficient = initialCoefficient;
        return this;
    }

    public Project create() {
        Project project = new Project();
        project.setName(AssertionUtil.require("name", name));
        project.setDescription(AssertionUtil.require("description", description));
        project.setInitialCoefficient(AssertionUtil.require("initialCoefficient", initialCoefficient));
        return projectService.createProject(project);
    }
}
