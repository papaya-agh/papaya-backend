package pl.edu.agh.papaya.notification.message;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.notification.NotificationMessageException;

public interface Message {

    String QUOTATION_MARK = "\"";

    DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");

    void send() throws NotificationMessageException;

    @SuppressWarnings({"MethodLength"})
    default String getMessage() {
        String sprintName = getSprintName();
        String formattedSprintName = sprintName == null ? "" : QUOTATION_MARK + sprintName + QUOTATION_MARK + " ";

        final StringBuilder notificationBase = new StringBuilder(
                String.format(
                        "Deklaracje dostępności w projekcie %s dla nadchodzącego sprintu %s(%s-%s) ",
                        getProjectName(),
                        formattedSprintName,
                        getDurationStart().format(DATE_TIME_FORMATTER),
                        getDurationEnd().format(DATE_TIME_FORMATTER)
                ));

        switch (getNotificationType()) {
            case SPRINT_ENROLLMENT_OPENED:
                notificationBase.append("zostały otwarte. Termin upływa w dniu ");
                break;
            case SPRINT_ENROLLMENT_REMINDER:
                notificationBase.append("zostaną zamknięte w dniu ");
                break;
            case SPRINT_ENROLLMENT_CLOSED:
                notificationBase.append("zostały zamknięte w dniu ");
                break;
            default:
                break;
        }
        notificationBase.append(getEnrollmentEnd().format(DATE_TIME_FORMATTER));
        notificationBase.append(".");

        String sprintNotificationMessage = getSprintNotificationMessage();
        if (sprintNotificationMessage != null) {
            notificationBase.append("\n\n");
            notificationBase.append(
                    String.format(
                            "Informacja od team leadera:%n%s",
                            sprintNotificationMessage
                    ));
        }

        return notificationBase.toString();
    }

    LocalDateTime getDurationEnd();

    LocalDateTime getEnrollmentEnd();

    LocalDateTime getDurationStart();

    String getProjectName();

    String getSprintName();

    String getSprintNotificationMessage();

    NotificationType getNotificationType();
}
