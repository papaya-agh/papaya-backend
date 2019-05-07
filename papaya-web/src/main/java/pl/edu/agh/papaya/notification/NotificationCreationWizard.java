package pl.edu.agh.papaya.notification;

import java.time.LocalDateTime;
import java.util.Objects;
import pl.edu.agh.papaya.model.Notification;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.util.AssertionUtil;

@SuppressWarnings({"checkstyle:HiddenField", "PMD.BeanMembersShouldSerialize"})
public class NotificationCreationWizard {

    private final NotificationService notificationService;

    private Sprint sprint;
    private LocalDateTime lastNotificationDate;
    private NotificationType notificationType;

    NotificationCreationWizard(NotificationService notificationService) {
        this.notificationService = Objects.requireNonNull(notificationService);
    }

    public NotificationCreationWizard withSprint(Sprint sprint) {
        this.sprint = Objects.requireNonNull(sprint);
        return this;
    }

    public NotificationCreationWizard withLastNotificationDate(LocalDateTime lastNotificationDate) {
        this.lastNotificationDate = Objects.requireNonNull(lastNotificationDate);
        return this;
    }

    public NotificationCreationWizard withNotificationType(NotificationType notificationType) {
        this.notificationType = notificationType;
        return this;
    }

    public Notification create() {
        Notification notification = new Notification();
        notification.setSprint(AssertionUtil.require("sprint", sprint));
        notification.setType(AssertionUtil.require("notificationType", notificationType));
        notification.setLastNotificationDate(AssertionUtil.require("lastNotificationDate", lastNotificationDate));
        return notificationService.createNotification(notification);
    }
}
