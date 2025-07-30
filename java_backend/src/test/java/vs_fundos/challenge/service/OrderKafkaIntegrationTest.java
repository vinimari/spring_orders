package vs_fundos.challenge.service;

import org.junit.jupiter.api.BeforeEach;
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
import vs_fundos.challenge.consumer.OrderConsumerService;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.JsonConvertionException;
import vs_fundos.challenge.producer.OrderProducerService;
import vs_fundos.challenge.service.notification.NotificationDispatcherService;
import vs_fundos.challenge.util.Convert;

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
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public class OrderKafkaIntegrationTest {
    @TestConfiguration
    static class KafkaTestConfig {
        @Bean
        @Primary
        public OrderProcessingService orderProcessingService() {
            return Mockito.mock(OrderProcessingService.class);
        }
    }
    @Autowired
    private OrderProducerService producerService;
    @Autowired
    private OrderProcessingService orderProcessingService;
    @Autowired
    private Convert convert;
    @Autowired
    private NotificationDispatcherService notificationDispatcherService;
    @Autowired
    private OrderConsumerService orderConsumerService;

    @BeforeEach
    void setUp() { Mockito.reset(orderProcessingService); }

    @Test
    void shouldSendAndReceiveMessageSuccessfully() {
        String expectedOrderNumber = "ORDER-01";
        OrderDTO messagePayload = OrderDTO.builder().orderNumber(expectedOrderNumber).build();

        producerService.sendMessage(messagePayload);

        verify(orderProcessingService, timeout(5000)).processOrder(expectedOrderNumber);
    }

    @ParameterizedTest
    @ValueSource(strings = {"ORDER-01", "ORDER-02", "ORDER-03"})
    void shouldProcessMultipleMessagesSuccessfully(String orderNumber) {
        OrderDTO order = OrderDTO.builder().orderNumber(orderNumber).build();

        producerService.sendMessage(order);

        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            verify(orderProcessingService, times(1)).processOrder(orderNumber);
        });
    }
}
