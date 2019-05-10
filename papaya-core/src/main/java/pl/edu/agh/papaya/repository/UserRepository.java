package pl.edu.agh.papaya.repository;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.User;

@Repository
public interface UserRepository extends CrudRepository<User, Long> {

    Optional<User> findByEmail(String username);

    default Optional<User> findById(String userId) {
        long id;
        try {
            id = Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return Optional.empty();
        }

        return findById(id);
    }
}
