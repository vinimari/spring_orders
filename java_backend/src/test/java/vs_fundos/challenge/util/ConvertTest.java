package vs_fundos.challenge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.exception.JsonConvertionException;
import vs_fundos.challenge.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static net.bytebuddy.matcher.ElementMatchers.any;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ConvertTest {
    @Mock
    private ObjectMapper objectMapper;
    @InjectMocks
    private Convert convert;

    @Test
    void objectToJson_shouldReturnJsonString_whenConversionIsSuccessful() throws JsonProcessingException {
        OrderDTO dto = OrderDTO.builder()
                .orderNumber("ORDER-1")
                .build();
        String expectedJson = "{\"orderNumber\":\"ORDER-1\"}";
        when(objectMapper.writeValueAsString(dto)).thenReturn(expectedJson);

        String actualJson = convert.objectToJson(dto);

        assertEquals(expectedJson, actualJson);
        verify(objectMapper, times(1)).writeValueAsString(dto);
    }

    @Test
    void objectToJson_shouldThrowJsonConvertionException_whenConversionFails() throws JsonProcessingException {
        OrderDTO dto = OrderDTO.builder()
                .orderNumber("ORDER-1")
                .build();
        JsonProcessingException rootException = new JsonProcessingException("Serialization error") {};
        when(objectMapper.writeValueAsString(dto)).thenThrow(rootException);

        JsonConvertionException thrown = assertThrows(
                JsonConvertionException.class,
                () -> convert.objectToJson(dto)
        );

        assertEquals("Error converting Object to JSON", thrown.getMessage());
        assertEquals(rootException, thrown.getCause());
        verify(objectMapper, times(1)).writeValueAsString(dto);
    }

    @Test
    void orderModelToDTO_shouldMapAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.now();
        Order sourceOrder = new Order();
        sourceOrder.setOrderNumber("ORDER-01");
        sourceOrder.setStatus(OrderStatus.PROCESSED);
        sourceOrder.setOrderDateCreated(now.minusDays(1));
        sourceOrder.setOrderDateUpdated(now);

        OrderDTO expectedDTO = convert.orderModelToDTO(sourceOrder);

        assertNotNull(expectedDTO);
        assertEquals(expectedDTO.getOrderNumber(), sourceOrder.getOrderNumber());
        assertEquals(expectedDTO.getOrderNumber(), sourceOrder.getOrderNumber());
        assertEquals(expectedDTO.getTotalValue(), sourceOrder.getTotalValue());
        assertEquals(expectedDTO.getStatus(), sourceOrder.getStatus());
        assertEquals(expectedDTO.getOrderDateCreated(), sourceOrder.getOrderDateCreated());
        assertEquals(expectedDTO.getOrderDateUpdated(), sourceOrder.getOrderDateUpdated());
    }
}
