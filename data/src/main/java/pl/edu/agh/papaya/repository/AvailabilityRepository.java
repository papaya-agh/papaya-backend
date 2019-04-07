package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import pl.edu.agh.papaya.model.Availability;

public interface AvailabilityRepository extends CrudRepository<Availability, Long> {

}
