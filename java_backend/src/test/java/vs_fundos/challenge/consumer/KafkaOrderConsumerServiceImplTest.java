package vs_fundos.challenge.consumer;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.consumer.impl.KafkaOrderConsumerServiceImpl;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.handler.OrderConsumerHandler;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaOrderConsumerServiceImplTest {
    @Mock
    private OrderConsumerHandler orderConsumerHandler;

    @InjectMocks
    private KafkaOrderConsumerServiceImpl kafkaOrderConsumerService;

    @Test
    void listen_whenMessageIsReceived_shouldDelegateToHandler() {
        OrderDTO testMessage = OrderDTO.builder().orderNumber("ORDER-123").build();
        doNothing().when(orderConsumerHandler).handleMessage(testMessage);

        kafkaOrderConsumerService.listen(testMessage);

        verify(orderConsumerHandler, times(1)).handleMessage(testMessage);
    }
}
