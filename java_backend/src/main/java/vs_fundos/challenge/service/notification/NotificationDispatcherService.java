package vs_fundos.challenge.service.notification;

import vs_fundos.challenge.enums.NotificationType;

public interface NotificationDispatcherService {
    void dispatch(NotificationType type, String message);
}
