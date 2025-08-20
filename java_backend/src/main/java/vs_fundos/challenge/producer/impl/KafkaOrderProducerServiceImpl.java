package vs_fundos.challenge.producer.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.producer.OrderProducerService;

@Service
@RequiredArgsConstructor
public class KafkaOrderProducerServiceImpl implements OrderProducerService {
    @Value("${kafka.topic.name}")
    private String topicName;
    private final KafkaTemplate<String, OrderDTO> kafkaTemplate;
    private final Logger logger = LogManager.getLogger(KafkaOrderProducerServiceImpl.class);

    public void sendMessage(OrderDTO orderDTO) {
        kafkaTemplate.send(topicName, orderDTO)
                .whenComplete((result, th) -> {
                    if (th != null) {
                        logger.error("Error sending message to Kafka topic [{}]: {}, message={}", topicName, th.getMessage(), th, orderDTO.toString());
                    } else {
                        logger.info("Message successfully sent to Kafka topic [{}], offset={}, message={}", topicName, result.getRecordMetadata().offset(), orderDTO.toString());
                    }
                });
    }
    }