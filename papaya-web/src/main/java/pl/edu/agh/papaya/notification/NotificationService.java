package pl.edu.agh.papaya.notification;

import com.google.common.collect.Iterables;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import pl.edu.agh.papaya.model.Notification;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.SprintState;
import pl.edu.agh.papaya.notification.message.EmailMessage;
import pl.edu.agh.papaya.notification.message.Message;
import pl.edu.agh.papaya.notification.message.WebhookMessage;
import pl.edu.agh.papaya.repository.AvailabilityRepository;
import pl.edu.agh.papaya.repository.NotificationRepository;
import pl.edu.agh.papaya.security.User;
import pl.edu.agh.papaya.service.sprint.SprintService;
import pl.edu.agh.papaya.service.userinproject.UserInProjectService;

@Service
@SuppressWarnings({"PMD.BeanMembersShouldSerialize", "ClassFanOutComplexity"})
public final class NotificationService {

    private final SprintService sprintService;

    private final UserInProjectService userInProjectService;

    private final NotificationRepository notificationRepository;

    private final AvailabilityRepository availabilityRepository;

    private final JavaMailSender javaMailSender;

    @Value("${notifications.frequency.minutes:1440}")
    private transient Integer notificationFrequency;

    @Autowired
    public NotificationService(SprintService sprintService, UserInProjectService userInProjectService,
            NotificationRepository notificationRepository, AvailabilityRepository availabilityRepository,
            JavaMailSender javaMailSender) {
        this.sprintService = sprintService;
        this.userInProjectService = userInProjectService;
        this.notificationRepository = notificationRepository;
        this.availabilityRepository = availabilityRepository;
        this.javaMailSender = javaMailSender;
    }

    @Scheduled(cron = "${notifications.cron.trigger:-}")
    public void sendNotifications() {
        LocalDateTime currentTime = LocalDateTime.now();
        sendDeclarationsOpenOrReminderNotifications(currentTime);
        sendDeclarationsFinishedNotifications(currentTime);
    }

    private void sendDeclarationsFinishedNotifications(LocalDateTime currentTime) {
        List<Sprint> paddingSprints = sprintService.getByState(SprintState.PADDING, currentTime);

        paddingSprints.forEach(sprint -> {
            var project = sprint.getProject();
            var users = userInProjectService.getActiveUsersInProject(project);
            var notificationStrategy = NotificationStrategy.resolveNotificationStrategy(project);

            Optional<Notification> lastNotification = Optional.ofNullable(
                    Iterables.getLast(notificationRepository.findSprintNotifications(sprint), null));

            if (lastNotification.isPresent() && sprintClosedMessageNotYetSent(lastNotification.get())) {
                Optional<Message> message = createSprintClosedMessage(sprint, users, notificationStrategy);
                message.ifPresent(value -> sendAndUpdate(currentTime, sprint, value));
            }
        });
    }

    private void sendAndUpdate(LocalDateTime currentTime, Sprint sprint, Message message) {
        try {
            message.send();
        } catch (NotificationMessageException e) {
            return;
        }
        updateNotificationRepository(sprint, message, currentTime);
    }

    private Optional<Message> createSprintClosedMessage(Sprint sprint, List<User> users,
            NotificationStrategy notificationStrategy) {
        List<User> usersWhoMissedDeadline = getMembersWithoutDeclarations(sprint, users);
        Message message = null;
        if (!usersWhoMissedDeadline.isEmpty()) {
            if (notificationStrategy.equals(NotificationStrategy.EMAIL)) {
                message = new EmailMessage(javaMailSender, usersWhoMissedDeadline, sprint,
                        NotificationType.SPRINT_ENROLLMENT_CLOSED);
            } else {
                message = new WebhookMessage(sprint, NotificationType.SPRINT_ENROLLMENT_CLOSED);
            }
        }
        return Optional.ofNullable(message);
    }

    private boolean sprintClosedMessageNotYetSent(Notification lastNotification) {
        return !lastNotification.getType().equals(NotificationType.SPRINT_ENROLLMENT_CLOSED);
    }

    private List<User> getMembersWithoutDeclarations(Sprint sprint, List<User> users) {
        return users.stream()
                .filter(user -> availabilityRepository
                        .findByUserIdAndSprint(user.getId(), sprint)
                        .isEmpty())
                .collect(Collectors.toList());
    }

    private void updateNotificationRepository(Sprint sprint, Message message, LocalDateTime notificationTimestamp) {
        new NotificationCreationWizard(this)
                .withSprint(sprint)
                .withNotificationType(message.getNotificationType())
                .withLastNotificationDate(notificationTimestamp)
                .create();
    }

    private void sendDeclarationsOpenOrReminderNotifications(LocalDateTime currentTime) {
        var declarableSprints = sprintService.getByState(SprintState.DECLARABLE, currentTime);

        declarableSprints.forEach(sprint -> {
            var project = sprint.getProject();
            var users = userInProjectService.getActiveUsersInProject(project);
            var notificationStrategy = NotificationStrategy.resolveNotificationStrategy(project);

            Optional<Message> message =
                    createDeclarableMessage(currentTime, sprint, users, notificationStrategy);
            message.ifPresent(value -> sendAndUpdate(currentTime, sprint, value));
        });
    }

    private Optional<Message> createDeclarableMessage(LocalDateTime currentTime, Sprint sprint,
            List<User> users, NotificationStrategy notificationStrategy) {
        Notification lastNotification = Iterables.getLast(notificationRepository.findSprintNotifications(sprint), null);

        Message message = null;

        if (lastNotification == null) {
            if (notificationStrategy.equals(NotificationStrategy.EMAIL)) {
                message = new EmailMessage(javaMailSender, users, sprint,
                        NotificationType.SPRINT_ENROLLMENT_OPENED);
            } else {
                message = new WebhookMessage(sprint, NotificationType.SPRINT_ENROLLMENT_OPENED);
            }
        } else if (isNextNotificationDue(currentTime, lastNotification)) {
            message = createReminderMessage(sprint, users);
        }

        return Optional.ofNullable(message);
    }

    private boolean isNextNotificationDue(LocalDateTime currentTime, Notification lastNotification) {
        return Duration.between(lastNotification.getLastNotificationDate(), currentTime)
                .compareTo(Duration.ofMinutes(notificationFrequency)) >= 0;
    }

    private Message createReminderMessage(Sprint sprint, List<User> users) {
        List<User> usersWithoutDeclaration = getMembersWithoutDeclarations(sprint, users);
        return new EmailMessage(javaMailSender, usersWithoutDeclaration, sprint,
                NotificationType.SPRINT_ENROLLMENT_REMINDER);
    }

    Notification createNotification(Notification notification) {
        return notificationRepository.save(notification);
    }
}
