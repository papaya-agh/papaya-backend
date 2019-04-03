package pl.edu.agh.model;

import java.time.Duration;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Transient;

@Entity
public class Sprint {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private long id;

    // TODO: hook up reference
    @Transient
    private Project project;


    private LocalDateTimeRange date;
    private LocalDateTimeRange enrollmentDate;

    private boolean closed;

    private Duration timeWorked;

    // TODO: hook up reference
    @Transient
    private List<Availability> availabilities;

    private Sprint() {
    }

    public long getId() {
        return id;
    }

    private boolean isClosed() {
        return closed;
    }

    public Project getProject() {
        return project;
    }

    public LocalDateTimeRange getDate() {
        return date;
    }

    public LocalDateTimeRange getEnrollmentDate() {
        return enrollmentDate;
    }

    private void setClosed(boolean closed) {
        this.closed = closed;
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void setDate(LocalDateTimeRange date) {
        this.date = date;
    }

    public void setEnrollmentDate(LocalDateTimeRange enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }
}
