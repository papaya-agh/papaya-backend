package pl.edu.agh.papaya.notification.message;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.notification.NotificationMessageException;
import pl.edu.agh.papaya.security.User;

@Getter
@RequiredArgsConstructor
public class EmailMessage implements Message {

    private final JavaMailSender javaMailSender;
    private final List<User> users;
    private final Sprint sprint;
    private final NotificationType notificationType;

    @Override
    public void send() throws NotificationMessageException {
        if (!users.isEmpty()) {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            String[] emails = users.stream()
                    .map(User::getEmail)
                    .filter(Objects::nonNull)
                    .toArray(String[]::new);
            if (emails.length > 0) {
                simpleMailMessage.setBcc(emails);
                simpleMailMessage.setSubject(notificationType.getEmailSubject());
                simpleMailMessage.setText(getMessage());
            }
            try {
                javaMailSender.send(simpleMailMessage);
            } catch (MailException e) {
                throw new NotificationMessageException("Email message error", e);
            }
        }
    }

    @Override
    public LocalDateTime getDurationEnd() {
        return sprint.getDurationPeriod().getEnd();
    }

    @Override
    public LocalDateTime getEnrollmentEnd() {
        return sprint.getEnrollmentPeriod().getEnd();
    }

    @Override
    public LocalDateTime getDurationStart() {
        return sprint.getDurationPeriod().getEnd();
    }

    @Override
    public String getProjectName() {
        return sprint.getProject().getName();
    }
}
