package vs_fundos.challenge.producer.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.producer.OrderProducerService;

@Service
@RequiredArgsConstructor
public class KafkaOrderProducerServiceImpl implements OrderProducerService {
    @Value("${kafka.topic.name}")
    private String topicName;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Logger logger = LogManager.getLogger(KafkaOrderProducerServiceImpl.class);

    public void sendMessage(String message) {
        kafkaTemplate.send(topicName, message)
                .whenComplete((result, th) -> {
                    if (th != null) {
                        logger.error("Erro ao enviar mensagem para o tópico Kafka [{}]: {}, mensagem={}", topicName, th.getMessage(), th, message);
                    } else {
                        logger.info("Mensagem enviada com sucesso para o tópico Kafka [{}], offset={}, mensagem={}", topicName, result.getRecordMetadata().offset(), message);
                    }
                });
    }
    }