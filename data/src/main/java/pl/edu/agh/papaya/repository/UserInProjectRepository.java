package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import pl.edu.agh.papaya.model.UserInProject;

public interface UserInProjectRepository extends CrudRepository<UserInProject, Long> {

}
