package vs_fundos.challenge.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.event.OrderCreatedEvent;
import vs_fundos.challenge.exception.OrderCreationException;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.model.Order;
import vs_fundos.challenge.repository.OrderRepository;
import vs_fundos.challenge.service.impl.OrderServiceImpl;
import vs_fundos.challenge.util.Convert;
import vs_fundos.challenge.util.OrderFactory;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceImplTest {
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @Mock
    private OrderFactory orderFactory;
    @Mock
    private Convert convert;
    @InjectMocks
    private OrderServiceImpl orderServiceImpl;

    @Test
    void getOrderById_shouldRetrieveOrderSuccesfully() {
        Long orderId = 1L;
        OrderDTO mockedDTO = OrderDTO.builder().orderNumber("ORDER-01").build();
        Order mockedOrder = new Order();
        mockedOrder.setOrderNumber("ORDER-01");
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockedOrder));
        when(convert.orderModelToDTO(mockedOrder)).thenReturn(mockedDTO);

        OrderDTO resultDTO = orderServiceImpl.getOrderById(orderId);

        verify(orderRepository, times(1)).findById(orderId);
        verify(convert, times(1)).orderModelToDTO(mockedOrder);
        assertEquals(resultDTO, mockedDTO);
    }

    @Test
    void getOrderById_shouldThrowOrderNotFoundException_whenRetrieveNonExistentOrder() {
        Long orderId = 1L;
        when(orderRepository.findById(orderId)).thenThrow(new OrderNotFoundException(orderId));

        OrderNotFoundException thrown = assertThrows(OrderNotFoundException.class, () -> {
            orderServiceImpl.getOrderById(orderId);
        });

        assertEquals("Order not found with ID: " + orderId, thrown.getMessage());
        verify(convert, never()).orderModelToDTO(any());
    }

    @Test
    void createOrder_shouldCreateOrderSuccessfullyAndPublishEvent() {
        OrderDTO inputDto = OrderDTO.builder()
                .orderNumber("ORD-123")
                .totalValue(new BigDecimal("150.75"))
                .status(OrderStatus.UNPROCESSED)
                .build();

        OrderDTO resultDTO = orderServiceImpl.createOrder(inputDto);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getOrderNumber()).isEqualTo(inputDto.getOrderNumber());
        assertThat(savedOrder.getTotalValue()).isEqualTo(inputDto.getTotalValue());
        assertThat(savedOrder.getStatus()).isEqualTo(inputDto.getStatus());
        assertThat(savedOrder.getOrderDateCreated()).isNotNull();
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        OrderCreatedEvent publishedEvent = eventCaptor.getValue();
        assertThat(publishedEvent.getOrderDTO()).isEqualTo(inputDto);
        assertThat(resultDTO).isEqualTo(inputDto);
    }

    @Test
    void createOrder_shouldThrowException_whenSavingFailsOnCreateOrder() {
        OrderDTO inputDto = OrderDTO.builder().orderNumber("ORD-FAIL").build();
        when(orderRepository.save(any(Order.class))).thenThrow(new RuntimeException("Database connection failed"));

        assertThrows(RuntimeException.class, () -> {
            orderServiceImpl.createOrder(inputDto);
        });

        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void updateById_shouldUpdateOrderSuccessfully() {
        Long orderId = 1L;
        OrderDTO orderDetailsDto = OrderDTO.builder()
                .totalValue(new BigDecimal("99.99"))
                .status(OrderStatus.PROCESSED)
                .orderDateUpdated(LocalDateTime.now())
                .build();

        Order existingOrder = new Order();
        existingOrder.setId(orderId);
        existingOrder.setOrderNumber("ORD-EXISTING");
        existingOrder.setTotalValue(new BigDecimal("50.00"));
        existingOrder.setStatus(OrderStatus.UNPROCESSED);
        existingOrder.setOrderDateCreated(LocalDateTime.now().minusDays(1));
        OrderDTO finalDto = OrderDTO.builder()
                .orderNumber("ORD-EXISTING")
                .status(OrderStatus.PROCESSED)
                .orderDateUpdated(LocalDateTime.now())
                .build();
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(existingOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(existingOrder);
        when(convert.orderModelToDTO(any(Order.class))).thenReturn(finalDto);

        OrderDTO resultDto = orderServiceImpl.updateById(orderId, orderDetailsDto);

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
        verify(orderRepository, times(1)).save(orderCaptor.capture());
        Order savedOrder = orderCaptor.getValue();
        assertThat(savedOrder.getId()).isEqualTo(orderId);
        assertThat(savedOrder.getTotalValue()).isEqualTo(orderDetailsDto.getTotalValue());
        assertThat(savedOrder.getStatus()).isEqualTo(orderDetailsDto.getStatus());
        assertThat(savedOrder.getOrderDateUpdated()).isAfter(savedOrder.getOrderDateCreated());
        verify(convert, times(1)).orderModelToDTO(savedOrder);
        assertThat(resultDto).isEqualTo(finalDto);
    }

    @Test
    void updateById_shouldThrowOrderNotFoundException_whenUpdatingNonExistentOrder() {
        Long nonExistentId = 99L;
        OrderDTO orderDetailsDto = OrderDTO.builder().orderNumber("ORD-NON-EXISTENT").build();
        when(orderRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        OrderNotFoundException thrown = assertThrows(OrderNotFoundException.class, () -> {
            orderServiceImpl.updateById(nonExistentId, orderDetailsDto);
        });

        assertThat(thrown.getMessage()).contains(String.valueOf(nonExistentId));
        verify(orderRepository, never()).save(any());
    }

    @Test
    void createRandomOrder_shouldCreateRandomOrderSuccessfullyAndPublishEvent() {
        OrderDTO orderMockDTO = OrderDTO.builder()
                .orderNumber("MOCKED")
                .totalValue(BigDecimal.valueOf(1))
                .status(OrderStatus.UNPROCESSED)
                .orderDateCreated(LocalDateTime.now())
                .orderDateUpdated(LocalDateTime.now())
                .build();
        when(orderFactory.createRandomOrder()).thenReturn(orderMockDTO);
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());

        OrderDTO resultDTO = orderServiceImpl.createRandomOrder();

        assertEquals(orderMockDTO, resultDTO);
        verify(orderRepository,  times(1)).save(any(Order.class));
        ArgumentCaptor<OrderCreatedEvent> eventCaptor = ArgumentCaptor.forClass(OrderCreatedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        OrderCreatedEvent capturedEvent = eventCaptor.getValue();
        assertEquals(orderMockDTO, capturedEvent.getOrderDTO());
    }
    @Test
    void createRandomOrder_shouldThrowException_whenOrderCreationFails() {
        String fixedOrderNumber = "MOCKED-FAILED-ORDER";
        OrderDTO mockOrderDTO = OrderDTO.builder().build();
        mockOrderDTO.setOrderNumber(fixedOrderNumber);
        mockOrderDTO.setStatus(OrderStatus.UNPROCESSED);
        when(orderFactory.createRandomOrder()).thenReturn(mockOrderDTO);
        when(orderRepository.save(any(Order.class))).thenThrow(new OrderCreationException("Database error during save", new Exception()));

        RuntimeException thrown = assertThrows(RuntimeException.class, () -> {
            orderServiceImpl.createRandomOrder();
        });

        assertTrue(thrown.getMessage().contains("Failed to save random order"));
        verify(orderFactory, times(1)).createRandomOrder();
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(eventPublisher, never()).publishEvent(any(OrderCreatedEvent.class));
    }
}
