package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

}
