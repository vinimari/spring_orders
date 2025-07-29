package vs_fundos.challenge.service.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import vs_fundos.challenge.dto.OrderDTO;
import jakarta.transaction.Transactional;
import vs_fundos.challenge.event.OrderCreatedEvent;
import vs_fundos.challenge.exception.*;
import vs_fundos.challenge.model.Order;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.repository.OrderRepository;
import vs_fundos.challenge.service.OrderService;
import vs_fundos.challenge.util.Convert;
import vs_fundos.challenge.util.OrderFactory;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderFactory orderFactory;
    private final Convert convert;
    private static final Logger logger = LogManager.getLogger(OrderServiceImpl.class);

    @Transactional
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return convert.orderModelToDTO(order);
    }

    @Transactional
    public OrderDTO createRandomOrder() {
        OrderDTO orderDTO = orderFactory.createRandomOrder();
        logger.info("Starting random order creation: {}", orderDTO.getOrderNumber());
        Order order = new Order();
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setTotalValue(orderDTO.getTotalValue());
        order.setStatus(orderDTO.getStatus());
        order.setNotificationType(orderDTO.getNotificationType());
        order.setOrderDateCreated(orderDTO.getOrderDateCreated());
        order.setOrderDateUpdated(orderDTO.getOrderDateUpdated());
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Failed to save random order {}, Error: {}", orderDTO.getOrderNumber(), e.getMessage());
            throw new OrderCreationException("Failed to save random order: " + orderDTO.getOrderNumber(), e);
        }
        logger.info("Random order created successfully: {}", orderDTO.getOrderNumber());
        eventPublisher.publishEvent(new OrderCreatedEvent(orderDTO));
        return orderDTO;
    }

    @Transactional
    public OrderDTO createOrder(OrderDTO orderDTO) {
        logger.info("Starting order creation: {}", orderDTO.getOrderNumber());
        Order order = new Order();
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setTotalValue(orderDTO.getTotalValue());
        order.setStatus(orderDTO.getStatus());
        order.setNotificationType(orderDTO.getNotificationType());
        order.setOrderDateCreated(LocalDateTime.now());
        order.setOrderDateUpdated(LocalDateTime.now());
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            logger.error("Failed to save order {}, Error: {}", orderDTO.getOrderNumber(), e.getMessage());
            throw new OrderCreationException("Failed to save order: " + orderDTO.getOrderNumber(), e);
        }
        logger.info("Order created successfully: {}", orderDTO.getOrderNumber());
        eventPublisher.publishEvent(new OrderCreatedEvent(orderDTO));
        return orderDTO;
    }

    public OrderDTO updateById(Long id, OrderDTO orderDetails) {
        logger.info("Starting order update: {}", id);
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));
        existingOrder.setTotalValue(orderDetails.getTotalValue());
        existingOrder.setStatus(orderDetails.getStatus());
        existingOrder.setNotificationType(orderDetails.getNotificationType());
        existingOrder.setOrderDateUpdated(LocalDateTime.now());
        Order updatedOrder = null;
        try {
            updatedOrder = orderRepository.save(existingOrder);
        } catch (Exception e) {
            logger.error("Failed to update order {}, Error: {}", orderDetails.getOrderNumber(), e.getMessage());
            throw new OrderUpdateException("Failed to update order: " +  orderDetails.getOrderNumber(), e);
        }
        logger.info("Order updated successfully: {}", orderDetails.getOrderNumber());
        return convert.orderModelToDTO(updatedOrder);
    }
}
