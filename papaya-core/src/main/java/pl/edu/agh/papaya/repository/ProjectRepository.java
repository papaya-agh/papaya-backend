package pl.edu.agh.papaya.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Project;

@Repository
public interface ProjectRepository extends CrudRepository<Project, Long> {

    @Query("select p from Project p join p.usersInProject up where up.user.id = :userId")
    List<Project> findByUserId(@Param("userId") Long userId);
}
