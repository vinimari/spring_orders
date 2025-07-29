package vs_fundos.challenge.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.consumer.impl.KafkaOrderConsumerServiceImpl;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.*;
import vs_fundos.challenge.handler.OrderConsumerHandler;
import vs_fundos.challenge.service.impl.OrderServiceImpl;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KafkaOrderConsumerServiceImplTest {
    @Mock
    private OrderConsumerHandler orderConsumerHandler;

    @InjectMocks
    private KafkaOrderConsumerServiceImpl kafkaOrderConsumerService;

    @Test
    void listen_whenMessageIsReceived_shouldDelegateToHandler() {
        String testMessage = "{\"orderNumber\":\"ORDER-123\"}";
        doNothing().when(orderConsumerHandler).handleMessage(testMessage);

        kafkaOrderConsumerService.listen(testMessage);

        verify(orderConsumerHandler, times(1)).handleMessage(testMessage);
    }
}
