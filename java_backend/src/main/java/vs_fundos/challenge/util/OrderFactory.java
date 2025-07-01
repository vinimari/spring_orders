package vs_fundos.challenge.util;

import org.springframework.stereotype.Component;
import vs_fundos.challenge.dto.OrderDTO;
import org.springframework.stereotype.Service;
import vs_fundos.challenge.enums.OrderStatus;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Random;

@Component
public class OrderFactory {
    public OrderDTO createRandomOrder() {
        return OrderDTO.builder()
                .orderNumber(generateOrderNumber())
                .totalValue(generateTotalValue())
                .status(OrderStatus.UNPROCESSED)
                .orderDateCreated(LocalDateTime.now())
                .orderDateUpdated(LocalDateTime.now())
                .build();
    }

    private static String generateOrderNumber() {
        return String.format("ORDER-%06d", new Random().nextInt(1000000));
    }

    private static BigDecimal generateTotalValue() {
        double value = (Math.random() * 100) + 1;
        return BigDecimal.valueOf(value).setScale(2, RoundingMode.HALF_UP);
    }
}
