package vs_fundos.challenge.service;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderProducerServiceTest {
    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;
    @InjectMocks
    private OrderProducerService orderProducerService;
    private final String TEST_TOPIC_NAME = "test-topic";

    @BeforeEach
    void SetUp() {
        try {
            java.lang.reflect.Field topicNameField = OrderProducerService.class.getDeclaredField("topicName");
            topicNameField.setAccessible(true);
            topicNameField.set(orderProducerService, TEST_TOPIC_NAME);
        } catch (Exception e) {
            fail("Falha na configuração do campo topicName para o teste: " + e.getMessage());
        }
    }
    @Test
    void shouldSendMessageSuccessfully() {
        String message = "test message";
        // O offset é uma posição sequencial imutável de um registro dentro de uma partição
        long offset = 12345L;
        // Unidade de paralelismo; um tópico pode ter uma ou mais partições.
        int partition = 0;
        // Combina o nome do tópico e o número da partição para identificar um local específico no Kafka.
        TopicPartition topicPartition = new TopicPartition(TEST_TOPIC_NAME, partition);
        // RecordMetadata contém os metadados de um registro que foi produzido com sucesso (tópico, partição, offset, timestamp).
        RecordMetadata recordMetadata = new RecordMetadata(topicPartition, offset, 0, System.currentTimeMillis(), 0L, 0, 0);
        // ProducerRecord representa um registro (chave-valor) a ser enviado para um tópico específico no Kafka.
        ProducerRecord<String, String> producerRecord = new ProducerRecord<>(TEST_TOPIC_NAME, message);
        // SendResult encapsula o ProducerRecord original e o RecordMetadata retornado após o envio bem-sucedido para o Kafka.
        SendResult<String, String> sendResult = new SendResult<>(producerRecord, recordMetadata);
        // No contexto do Kafka, o método 'send' do KafkaTemplate retorna um CompletableFuture que será completado com o SendResult.
        CompletableFuture<SendResult<String, String>> future = CompletableFuture.completedFuture(sendResult);
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);
        assertDoesNotThrow(() -> orderProducerService.sendMessage(message));
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC_NAME, message);
    }

    @Test
    void shouldHandleSendMessageFailure() {
        String message = "test message";
        String errorMessage = "Failed to send message";
        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException(errorMessage));
        when(kafkaTemplate.send(anyString(), anyString())).thenReturn(future);
        assertDoesNotThrow(() -> orderProducerService.sendMessage(message));
        verify(kafkaTemplate, times(1)).send(TEST_TOPIC_NAME, message);
    }
}
