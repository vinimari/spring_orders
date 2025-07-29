package vs_fundos.challenge.service.notification;

import org.springframework.stereotype.Service;
import vs_fundos.challenge.enums.NotificationType;

@Service
public class SmsNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("[SMS] - " + message);
    }

    @Override
    public NotificationType getStrategy() {
        return NotificationType.SMS;
    }
}
