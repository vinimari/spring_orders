package vs_fundos.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.enums.NotificationType;
import vs_fundos.challenge.exception.StrategyNotFoundException;
import vs_fundos.challenge.service.notification.NotificationService;
import vs_fundos.challenge.service.notification.impl.NotificationDispatcherServiceImpl;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class NotificationDispatcherServiceImplTest {
    @Mock
    private NotificationService emailServiceMock;

    @Mock
    private NotificationService smsServiceMock;

    private NotificationDispatcherServiceImpl notificationDispatcher;

    @BeforeEach
    void setUp() {
        when(emailServiceMock.getStrategy()).thenReturn(NotificationType.EMAIL);
        when(smsServiceMock.getStrategy()).thenReturn(NotificationType.SMS);

        List<NotificationService> services = List.of(emailServiceMock, smsServiceMock);
        notificationDispatcher = new NotificationDispatcherServiceImpl(services);
    }

    @Test
    void dispatch_whenTypeIsAll_shouldCallAllServices() {
        String message = "Mensagem para todos";

        notificationDispatcher.dispatch(NotificationType.ALL, message);

        verify(emailServiceMock, times(1)).send(message);
        verify(smsServiceMock, times(1)).send(message);
    }

        @Test
        void dispatch_whenTypeIsEmail_shouldCallOnlyEmailService() {
            String message = "Mensagem de e-mail";

            notificationDispatcher.dispatch(NotificationType.EMAIL, message);

            verify(emailServiceMock, times(1)).send(message);
            verify(smsServiceMock, never()).send(anyString());
        }

        @Test
        void dispatch_whenTypeIsSms_shouldCallOnlySmsService() {
            String message = "Mensagem de SMS";

            notificationDispatcher.dispatch(NotificationType.SMS, message);

            verify(smsServiceMock, times(1)).send(message);
            verify(emailServiceMock, never()).send(anyString());
        }

        @Test
        void dispatch_whenStrategyNotFound_shouldThrowException() {
            List<NotificationService> incompleteServices = List.of(emailServiceMock);
            NotificationDispatcherServiceImpl dispatcherWithMissingStrategy = new NotificationDispatcherServiceImpl(incompleteServices);
            String message = "Esta mensagem não deve ser enviada";

            assertThrows(StrategyNotFoundException.class, () -> {
                dispatcherWithMissingStrategy.dispatch(NotificationType.SMS, message);
            });

            verify(emailServiceMock, never()).send(anyString());
            verify(smsServiceMock, never()).send(anyString());
        }
    }
