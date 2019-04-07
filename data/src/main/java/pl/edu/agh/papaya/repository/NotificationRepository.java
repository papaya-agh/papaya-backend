package pl.edu.agh.papaya.repository;

import org.springframework.data.repository.CrudRepository;
import pl.edu.agh.papaya.model.Notification;

public interface NotificationRepository extends CrudRepository<Notification, Long> {

}
