package pl.edu.agh.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class UserInProject extends BaseEntity {

    @ManyToOne
    private Project project;

    @ManyToOne
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(length = 8)
    private UserRole userRole;
}
