package pl.edu.agh.papaya.model;

import java.time.Duration;
import java.util.List;
import javax.persistence.Column;
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

    @Column(nullable = false)
    private boolean closed;

    private Duration timeBurned;

    private Duration timePlanned;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Availability> availabilities;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "sprint")
    private List<Notification> notifications;
}
