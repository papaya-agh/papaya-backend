package pl.edu.agh.papaya.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.model.UserRole;

@Repository
public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

    List<UserInProject> findByProject(Project project);

    Optional<UserInProject> findByProjectIdAndUserId(Long projectId, Long userId);

    @Transactional
    @Modifying
    @Query("update UserInProject set userRole = :role where project = :project and user = :user")
    void updateUserRole(@Param("project") Project project, @Param("user") User user, @Param("role") UserRole role);
}
