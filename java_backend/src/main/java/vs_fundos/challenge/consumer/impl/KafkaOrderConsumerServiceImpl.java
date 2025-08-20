package vs_fundos.challenge.consumer.impl;

import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.consumer.OrderConsumerService;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.handler.OrderConsumerHandler;

@Service
@RequiredArgsConstructor
public class KafkaOrderConsumerServiceImpl implements OrderConsumerService {
    private final Logger logger = LogManager.getLogger(KafkaOrderConsumerServiceImpl.class);
    private final OrderConsumerHandler orderConsumerHandler;

    @KafkaListener(topics = "${kafka.topic.name}")
    public void listen(OrderDTO orderDTO) {
        logger.info("Listened order number for handle: {}", orderDTO.getOrderNumber());
        orderConsumerHandler.handleMessage(orderDTO);
    }
}