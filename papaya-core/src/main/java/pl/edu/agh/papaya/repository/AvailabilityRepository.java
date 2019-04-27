package pl.edu.agh.papaya.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Availability;

@Repository
public interface AvailabilityRepository extends CrudRepository<Availability, Long> {

    @Query("select a from Availability a join a.userInProject up where up.user.id = :userId and a.sprint.id = "
            + ":sprintId")
    Optional<Availability> findBySprintIdAndUserId(@Param("sprintId") Long sprintId, @Param("userId") Long userId);
}
