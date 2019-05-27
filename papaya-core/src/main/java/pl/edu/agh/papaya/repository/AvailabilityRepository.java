package pl.edu.agh.papaya.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;

@Repository
public interface AvailabilityRepository extends CrudRepository<Availability, Long> {

    @Query("select a from Availability a where a.userInProject.userId = :userId and a.sprint = :sprint")
    Optional<Availability> findByUserIdAndSprint(@Param("userId") String userId, @Param("sprint") Sprint sprint);

    List<Availability> findBySprintId(Long sprintId);

    Optional<Availability> findBySprintIdAndUserInProjectUserId(Long sprintId, String userId);
}
