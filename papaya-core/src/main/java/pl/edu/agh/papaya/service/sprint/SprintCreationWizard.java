package pl.edu.agh.papaya.service.sprint;

import java.time.Duration;
import java.util.Objects;
import pl.edu.agh.papaya.model.LocalDateTimePeriod;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.util.AssertionUtil;

@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class SprintCreationWizard {

    private final SprintService sprintService;

    private Project project;
    private LocalDateTimePeriod enrollmentPeriod;
    private LocalDateTimePeriod durationPeriod;
    private Duration timePlanned;

    SprintCreationWizard(SprintService sprintService) {
        this.sprintService = Objects.requireNonNull(sprintService);
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

    public SprintCreationWizard withTimePlanned(Duration timePlanned) {
        this.timePlanned = Objects.requireNonNull(timePlanned);
        return this;
    }

    public Sprint create() {
        Sprint sprint = new Sprint();

        if (!enrollmentPeriod.isBefore(durationPeriod)) {
            throw new IllegalStateException("enrollmentPeriod must be strictly before durationPeriod");
        }

        sprint.setProject(AssertionUtil.require("project", project));
        sprint.setDurationPeriod(AssertionUtil.require("durationPeriod", durationPeriod));
        sprint.setEnrollmentPeriod(AssertionUtil.require("enrollmentPeriod", enrollmentPeriod));
        sprint.setDateClosed(null);
        sprint.setTimeBurned(null);
        sprint.setTimePlanned(AssertionUtil.require("timePlanned", timePlanned));

        return sprintService.createSprint(sprint);
    }
}
