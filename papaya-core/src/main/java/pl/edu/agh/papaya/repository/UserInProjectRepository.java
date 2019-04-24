package pl.edu.agh.papaya.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.User;
import pl.edu.agh.papaya.model.UserInProject;

@Repository
public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

    List<UserInProject> findByProject(Project project);

    @Query("select u from UserInProject u where u.project.id = ?1 and u.user.id = ?2")
    Optional<UserInProject> findByProjectIdAndUserId(Long projectId, Long userId);

    @Query("delete from UserInProject where project = :project and user = :user")
    void removeUserFromProject(@Param("project") Project project, @Param("user") User user);
}
