package pl.edu.agh.papaya.model;

import java.time.Duration;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Availability extends BaseEntity {

    @ManyToOne
    @JoinColumn(nullable = false)
    private UserInProject userInProject;

    @ManyToOne
    @JoinColumn(nullable = false)
    private Sprint sprint;

    private Duration timeAvailable;

    private Duration pastSprintRemainingTime;

    private String notes;
}
