package pl.edu.agh.papaya.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Project;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.util.OptionalUtil;

@Repository
public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

    List<UserInProject> findByProject(Project project);

    default Optional<UserInProject> findByProjectIdAndUserId(Long projectId, Long userId) {
        return OptionalUtil.fromCollection(findByProjectIdAndUserId(projectId, userId, PageRequest.of(0, 1)));
    }

    @Query("select u from UserInProject u where u.project.id = ?1 and u.user.id = ?2")
    List<UserInProject> findByProjectIdAndUserId(Long projectId, Long userId, Pageable limit);
}
