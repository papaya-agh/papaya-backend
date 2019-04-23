package pl.edu.agh.papaya.model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sprint extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private Project project;

    private LocalDateTimePeriod durationPeriod;

    private LocalDateTimePeriod enrollmentPeriod;

    private LocalDateTime dateClosed;

    private Duration timeBurned;

    private Duration timePlanned;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Availability> availabilities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Notification> notifications;

    public SprintState getSprintState() {
        return getSprintState(LocalDateTime.now());
    }

    public SprintState getSprintState(LocalDateTime evaluationTime) {
        var sprintState = SprintState.CLOSED;
        if (evaluationTime.isBefore(enrollmentPeriod.getStart())) {
            sprintState = SprintState.UPCOMING;
        } else if (evaluationTime.isBefore(enrollmentPeriod.getEnd())) {
            sprintState = SprintState.DECLARABLE;
        } else if (evaluationTime.isBefore(durationPeriod.getStart())) {
            sprintState = SprintState.PADDING;
        } else if (evaluationTime.isBefore(durationPeriod.getEnd())) {
            sprintState = SprintState.IN_PROGRESS;
        } else if (dateClosed == null || evaluationTime.isBefore(dateClosed)) {
            sprintState = SprintState.FINISHED;
        }
        return sprintState;
    }
}
