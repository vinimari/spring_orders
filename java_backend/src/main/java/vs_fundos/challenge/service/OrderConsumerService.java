package vs_fundos.challenge.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.*;


@Service
@RequiredArgsConstructor
public class OrderConsumerService {
    private final Logger logger = LogManager.getLogger(OrderConsumerService.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.name}")
    public void listen(String message) {
        OrderDTO orderDTO = null;
        try {
            orderDTO = objectMapper.readValue(message, OrderDTO.class);
            logger.info("Message received: orderNumber={}", orderDTO.getOrderNumber());
            orderService.processOrder(orderDTO.getOrderNumber());
        } catch (JsonProcessingException e) {
            throw new JsonConvertionException("Error converting JSON to DTO: ", e);
        } catch (OrderNotFoundException | OrderAlreadyProcessedException | OrderProcessingException e) {
            logger.error("A business exception occurred while processing the message: {}", e.getMessage());
            throw new KafkaProcessingException("Failed to process order due to a business rule. ", e);
        } catch (Exception e) {
            logger.error("An unexpected error occurred: {}", e.getMessage(), e);
            throw new KafkaProcessingException("An unexpected error occurred during processing.", e);
        }
    }
}
