package pl.edu.agh.model;

import java.time.Duration;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
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
public class Availability {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    // TODO: hook up reference
    @Transient
    private UserInProject userInProject;

    @ManyToOne
    private Sprint sprint;

    private Duration timeAvailable;

    private Duration delayFromPreviousSprint;
}
