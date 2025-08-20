package vs_fundos.challenge.handler.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.*;
import vs_fundos.challenge.handler.OrderConsumerHandler;
import vs_fundos.challenge.service.OrderProcessingService;
import vs_fundos.challenge.service.notification.NotificationDispatcherService;

@Component
@RequiredArgsConstructor
public class OrderConsumerHandlerImpl implements OrderConsumerHandler {
    private final Logger logger = LogManager.getLogger(OrderConsumerHandlerImpl.class);
    private final OrderProcessingService orderProcessingService;
    private final ObjectMapper objectMapper;
    private final NotificationDispatcherService notificationDispatcherService;

   public void handleMessage(OrderDTO orderDTO) {
       try {
           orderProcessingService.processOrder(orderDTO.getOrderNumber());
           notificationDispatcherService.dispatch(orderDTO.getNotificationType(), "Your order with number: " + orderDTO.getOrderNumber() + " has been processed successfully!");
       } catch (OrderNotFoundException | OrderAlreadyProcessedException | OrderProcessingException e) {
           logger.error("A business exception occurred while processing the message: {}", e.getMessage());
           throw new KafkaProcessingException("Failed to process order due to a business rule. ", e);
       } catch (Exception e) {
           logger.error("An unexpected error occurred: {}", e.getMessage(), e);
           throw new KafkaProcessingException("An unexpected error occurred during processing.", e);
       }
    }
}
