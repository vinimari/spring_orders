package vs_fundos.challenge.service.notification;

import vs_fundos.challenge.enums.NotificationType;

public class EmailNotificationService implements NotificationService {
    @Override
    public void send(String message) {
        System.out.println("[Email] - " + message);
    }

    @Override
    public NotificationType getStrategy() {
        return NotificationType.EMAIL;
    }
}
