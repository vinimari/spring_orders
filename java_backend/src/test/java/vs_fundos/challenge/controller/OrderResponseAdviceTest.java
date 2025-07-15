package vs_fundos.challenge.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.http.ResponseEntity;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.util.Cryptography;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderResponseAdviceTest {

    @Mock
    private Cryptography cryptography;

    @InjectMocks
    private OrderResponseAdvice orderResponseAdvice;

    private OrderDTO orderDTO;
    private final String plainOrderNumber = "XYZ";
    private final String encryptedOrderNumber = "[encrypted]XYZ";

    @BeforeEach
    void setUp() {
        orderDTO = OrderDTO.builder().orderNumber(plainOrderNumber).build();
    }

    private static class TestController {
        public ResponseEntity<OrderDTO> getOrderAsResponseEntity() { return null; }
        public OrderDTO getOrderAsDirectDTO() { return null; }
        public ResponseEntity<String> getSomethingElse() { return null; }
    }

    @Test
    void beforeBodyWrite_shouldEncryptField_whenBodyIsOrderDTO() throws Exception {
        when(cryptography.encrypt(plainOrderNumber)).thenReturn(encryptedOrderNumber);

        OrderDTO result = (OrderDTO) orderResponseAdvice
                .beforeBodyWrite(orderDTO, null, null, null, null, null);

        assertNotNull(result);
        assertEquals(encryptedOrderNumber, result.getOrderNumber());
        verify(cryptography, times(1)).encrypt(plainOrderNumber);
    }

    @Test
    void beforeBodyWrite_shouldThrowException_whenCryptographyFails() throws Exception {
        String errorMessage = "Cryptography fails";
        when(cryptography.encrypt(plainOrderNumber)).thenThrow(new RuntimeException(errorMessage));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            orderResponseAdvice.beforeBodyWrite(orderDTO, null, null, null, null, null);
        });

        assertTrue(exception.getMessage().contains(errorMessage));
        verify(cryptography, times(1)).encrypt(plainOrderNumber);
    }

    @Test
    void supports_shouldReturnTrue_forResponseEntityWithOrderDTO() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("getOrderAsResponseEntity");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        boolean result = orderResponseAdvice.supports(methodParameter, null);

        assertTrue(result);
    }

    @Test
    void supports_shouldReturnTrue_forDirectOrderDTOReturn() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("getOrderAsDirectDTO");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        boolean result = orderResponseAdvice.supports(methodParameter, null);

        assertTrue(result);
    }

    @Test
    void supports_shouldReturnFalse_forOtherTypesInResponseEntity() throws NoSuchMethodException {
        Method method = TestController.class.getMethod("getSomethingElse");
        MethodParameter methodParameter = new MethodParameter(method, -1);

        boolean result = orderResponseAdvice.supports(methodParameter, null);

        assertFalse(result);
    }
}
