package pl.edu.agh.papaya.notification.message;

import com.google.gson.JsonObject;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import pl.edu.agh.papaya.model.NotificationType;
import pl.edu.agh.papaya.model.Sprint;
import pl.edu.agh.papaya.notification.NotificationMessageException;

@Getter
@RequiredArgsConstructor
public class WebhookMessage implements Message {

    private final Sprint sprint;
    private final NotificationType notificationType;

    public void send() throws NotificationMessageException {
        var closeableHttpClient = HttpClients.createDefault();
        var httpPost = new HttpPost(sprint.getProject().getWebHook());
        var jsonObject = new JsonObject();
        var channelName = sprint.getProject().getChannelName();
        if (channelName != null) {
            jsonObject.addProperty("channel", channelName);
        }
        jsonObject.addProperty("text", getMessage());
        try {
            var entity = new StringEntity(jsonObject.toString(), StandardCharsets.UTF_8);
            httpPost.setEntity(entity);
            httpPost.setHeader("Content-type", "application/json");
            closeableHttpClient.execute(httpPost);
            closeableHttpClient.close();
        } catch (IOException e) {
            throw new NotificationMessageException("Webhook message error", e);
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
        return sprint.getDurationPeriod().getStart();
    }

    @Override
    public String getProjectName() {
        return sprint.getProject().getName();
    }
}
