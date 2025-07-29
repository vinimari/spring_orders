package vs_fundos.challenge.service.notification.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.enums.NotificationType;
import vs_fundos.challenge.exception.StrategyNotFoundException;
import vs_fundos.challenge.service.notification.NotificationDispatcherService;
import vs_fundos.challenge.service.notification.NotificationService;

import java.util.List;

@Service
@AllArgsConstructor
public class NotificationDispatcherServiceImpl implements NotificationDispatcherService {
    private final List<NotificationService> notificationServices;

    @Override
    public void dispatch(NotificationType type, String message) {
        if(type == NotificationType.ALL) {
            notificationServices.forEach(service -> service.send(message));
        } else {
            NotificationService strategy = notificationServices.stream()
                    .filter(
                    service -> service.getStrategy() == type
                    )
                    .findFirst()
                    .orElseThrow(() -> new StrategyNotFoundException(type.toString()));
            strategy.send(message);
        }
    }
}
