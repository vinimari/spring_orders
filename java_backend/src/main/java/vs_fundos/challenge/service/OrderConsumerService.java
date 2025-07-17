package vs_fundos.challenge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.JsonConvertionException;


@Service
@RequiredArgsConstructor
public class OrderConsumerService {
    private final Logger logger = LogManager.getLogger(OrderConsumerService.class);
    private final OrderService orderService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "${kafka.topic.name}")
    public void listen(String message) {
        try {
            OrderDTO orderDTO = objectMapper.readValue(message, OrderDTO.class);
            logger.info("Message received: orderNumber={}", orderDTO.getOrderNumber());
            orderService.processOrder(orderDTO.getOrderNumber());
        } catch (Exception e) {
            logger.error("Error processing Kafka message: {}", e.getMessage(), e);
            throw new JsonConvertionException("Error converting JSON to DTO: ", e);
        }
    }
}
