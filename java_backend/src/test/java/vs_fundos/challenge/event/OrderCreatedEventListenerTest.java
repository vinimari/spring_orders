package vs_fundos.challenge.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.producer.OrderProducerService;
import vs_fundos.challenge.util.Convert;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderCreatedEventListenerTest {

    @Mock
    private OrderProducerService orderProducerService;
    @Mock
    private Convert convert;
    @InjectMocks
    private OrderCreatedEventListener orderCreatedEventListener;

    @Test
    void handleOrderCreated_shouldConvertAndSendMessage() {
        OrderDTO orderDTO = OrderDTO.builder()
                .orderNumber("ORDER-O1")
                .build();
        OrderCreatedEvent event = new OrderCreatedEvent(orderDTO);
        String expectedJsonMessage = "{\"orderNumber\":\"ORDER-01\"}";
        when(convert.objectToJson(orderDTO)).thenReturn(expectedJsonMessage);

        orderCreatedEventListener.handleOrderCreated(event);

        verify(convert, times(1)).objectToJson(orderDTO);
        verify(orderProducerService, times(1)).sendMessage(expectedJsonMessage);
        verifyNoMoreInteractions(convert, orderProducerService);
    }

}
