package pl.edu.agh.papaya.service.availability;

import java.time.Duration;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.service.userinproject.UserInProjectService;

@RequiredArgsConstructor
@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class AvailabilityCreationWizard {

    private final AvailabilityService availabilityService;

    private final UserInProjectService userInProjectService;

    private Sprint sprint;
    private User user;
    private Duration timeAvailable;
    private Duration pastSprintRemainingTime;
    private String notes;

    public AvailabilityCreationWizard withSprint(@NonNull Sprint sprint) {
        this.sprint = sprint;
        return this;
    }

    public AvailabilityCreationWizard withUser(@NonNull User user) {
        this.user = user;
        return this;
    }

    public AvailabilityCreationWizard withTimeAvailable(@NonNull Duration timeAvailable) {
        if (timeAvailable.isNegative()) {
            throw new IllegalStateException("timeAvailable is negative");
        }
        this.timeAvailable = timeAvailable;
        return this;
    }

    public AvailabilityCreationWizard withPastSprintRemainingTime(@NonNull Duration pastSprintRemainingTime) {
        if (pastSprintRemainingTime.isNegative()) {
            throw new IllegalStateException("pastSprintRemainingTime is negative");
        }
        this.pastSprintRemainingTime = pastSprintRemainingTime;
        return this;
    }

    public AvailabilityCreationWizard withNotes(@NonNull String notes) {
        this.notes = notes;
        return this;
    }

    public Availability create() {
        UserInProject userInProject = getValidUserInProject();
        Availability availability = new Availability();
        availability.setSprint(require("sprint", sprint));
        availability.setUserInProject(require("userInProject", userInProject));
        availability.setTimeAvailable(require("timeAvailable", timeAvailable));
        availability.setPastSprintRemainingTime(require("pastSprintRemainingTime", pastSprintRemainingTime));
        availability.setNotes(require("notes", notes));

        availabilityService.save(availability);
        return availability;
    }

    private UserInProject getValidUserInProject() {
        if (user != null && sprint != null) {
            return userInProjectService
                    .getByProjectIdAndUserId(sprint.getProject().getId(), user.getId())
                    .orElseThrow(() -> new IllegalStateException("User is not a member of the project"));
        }

        throw new IllegalStateException();
    }

    private <T> T require(String fieldName, T value) {
        if (value == null) {
            throw new IllegalStateException(fieldName + " is not initialized");
        }
        return value;
    }
}
