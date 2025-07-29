package vs_fundos.challenge.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.exception.OrderAlreadyProcessedException;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.model.Order;
import vs_fundos.challenge.repository.OrderRepository;
import vs_fundos.challenge.service.impl.OrderProcessingServiceImpl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
public class OrderProcessingServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderProcessingServiceImpl orderProcessingService;

    @Test
    void processOrder_shouldThrowOrderNotFoundException_whenOrderDoesNotExist() {
        String orderNumber = "NON-EXISTENT-ORDER";
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(null);

        OrderNotFoundException thrown = assertThrows(OrderNotFoundException.class, () -> {
            orderProcessingService.processOrder(orderNumber);
        });

        assertEquals("Order not found with number: " + orderNumber, thrown.getMessage());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void processOrder_shouldThrowOrderAlreadyProcessedException_whenOrderIsAlreadyProcessed() {
        String orderNumber = "ALREADY-PROCESSED-ORDER";
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.PROCESSED);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(order);

        OrderAlreadyProcessedException thrown = assertThrows(OrderAlreadyProcessedException.class, () -> {
            orderProcessingService.processOrder(orderNumber);
        });

        assertEquals("Order already processed: " + orderNumber, thrown.getMessage());
        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        verify(orderRepository, never()).save(any(Order.class));
    }
    @Test
    void processOrder_shouldProcessOrderSuccessfully_whenStatusIsUnprocessed() {
        String orderNumber = "ORD-001";
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setStatus(OrderStatus.UNPROCESSED);
        when(orderRepository.findByOrderNumber(orderNumber)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        orderProcessingService.processOrder(orderNumber);

        verify(orderRepository, times(1)).findByOrderNumber(orderNumber);
        assertEquals(OrderStatus.PROCESSED, order.getStatus());
        verify(orderRepository, times(1)).save(order);
    }
}
