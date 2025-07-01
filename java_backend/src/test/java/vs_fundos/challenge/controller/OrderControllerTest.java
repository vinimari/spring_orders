package vs_fundos.challenge.controller;

import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.service.OrderService;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {
    @Mock
    private OrderService orderService;
    @InjectMocks
    private OrderController orderController;

    @Test
    void createOrder_shouldReturnCreatedStatusAndOrderDTO_whenServiceSucceeds() {
        OrderDTO expectedOrderDTO = OrderDTO.builder().orderNumber("ORDER-1").build();
        when(orderService.createOrder(expectedOrderDTO)).thenReturn(expectedOrderDTO);

        ResponseEntity<OrderDTO> responseEntity = orderController.createOrder(expectedOrderDTO);

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(expectedOrderDTO, responseEntity.getBody());
        verify(orderService, times(1)).createOrder(expectedOrderDTO);
    }

    @Test
    void createOrder_shouldPropagateRuntimeException_whenServiceThrowsGenericError() {
        OrderDTO inputOrderDTO = OrderDTO.builder()
                .orderNumber("FAIL-ORDER-1")
                .build();

        String errorMessage = "Simulated database error during order creation";
        doThrow(new RuntimeException(errorMessage)).when(orderService).createOrder(any(OrderDTO.class));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderController.createOrder(inputOrderDTO);
        });

        assertEquals(errorMessage, thrown.getMessage());
        verify(orderService, times(1)).createOrder(inputOrderDTO);
    }

    @Test
    void updateOrder_shouldReturnOkStatusAndOrderDTO_whenServiceSucceeds() {
        Long orderId = 1L;
        OrderDTO orderDetails = OrderDTO.builder()
                .totalValue(BigDecimal.valueOf(0))
                .status(OrderStatus.PROCESSED)
                .orderDateUpdated(LocalDateTime.MIN)
                .build();
        OrderDTO updatedOrder = OrderDTO.builder()
                .totalValue(BigDecimal.valueOf(0))
                .status(OrderStatus.PROCESSED)
                .orderDateUpdated(LocalDateTime.MIN)
                .build();
        when(orderService.updateById(orderId, orderDetails)).thenReturn(updatedOrder);

        ResponseEntity<OrderDTO> responseEntity = orderController.updateOrder(orderId, orderDetails);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals(updatedOrder, responseEntity.getBody());
        verify(orderService, times(1)).updateById(orderId, updatedOrder);
    }

    @Test
    void updateOrder_shouldPropagateOrderNotFoundException_whenOrderDoesNotExist() {
        Long orderNonExistentId = 99L;
        OrderDTO orderDetails = OrderDTO.builder()
                .orderNumber("ORDER-1")
                .totalValue(BigDecimal.valueOf(0))
                .status(OrderStatus.PROCESSED)
                .orderDateUpdated(LocalDateTime.MIN)
                .build();
        doThrow(new OrderNotFoundException(orderDetails.getOrderNumber()))
                .when(orderService).updateById(eq(orderNonExistentId), any(OrderDTO.class));

        OrderNotFoundException thrown = assertThrows(OrderNotFoundException.class, () -> {
            orderController.updateOrder(orderNonExistentId, orderDetails);
        });

        assertEquals("Order not found with number: " + orderDetails.getOrderNumber(), thrown.getMessage());
        verify(orderService, times(1)).updateById(eq(orderNonExistentId), eq(orderDetails));
    }

    @Test
    void createRandomOrder_shouldReturnCreatedStatusAndOrderDTO_whenServiceSucceeds() {
        OrderDTO expectedOrderDTO = OrderDTO.builder().orderNumber("ORDER-1").build();
        when(orderService.createRandomOrder()).thenReturn(expectedOrderDTO);

        ResponseEntity<OrderDTO> responseEntity = orderController.createRandomOrder();

        assertEquals(HttpStatus.CREATED, responseEntity.getStatusCode());
        assertEquals(expectedOrderDTO, responseEntity.getBody());
        verify(orderService, times(1)).createRandomOrder();
    }
    @Test
    void createRandomOrder_shouldPropagateException_whenServiceThrowsException() {
        doThrow(new RuntimeException("Simulated service error")).when(orderService).createRandomOrder();

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderController.createRandomOrder();
        });

        verify(orderService, times(1)).createRandomOrder();
    }

}
