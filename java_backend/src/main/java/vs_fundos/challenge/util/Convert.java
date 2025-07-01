package vs_fundos.challenge.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import vs_fundos.challenge.dto.OrderDTO;
import vs_fundos.challenge.exception.JsonConvertionException;
import vs_fundos.challenge.model.Order;

@RequiredArgsConstructor
@Component
public class Convert {
    private final ObjectMapper objectMapper;

    public String objectToJson(Object object) {
        String json = null;
        try {
            json = objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new JsonConvertionException("Error converting Object to JSON", e);
        }
        return json;
    }

    public OrderDTO orderModelToDTO(Order order) {
        return OrderDTO.builder()
                .orderNumber(order.getOrderNumber())
                .totalValue(order.getTotalValue())
                .status(order.getStatus())
                .orderDateCreated(order.getOrderDateCreated())
                .orderDateUpdated(order.getOrderDateUpdated()).build();
    }
}
