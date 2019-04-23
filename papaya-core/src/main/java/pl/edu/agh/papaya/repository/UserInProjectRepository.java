package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.UserInProject;

@Repository
public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

}
