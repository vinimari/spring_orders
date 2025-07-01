package vs_fundos.challenge.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.enums.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderFactoryTest {

    private OrderFactory orderFactory;

    @BeforeEach
    void setUp() {
        orderFactory = new OrderFactory();
    }

    @Test
    void createRandomOrder_shouldReturnDtoWithAllFieldsSet() {
        OrderDTO createdOrder = orderFactory.createRandomOrder();

        assertNotNull(createdOrder);
        assertNotNull(createdOrder.getOrderNumber());
        assertNotNull(createdOrder.getTotalValue());
        assertNotNull(createdOrder.getStatus());
        assertNotNull(createdOrder.getOrderDateCreated());
        assertNotNull(createdOrder.getOrderDateUpdated());
    }

    @RepeatedTest(3)
    void createRandomOrder_shouldCreateOrderWithValidFormatAndValues() {
        LocalDateTime testStartTime = LocalDateTime.now();

        OrderDTO createdOrder = orderFactory.createRandomOrder();

        assertThat(createdOrder.getStatus()).isEqualTo(OrderStatus.UNPROCESSED);
        assertThat(createdOrder.getOrderNumber()).matches("ORDER-\\d{6}");
        assertThat(createdOrder.getTotalValue()).isBetween(new BigDecimal("1.00"), new BigDecimal("101.00"));
        assertThat(createdOrder.getTotalValue().scale()).isEqualTo(2);
        assertThat(createdOrder.getOrderDateCreated()).isAfterOrEqualTo(testStartTime);
    }
}
