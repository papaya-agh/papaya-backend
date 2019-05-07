package pl.edu.agh.papaya.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import pl.edu.agh.papaya.model.Notification;
import pl.edu.agh.papaya.model.Sprint;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {

    @Query("select n from Notification n where n.sprint = :sprint order by n.lastNotificationDate asc")
    List<Notification> findSprintNotifications(@Param("sprint") Sprint sprint);
}
