package pl.edu.agh.model;

import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

@Entity
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    @ManyToOne
    @JoinColumn(name = "sprintId")
    private Sprint sprint;

    // TODO: hook up reference
    @Transient
    private UserInProject userInProject;

    private LocalDateTime lastNotificationDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 64)
    private NotificationType type;

    public Notification() {
    }

    private long getId() {
        return id;
    }

    private Sprint getSprint() {
        return sprint;
    }

    private void setSprint(Sprint sprint) {
        this.sprint = sprint;
    }

    private UserInProject getUserInProject() {
        return userInProject;
    }

    private void setUserInProject(UserInProject userInProject) {
        this.userInProject = userInProject;
    }

    private LocalDateTime getLastNotificationDate() {
        return lastNotificationDate;
    }

    private void setLastNotificationDate(LocalDateTime lastNotificationDate) {
        this.lastNotificationDate = lastNotificationDate;
    }

    private NotificationType getType() {
        return type;
    }

    private void setType(NotificationType type) {
        this.type = type;
    }
}
