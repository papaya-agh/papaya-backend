package pl.edu.agh.papaya.notification;

import pl.edu.agh.papaya.model.Project;

public enum NotificationStrategy {
    WEBHOOK,
    EMAIL;

    public static NotificationStrategy resolveNotificationStrategy(Project project) {
        return project.getWebHook() == null ? NotificationStrategy.EMAIL : NotificationStrategy.WEBHOOK;
    }
}
