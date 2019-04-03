package pl.edu.agh.model;

import java.time.Duration;
import java.util.List;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Sprint {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // TODO: hook up reference
    @Transient
    private Project project;

    private LocalDateTimeRange date;
    private LocalDateTimeRange enrollmentDate;

    private boolean closed;

    private Duration timeWorked;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Availability> availabilities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Notification> notifications;
}
