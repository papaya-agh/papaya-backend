package pl.edu.agh.papaya.service.sprint;

import java.util.Objects;
import pl.edu.agh.papaya.model.LocalDateTimePeriod;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.util.AssertionUtil;

@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class SprintCreationWizard {

    private final SprintService sprintService;

    private String name;
    private String notificationMessage;
    private Project project;
    private LocalDateTimePeriod enrollmentPeriod;
    private LocalDateTimePeriod durationPeriod;

    SprintCreationWizard(SprintService sprintService) {
        this.sprintService = Objects.requireNonNull(sprintService);
    }

    public SprintCreationWizard withName(String name) {
        this.name = name;
        return this;
    }

    public SprintCreationWizard withNotificationMessage(String notificationMessage) {
        this.notificationMessage = notificationMessage;
        return this;
    }

    public SprintCreationWizard withProject(Project project) {
        this.project = Objects.requireNonNull(project);
        return this;
    }

    public SprintCreationWizard withEnrollmentPeriod(LocalDateTimePeriod enrollmentPeriod) {
        this.enrollmentPeriod = Objects.requireNonNull(enrollmentPeriod);
        return this;
    }

    public SprintCreationWizard withDurationPeriod(LocalDateTimePeriod durationPeriod) {
        this.durationPeriod = Objects.requireNonNull(durationPeriod);
        return this;
    }

    public Sprint create() {
        Sprint sprint = new Sprint();

        if (enrollmentPeriod.isAfter(durationPeriod)) {
            throw new IllegalStateException("Enrollment period must be before duration period");
        }

        sprint.setName(name);
        sprint.setNotificationMessage(notificationMessage);
        sprint.setProject(AssertionUtil.require("project", project));
        sprint.setDurationPeriod(AssertionUtil.require("durationPeriod", durationPeriod));
        sprint.setEnrollmentPeriod(AssertionUtil.require("enrollmentPeriod", enrollmentPeriod));
        sprint.setDateClosed(null);

        return sprintService.createSprint(sprint);
    }
}
