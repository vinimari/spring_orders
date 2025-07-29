package vs_fundos.challenge.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.enums.OrderStatus;
import vs_fundos.challenge.exception.OrderCreationException;
import vs_fundos.challenge.exception.OrderNotFoundException;
import vs_fundos.challenge.exception.OrderUpdateException;
import vs_fundos.challenge.exception.ResponseEncryptionException;
import vs_fundos.challenge.interceptor.OrderInterceptor;
import vs_fundos.challenge.service.impl.OrderServiceImpl;
import vs_fundos.challenge.util.Cryptography;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
@Import({OrderInterceptor.class, OrderResponseAdvice.class})
public class OrderControllerIntegrationTest {
    private static final String BASE_URL = "/orders";
    private static final String TOKEN_HEADER = "token";
    private static final String TOKEN_VALUE = "token_value";
    private static final String ENCRYPTED_ORDER_NUMBER = "ENCRYPTED-ORDER-XYZ";
    private static final String ORIGINAL_ORDER_NUMBER = "ORDER-XYZ";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderServiceImpl orderServiceImpl;

    @MockitoBean
    private Cryptography cryptography;

    private OrderDTO sampleOrderDTO;

    @BeforeEach
    void setUp() throws Exception {
        sampleOrderDTO = OrderDTO.builder()
                .orderNumber(ORIGINAL_ORDER_NUMBER)
                .totalValue(new BigDecimal("199.99"))
                .status(OrderStatus.UNPROCESSED)
                .orderDateCreated(LocalDateTime.now())
                .orderDateUpdated(LocalDateTime.now())
                .build();
        when(cryptography.encrypt(ORIGINAL_ORDER_NUMBER)).thenReturn(ENCRYPTED_ORDER_NUMBER);
    }

    @Nested
    @DisplayName("GET /orders/{id}")
    class GetOrderByIdTests {
        @Test
        void getOrderById_whenOrderExists_shouldReturn200AndEncryptedDTO() throws Exception {
            when(orderServiceImpl.getOrderById(1L)).thenReturn(sampleOrderDTO);

            ResultActions result = mockMvc.perform(get(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderNumber").value(ENCRYPTED_ORDER_NUMBER))
                    .andExpect(jsonPath("$.totalValue").value(199.99))
                    .andExpect(jsonPath("$.status").value("UNPROCESSED"));
        }

        @Test
        void getOrderById_whenOrderNotFound_shouldReturn404() throws Exception {
            long nonExistentId = 99L;
            when(orderServiceImpl.getOrderById(nonExistentId)).thenThrow(new OrderNotFoundException(nonExistentId));

            ResultActions result = mockMvc.perform(get(BASE_URL + "/" + nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Order not found with ID: " + nonExistentId));
        }

        @Test
        void getOrderById_whenEncryptionFails_shouldReturn500() throws Exception {
            String errorMessage = "Failed to encrypt the order number: " + ORIGINAL_ORDER_NUMBER;
            when(orderServiceImpl.getOrderById(1L)).thenReturn(sampleOrderDTO);
            when(cryptography.encrypt(anyString()))
                    .thenThrow(new ResponseEncryptionException(errorMessage, new RuntimeException("AES error")));

            ResultActions result = mockMvc.perform(get(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("POST /orders/")
    class CreateOrderTests {
        @Test
        @DisplayName("Should return 201 Created with encrypted OrderDTO for a valid request")
        void createOrder_whenValidRequest_shouldReturn201AndEncryptedDTO() throws Exception {
            when(orderServiceImpl.createOrder(any(OrderDTO.class))).thenReturn(sampleOrderDTO);

            ResultActions result = mockMvc.perform(post(BASE_URL + "/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleOrderDTO)));

            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").value(ENCRYPTED_ORDER_NUMBER))
                    .andExpect(jsonPath("$.totalValue").value(199.99))
                    .andExpect(jsonPath("$.status").value("UNPROCESSED"));
        }

        @Test
        void createOrder_whenInvalidPayload_shouldReturn400() throws Exception {
            String invalidPayload = "{\"orderNumber\":\"123\", \"totalValue\":\"invalid-number\"}";

            ResultActions result = mockMvc.perform(post(BASE_URL + "/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(invalidPayload));

            result.andExpect(status().isBadRequest());
        }

        @Test
        void createOrder_whenCreationFails_shouldReturn500() throws Exception {
            String errorMessage = "Failed to save";
            when(orderServiceImpl.createOrder(any(OrderDTO.class)))
                    .thenThrow(new OrderCreationException(errorMessage, new RuntimeException()));

            ResultActions result = mockMvc.perform(post(BASE_URL + "/")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(sampleOrderDTO)));

            result.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("PUT /orders/{id}")
    class UpdateOrderTests {
        @Test
        void updateOrder_whenOrderExists_shouldReturn200AndEncryptedDTO() throws Exception {
            OrderDTO updateDetails = OrderDTO.builder().totalValue(new BigDecimal("250.50")).build();
            OrderDTO updatedOrder = sampleOrderDTO;
            updatedOrder.setTotalValue(updateDetails.getTotalValue());
            when(orderServiceImpl.updateById(eq(1L), any(OrderDTO.class))).thenReturn(updatedOrder);

            ResultActions result = mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDetails)));

            result.andExpect(status().isOk())
                    .andExpect(jsonPath("$.orderNumber").value(ENCRYPTED_ORDER_NUMBER))
                    .andExpect(jsonPath("$.totalValue").value(250.50))
                    .andExpect(jsonPath("$.status").value("UNPROCESSED"));
        }

        @Test
        void updateOrder_whenOrderNotFound_shouldReturn404() throws Exception {
            long nonExistentId = 99L;
            OrderDTO updateDetails = OrderDTO.builder().totalValue(new BigDecimal("250.50")).build();
            when(orderServiceImpl.updateById(eq(nonExistentId), any(OrderDTO.class)))
                    .thenThrow(new OrderNotFoundException(nonExistentId));

            ResultActions result = mockMvc.perform(put(BASE_URL + "/" + nonExistentId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDetails)));

            result.andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.error").value("Not Found"))
                    .andExpect(jsonPath("$.message").value("Order not found with ID: " + nonExistentId));
        }

        @Test
        void updateOrder_whenUpdateFails_shouldReturn500() throws Exception {
            String errorMessage = "Failed to update";
            OrderDTO updateDetails = OrderDTO.builder().totalValue(new BigDecimal("250.50")).build();
            when(orderServiceImpl.updateById(eq(1L), any(OrderDTO.class)))
                    .thenThrow(new OrderUpdateException(errorMessage, new RuntimeException()));

            ResultActions result = mockMvc.perform(put(BASE_URL + "/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateDetails)));

            result.andExpect(status().isInternalServerError())
                    .andExpect(jsonPath("$.error").value("Internal Server Error"))
                    .andExpect(jsonPath("$.message").value(errorMessage));
        }
    }

    @Nested
    @DisplayName("POST /orders/random (Protected Endpoint)")
    class CreateRandomOrderTests {
        @Test
        void createRandomOrder_whenTokenIsValid_shouldReturn201AndEncryptedDTO() throws Exception {
            when(orderServiceImpl.createRandomOrder()).thenReturn(sampleOrderDTO);

            ResultActions result = mockMvc.perform(post(BASE_URL + "/random")
                    .header(TOKEN_HEADER, TOKEN_VALUE)
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isCreated())
                    .andExpect(jsonPath("$.orderNumber").value(ENCRYPTED_ORDER_NUMBER))
                    .andExpect(jsonPath("$.totalValue").value(199.99))
                    .andExpect(jsonPath("$.status").value("UNPROCESSED"));
        }

        @Test
        void createRandomOrder_whenTokenIsMissing_shouldReturn400() throws Exception {
            ResultActions result = mockMvc.perform(post(BASE_URL + "/random")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isBadRequest())
                    .andExpect(content().string("Header '" + TOKEN_HEADER + "' missing or invalid."));
        }

        @Test
        void createRandomOrder_whenTokenIsInvalid_shouldReturn400() throws Exception {
            ResultActions result = mockMvc.perform(post(BASE_URL + "/random")
                    .header(TOKEN_HEADER, "invalid_token")
                    .contentType(MediaType.APPLICATION_JSON));

            result.andExpect(status().isBadRequest())
                    .andExpect(content().string("Header '" + TOKEN_HEADER + "' missing or invalid."));
        }
    }
}
