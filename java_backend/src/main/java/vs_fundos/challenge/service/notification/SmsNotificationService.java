package vs_fundos.challenge.service.notification;

import vs_fundos.challenge.enums.NotificationType;

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
