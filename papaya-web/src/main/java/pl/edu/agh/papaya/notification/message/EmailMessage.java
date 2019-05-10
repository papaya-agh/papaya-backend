package pl.edu.agh.papaya.notification.message;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.model.UserInProject;
import pl.edu.agh.papaya.notification.NotificationMessageException;

@Getter
@RequiredArgsConstructor
public class EmailMessage implements Message {

    private final JavaMailSender javaMailSender;
    private final List<UserInProject> usersInProject;
    private final Sprint sprint;
    private final NotificationType notificationType;

    @Override
    public void send() throws NotificationMessageException {
        if (!usersInProject.isEmpty()) {
            SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
            String[] emails = usersInProject.stream()
                    .map(userInProject -> userInProject.getUser().getEmail())
                    .toArray(String[]::new);
            simpleMailMessage.setBcc(emails);
            simpleMailMessage.setSubject(notificationType.getEmailSubject());
            simpleMailMessage.setText(getMessage());
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
