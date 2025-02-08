package pl.excellentapp.brewery.order.application.sender.jms;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.jms.JMSException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;
import pl.excellentapp.brewery.model.events.Event;
import pl.excellentapp.brewery.model.events.OrderCancelledEvent;
import pl.excellentapp.brewery.model.events.OrderCreatedEvent;
import pl.excellentapp.brewery.model.events.OrderPickedUpEvent;

@Component
@Slf4j
class JmsOrderHandler {

    private final JmsTemplate jmsTemplate;
    private final ObjectMapper objectMapper;
    private final String jmsReserveStockQueueName;
    private final String jmsReleaseStockQueueName;
    private final String jmsNotificationEmailQueueName;
    private final String jmsNotificationSmsQueueName;

    JmsOrderHandler(JmsTemplate jmsTemplate,
                    ObjectMapper objectMapper,
                    @Value("${queue.inventory.allocate-stock}") String jmsReserveStockQueueName,
                    @Value("${queue.inventory.allocate-failure}") String jmsReleaseStockQueueName,
                    @Value("${queue.notification.email}") String jmsNotificationEmailQueueName,
                    @Value("${queue.notification.sms}") String jmsNotificationSmsQueueName
    ) {
        this.jmsTemplate = jmsTemplate;
        this.objectMapper = objectMapper;
        this.jmsReserveStockQueueName = jmsReserveStockQueueName;
        this.jmsReleaseStockQueueName = jmsReleaseStockQueueName;
        this.jmsNotificationEmailQueueName = jmsNotificationEmailQueueName;
        this.jmsNotificationSmsQueueName = jmsNotificationSmsQueueName;
    }

    @EventListener
    public void publish(OrderCreatedEvent event) {
        sendAndWaitForAnswer(jmsReserveStockQueueName, event); // TODO: create reserve stock for beer event
        sendAndWaitForAnswer(jmsNotificationEmailQueueName, event); // TODO: create email notification for customer
        sendAndWaitForAnswer(jmsNotificationSmsQueueName, event); // TODO: create sms notification for customer
    }

    @EventListener
    public void publish(OrderPickedUpEvent event) {
        sendAndWaitForAnswer(jmsNotificationEmailQueueName, event); // TODO: create email notification for customer
        sendAndWaitForAnswer(jmsNotificationSmsQueueName, event); // TODO: create sms notification for customer
    }

    @EventListener
    public void publish(OrderCancelledEvent event) {
        sendAndWaitForAnswer(jmsReleaseStockQueueName, event); // TODO: create release stock for beer event
    }

    private void sendAndWaitForAnswer(String queueName, Event event) {
        try {
            jmsTemplate.sendAndReceive(queueName, session -> {
                try {
                    final var helloMessage = session.createTextMessage(objectMapper.writeValueAsString(event));
                    helloMessage.setStringProperty("_type", event.getClass().getName());
                    log.info("Send event: {}", event);
                    return helloMessage;

                } catch (JsonProcessingException e) {
                    throw new JMSException("boom");
                }
            });
        } catch (Exception exception) {
            log.error("Problem with send JSM event {}", exception.getMessage(), exception);
        }
    }
}
