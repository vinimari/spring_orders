package vs_fundos.challenge.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.JsonConvertionException;
import vs_fundos.challenge.util.Convert;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@SpringBootTest(properties = {
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "kafka.topic.name=test_topic",
        "spring.kafka.consumer.auto-offset-reset=earliest",
        "spring.kafka.consumer.group-id=test-group-${random.uuid}"
})
@EmbeddedKafka(
        partitions = 1,
        topics = {"${kafka.topic.name}"},
        brokerProperties = { "listeners=PLAINTEXT://localhost:9094", "port=9094" }
)
@DirtiesContext
public class OrderKafkaIntegrationTest {
    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        @Primary
        public OrderService orderService() {
            return Mockito.mock(OrderService.class);
        }
    }
    @Autowired
    private OrderProducerService producerService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private Convert convert;
    @Autowired
    private OrderConsumerService orderConsumerService;

    @BeforeEach
    void setUp() { Mockito.reset(orderService); }

    @Test
    void shouldSendAndReceiveMessageSuccessfully() {
        String expectedOrderNumber = "ORDER-01";
        OrderDTO orderDTO = OrderDTO.builder().orderNumber(expectedOrderNumber).build();
        String messagePayload = convert.objectToJson(orderDTO);

        producerService.sendMessage(messagePayload);

        verify(orderService, timeout(5000)).processOrder(expectedOrderNumber);
    }

    @Test
    void shouldNotProcessMalformedMessage() {
        String malformedMessage = "{\"orderNumber\": \"ORDER-02\", \"status\": ";
        producerService.sendMessage(malformedMessage);

        await().pollDelay(5, TimeUnit.SECONDS).until(() -> true);

        verify(orderService, never()).processOrder(Mockito.anyString());
    }

    @Test
    void shouldThrowJsonConvertionExceptionForMalformedMessage() {
        String malformedMessage = "{\"orderNumber\": \"ORDER-02\", \"status\": ";

        JsonConvertionException thrown = assertThrows(JsonConvertionException.class, () -> {
            orderConsumerService.listen(malformedMessage);
        });

        assertTrue(thrown.getMessage().contains("Error converting JSON to DTO: "));
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER-01", "ORDER-02", "ORDER-03"})
    void shouldProcessMultipleMessagesSuccessfully(String orderNumber) {
        OrderDTO order = OrderDTO.builder().orderNumber(orderNumber).build();

        String payload = convert.objectToJson(order);
        producerService.sendMessage(payload);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderService, times(1)).processOrder(orderNumber);
        });
    }
}
