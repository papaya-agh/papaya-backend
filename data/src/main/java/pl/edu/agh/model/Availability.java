package pl.edu.agh.model;

import java.time.Duration;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Availability {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // TODO: hook up reference
    @Transient
    private UserInProject userInProject;

    @ManyToOne
    @JoinColumn(name = "sprintId")
    private Sprint sprint;

    private Duration timeAvailable;

    private Duration delayFromPreviousSprint;

    public Availability() {
    }

    public long getId() {
        return id;
    }

    private UserInProject getUserInProject() {
        return userInProject;
    }

    private void setUserInProject(UserInProject userInProject) {
        this.userInProject = userInProject;
    }

    private Sprint getSprint() {
        return sprint;
    }

    private void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    private Duration getTimeAvailable() {
        return timeAvailable;
    }

    private void setTimeAvailable(Duration timeAvailable) {
        this.timeAvailable = timeAvailable;
    }

    private Duration getDelayFromPreviousSprint() {
        return delayFromPreviousSprint;
    }

    private void setDelayFromPreviousSprint(Duration delayFromPreviousSprint) {
        this.delayFromPreviousSprint = delayFromPreviousSprint;
    }
}
