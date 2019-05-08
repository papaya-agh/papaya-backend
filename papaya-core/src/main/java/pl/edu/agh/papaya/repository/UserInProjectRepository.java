package pl.edu.agh.papaya.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserInProject;

@Repository
public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

    List<UserInProject> findByProject(Project project);

    Optional<UserInProject> findByProjectIdAndUserId(Long projectId, Long userId);
}
