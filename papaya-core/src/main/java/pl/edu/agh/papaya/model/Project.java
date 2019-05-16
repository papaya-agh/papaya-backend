package pl.edu.agh.papaya.model;

import java.util.List;
import java.util.Optional;
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

    private String webHook;

    private String channelName;

    public boolean isAdmin(User user) {
        return hasRole(user, UserRole.ADMIN);
    }

    public boolean hasRole(User user, UserRole role) {
        return getUserRoleInProject(user)
                .map(role::equals)
                .orElse(false);
    }

    public Optional<UserRole> getUserRoleInProject(User user) {
        return usersInProject.stream()
                .filter(up -> up.getUser().getId().equals(user.getId()))
                .findAny()
                .map(UserInProject::getUserRole);
    }
}
