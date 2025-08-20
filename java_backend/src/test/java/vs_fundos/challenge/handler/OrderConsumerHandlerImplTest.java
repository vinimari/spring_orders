package vs_fundos.challenge.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.*;
import vs_fundos.challenge.handler.impl.OrderConsumerHandlerImpl;
import vs_fundos.challenge.service.impl.OrderProcessingServiceImpl;
import vs_fundos.challenge.service.notification.NotificationDispatcherService;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderConsumerHandlerImplTest {
    @Mock
    private OrderProcessingServiceImpl orderProcessingService;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private NotificationDispatcherService notificationDispatcherService;
    @InjectMocks
    private OrderConsumerHandlerImpl orderConsumerHandler;

    private OrderDTO mockOrderDTO;
    private OrderDTO testMessage;

    @BeforeEach
    void setUp() {
        testMessage = OrderDTO.builder().orderNumber("ORDER-123").build();
        mockOrderDTO = OrderDTO.builder().orderNumber("ORDER-123").build();
    }

    @Test
    void shouldThrowKafkaProcessingExceptionOnOrderNotFound() throws Exception {
        doThrow(new OrderNotFoundException("ORDER-123")).when(orderProcessingService).processOrder("ORDER-123");

        KafkaProcessingException thrown = assertThrows(KafkaProcessingException.class, () -> {
            orderConsumerHandler.handleMessage(testMessage);
        });

        assertTrue(thrown.getMessage().contains("Failed to process order due to a business rule."));
        assertInstanceOf(OrderNotFoundException.class, thrown.getCause()); // More robust check
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }

    @Test
    void shouldThrowKafkaProcessingExceptionOnOrderAlreadyProcessed() throws Exception {
        doThrow(new OrderAlreadyProcessedException("ORDER-123")).when(orderProcessingService).processOrder("ORDER-123");

        KafkaProcessingException thrown = assertThrows(KafkaProcessingException.class, () -> {
            orderConsumerHandler.handleMessage(testMessage);
        });

        assertTrue(thrown.getMessage().contains("Failed to process order due to a business rule."));
        assertInstanceOf(OrderAlreadyProcessedException.class, thrown.getCause());
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }

    @Test
    void shouldThrowKafkaProcessingExceptionOnOrderProcessingError() throws Exception {
        doThrow(new OrderProcessingException("DB error", new RuntimeException())).when(orderProcessingService).processOrder("ORDER-123");

        KafkaProcessingException thrown = assertThrows(KafkaProcessingException.class, () -> {
            orderConsumerHandler.handleMessage(testMessage);
        });
        System.out.println("Mensagem: " + thrown.getMessage());
        assertTrue(thrown.getMessage().contains("Failed to process order due to a business rule."));
        assertInstanceOf(OrderProcessingException.class, thrown.getCause());
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }

    @Test
    void shouldThrowKafkaProcessingExceptionOnUnexpectedError() throws Exception {
        doThrow(new RuntimeException("Unexpected database connection failure")).when(orderProcessingService).processOrder("ORDER-123");

        KafkaProcessingException thrown = assertThrows(KafkaProcessingException.class, () -> {
            orderConsumerHandler.handleMessage(testMessage);
        });

        assertTrue(thrown.getMessage().contains("An unexpected error occurred during processing."));
        assertInstanceOf(RuntimeException.class, thrown.getCause());
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }
}
