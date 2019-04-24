package pl.edu.agh.papaya.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
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

    @OneToMany(mappedBy = "project")
    private List<Sprint> sprints;

    private String description;

    private double initialCoefficient;

    public boolean hasRole(User user, UserRole role) {
        return usersInProject.stream()
                .filter(up -> up.getUser().equals(user))
                .findAny()
                .map(UserInProject::getUserRole)
                .map(role::equals)
                .orElse(false);
    }
}
