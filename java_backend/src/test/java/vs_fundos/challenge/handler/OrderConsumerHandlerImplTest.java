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
    private String testMessage;

    @BeforeEach
    void setUp() {
        testMessage = "{\"orderNumber\":\"ORDER-123\"}";
        mockOrderDTO = OrderDTO.builder().orderNumber("ORDER-123").build();
    }

    @Test
    void shouldProcessMessageSuccessfully() throws Exception {
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
        doNothing().when(orderProcessingService).processOrder("ORDER-123");
        doNothing().when(notificationDispatcherService).dispatch(any(), any());

        orderConsumerHandler.handleMessage(testMessage);

        verify(objectMapper, times(1)).readValue(testMessage, OrderDTO.class);
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }

    @Test
    void shouldThrowJsonConvertionExceptionOnInvalidJson() throws Exception {
        String invalidJsonMessage = "{invalid json";
        when(objectMapper.readValue(invalidJsonMessage, OrderDTO.class)).thenThrow(new JsonProcessingException("JSON parsing error"){});

        JsonConvertionException thrown = assertThrows(JsonConvertionException.class, () -> {
            orderConsumerHandler.handleMessage(invalidJsonMessage);
        });

        assertTrue(thrown.getMessage().contains("Error converting JSON to DTO:"));
        assertInstanceOf(JsonProcessingException.class, thrown.getCause());
        verify(orderProcessingService, never()).processOrder(anyString());
    }

    @Test
    void shouldThrowKafkaProcessingExceptionOnOrderNotFound() throws Exception {
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
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
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
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
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
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
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
        doThrow(new RuntimeException("Unexpected database connection failure")).when(orderProcessingService).processOrder("ORDER-123");

        KafkaProcessingException thrown = assertThrows(KafkaProcessingException.class, () -> {
            orderConsumerHandler.handleMessage(testMessage);
        });

        assertTrue(thrown.getMessage().contains("An unexpected error occurred during processing."));
        assertInstanceOf(RuntimeException.class, thrown.getCause());
        verify(orderProcessingService, times(1)).processOrder("ORDER-123");
    }
}
