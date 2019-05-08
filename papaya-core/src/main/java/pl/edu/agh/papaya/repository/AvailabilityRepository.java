package pl.edu.agh.papaya.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Availability;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.UserInProject;

@Repository
public interface AvailabilityRepository extends CrudRepository<Availability, Long> {

    Optional<Availability> findByUserInProjectAndSprint(UserInProject userInProject, Sprint sprint);

    List<Availability> findBySprintId(Long sprintId);

    Optional<Availability> findBySprintIdAndUserInProjectUserId(Long sprintId, Long userId);
}
