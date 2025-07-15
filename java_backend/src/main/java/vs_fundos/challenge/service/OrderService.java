package vs_fundos.challenge.service;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.ApplicationEventPublisher;
import vs_fundos.challenge.dto.OrderDTO;
import jakarta.transaction.Transactional;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.event.OrderCreatedEvent;
import vs_fundos.challenge.exception.OrderAlreadyProcessedException;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.model.Order;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.repository.OrderRepository;
import vs_fundos.challenge.util.Convert;
import vs_fundos.challenge.util.OrderFactory;

import java.time.LocalDateTime;


@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final OrderFactory orderFactory;
    private final Convert convert;
    private static final Logger logger = LogManager.getLogger(OrderService.class);

    @Transactional
    public OrderDTO getOrderById(Long id) {
        Order order = orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException(id));
        return convert.orderModelToDTO(order);
    }

    @Transactional
    public OrderDTO createRandomOrder() {
        OrderDTO orderDTO = orderFactory.createRandomOrder();
        logger.info("Starting order creation: {}", orderDTO.getOrderNumber());
        Order order = new Order();
        order.setOrderNumber(orderDTO.getOrderNumber());
        order.setTotalValue(orderDTO.getTotalValue());
        order.setStatus(orderDTO.getStatus());
        order.setOrderDateCreated(orderDTO.getOrderDateCreated());
        order.setOrderDateUpdated(orderDTO.getOrderDateUpdated());
        try {
            orderRepository.save(order);
            logger.info("Order created successfully: {}", orderDTO.getOrderNumber());
        } catch (Exception e) {
            logger.error("Failed to save order {}, Error: {}", orderDTO.getOrderNumber(), e.getMessage());
            throw e;
        }
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
        order.setOrderDateCreated(LocalDateTime.now());
        order.setOrderDateUpdated(LocalDateTime.now());
        try {
            orderRepository.save(order);
            logger.info("Order created successfully: {}", orderDTO.getOrderNumber());
        } catch (Exception e) {
            logger.error("Failed to save order {}, Error: {}", orderDTO.getOrderNumber(), e.getMessage());
            throw e;
        }
        eventPublisher.publishEvent(new OrderCreatedEvent(orderDTO));
        return orderDTO;
    }

    public OrderDTO updateById(Long id, OrderDTO orderDetails) {
        Order existingOrder = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException(id));

        existingOrder.setTotalValue(orderDetails.getTotalValue());
        existingOrder.setStatus(orderDetails.getStatus());
        existingOrder.setOrderDateUpdated(LocalDateTime.now());
        Order updatedOrder = orderRepository.save(existingOrder);
        return convert.orderModelToDTO(updatedOrder);
    }

    @Transactional
    public void processOrder(String orderNumber) {
        logger.info("Starting order process: {}", orderNumber);
        Order order = orderRepository.findByOrderNumber(orderNumber);
        if (order == null) {
            throw new OrderNotFoundException(orderNumber);
        }
        if (order.getStatus() == OrderStatus.PROCESSED) {
            throw new OrderAlreadyProcessedException(orderNumber);
        }
        order.setStatus(OrderStatus.PROCESSED);
        orderRepository.save(order);
        logger.info("Order processed successfully: {}", orderNumber);
    }
}
