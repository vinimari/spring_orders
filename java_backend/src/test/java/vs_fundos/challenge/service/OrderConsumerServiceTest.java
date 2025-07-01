package vs_fundos.challenge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.OrderAlreadyProcessedException;
import vs_fundos.challenge.exception.OrderNotFoundException;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderConsumerServiceTest {
    @Mock
    private OrderService orderService;
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private OrderConsumerService orderConsumerService;

    @Test
    void shouldListenAndProcessMessageSuccessfully() throws Exception {
        String testMessage = "{\"orderNumber\":\"ORDER-123\"}";
        OrderDTO mockOrderDTO = OrderDTO.builder().build();
        mockOrderDTO.setOrderNumber("ORDER-123");
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);

        orderConsumerService.listen(testMessage);

        verify(objectMapper, times(1)).readValue(testMessage, OrderDTO.class);
        verify(orderService, times(1)).processOrder("ORDER-123");
    }

    @Test
    void shouldHandleInvalidJsonMessage() throws Exception {
        String invalidJsonMessage = "{invalid json";
        when(objectMapper.readValue(invalidJsonMessage, OrderDTO.class)).thenThrow(new RuntimeException("JSON parsing error"));

        orderConsumerService.listen(invalidJsonMessage);

        verify(objectMapper, times(1)).readValue(invalidJsonMessage, OrderDTO.class);
        verify(orderService, never()).processOrder(anyString());
    }

    @Test
    void shouldHandleOrderNotFoundException() throws Exception {
        String testMessage = "{\"orderNumber\":\"ORDER-404\"}";
        OrderDTO mockOrderDTO = OrderDTO.builder().orderNumber("ORDER-404").build();
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
        doThrow(new OrderNotFoundException("ORDER-404")).when(orderService).processOrder("ORDER-404");

        orderConsumerService.listen(testMessage);

        verify(objectMapper, times(1)).readValue(testMessage, OrderDTO.class);
        verify(orderService, times(1)).processOrder("ORDER-404");
    }

    @Test
    void shouldHandleOrderAlreadyProcessedException() throws Exception {
        String testMessage = "{\"orderNumber\":\"ORDER-404\"}";
        OrderDTO mockOrderDTO = OrderDTO.builder().orderNumber("ORDER-404").build();
        when(objectMapper.readValue(testMessage, OrderDTO.class)).thenReturn(mockOrderDTO);
        doThrow(new OrderAlreadyProcessedException("ORDER-404")).when(orderService).processOrder("ORDER-404");

        orderConsumerService.listen(testMessage);

        verify(objectMapper, times(1)).readValue(testMessage, OrderDTO.class);
        verify(orderService, times(1)).processOrder("ORDER-404");
    }
}
