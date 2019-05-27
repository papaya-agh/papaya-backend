package pl.edu.agh.papaya.service.project;

import java.util.Objects;
import org.apache.commons.validator.routines.UrlValidator;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.util.AssertionUtil;

@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class ProjectCreationWizard {

    private final ProjectService projectService;

    private String name;
    private String description;
    private Double initialCoefficient;
    private String webhookUrl;
    private String channelName;
    private String jiraUrl;

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

    public ProjectCreationWizard withWebhook(String webhookUrl) {
        this.webhookUrl = new UrlValidator().isValid(webhookUrl) ? webhookUrl : null;
        return this;
    }

    public ProjectCreationWizard withChannelName(String channelName) {
        this.channelName = channelName;
        return this;
    }

    public ProjectCreationWizard withJiraUrl(String jiraUrl) {
        if (!new UrlValidator().isValid(jiraUrl)) {
            throw new IllegalArgumentException("The provided Jira url is not correct");
        }
        this.jiraUrl = jiraUrl;
        return this;
    }

    public Project create() {
        Project project = new Project();
        project.setName(AssertionUtil.require("name", name));
        project.setDescription(AssertionUtil.require("description", description));
        project.setInitialCoefficient(AssertionUtil.require("initialCoefficient", initialCoefficient));
        project.setJiraUrl(AssertionUtil.require("jiraUrl", jiraUrl));
        if (webhookUrl != null) {
            project.setWebHook(webhookUrl);
            project.setChannelName(channelName);
        }
        return projectService.createProject(project);
    }
}
