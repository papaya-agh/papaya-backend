package pl.edu.agh.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Project extends BaseEntity {

    @Column(length = 64)
    private String name;

    @OneToMany(mappedBy = "project")
    private List<UserInProject> usersInProject;

    @Transient //TODO add missing mapping
    private List<Sprint> sprints;

    private String description;

    private double initialCoefficient;
}
