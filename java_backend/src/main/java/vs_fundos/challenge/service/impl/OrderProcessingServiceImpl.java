package vs_fundos.challenge.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.exception.OrderAlreadyProcessedException;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.exception.OrderProcessingException;
import vs_fundos.challenge.model.Order;
import vs_fundos.challenge.repository.OrderRepository;
import vs_fundos.challenge.service.OrderProcessingService;

@Service
@RequiredArgsConstructor
public class OrderProcessingServiceImpl implements OrderProcessingService {
    private final OrderRepository orderRepository;
    private static final Logger logger = LogManager.getLogger(OrderProcessingServiceImpl.class);

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
        try {
            orderRepository.save(order);
        } catch (Exception e) {
            throw new OrderProcessingException("Failed to update order " + orderNumber, e);
        }
        logger.info("Order processed successfully: {}", orderNumber);
    }
}
