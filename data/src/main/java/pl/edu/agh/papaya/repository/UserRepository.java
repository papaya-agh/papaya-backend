package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import pl.edu.agh.papaya.model.User;

public interface UserRepository extends CrudRepository<User, Long> {

}
