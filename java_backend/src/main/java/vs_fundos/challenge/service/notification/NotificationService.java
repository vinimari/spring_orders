package vs_fundos.challenge.service.notification;

import vs_fundos.challenge.enums.NotificationType;

public interface NotificationService {
    void send(String message);
    NotificationType getStrategy();
}
