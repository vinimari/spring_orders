package vs_fundos.challenge.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import vs_fundos.challenge.producer.OrderProducerService;
import vs_fundos.challenge.util.Convert;

@Component
@RequiredArgsConstructor
public class OrderCreatedEventListener {
    private final OrderProducerService orderProducerService;
    private final Convert convert;

    @Async
    @EventListener
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreated(OrderCreatedEvent event) {
        String message = convert.objectToJson(event.getOrderDTO());
        orderProducerService.sendMessage(message);
    }
}
