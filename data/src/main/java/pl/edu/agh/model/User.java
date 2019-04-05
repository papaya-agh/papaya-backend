package pl.edu.agh.model;

import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_")
public class User extends BaseEntity {
    @Column(length = 60)
    private String email;

    @Column(length = 30)
    private String firstName;

    @Column(length = 30)
    private String lastName;

    @OneToMany(mappedBy = "user")
    private List<UserInProject> projects;
}
