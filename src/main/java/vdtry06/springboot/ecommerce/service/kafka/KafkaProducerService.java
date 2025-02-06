package vdtry06.springboot.ecommerce.service.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.notification.NotificationRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderConfirmation;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class KafkaProducerService {
    KafkaTemplate<String, String> kafkaTemplate;
    KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate;
    KafkaTemplate<String, OrderConfirmation> orderKafkaTemplate;

    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
    }

    public void sendNotification(NotificationRequest request) {
        log.info("Sending notification request: {}", request);
        Message<NotificationRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader(KafkaHeaders.TOPIC, "payment-topic")
                .build();
        notificationKafkaTemplate.send(message);
    }

    public void sendOrderConfirmation(OrderConfirmation orderConfirmation) {
        log.info("Sending order confirmation: {}", orderConfirmation);
        Message<OrderConfirmation> message = MessageBuilder
                .withPayload(orderConfirmation)
                .setHeader(KafkaHeaders.TOPIC, "order-topic")
                .build();
        orderKafkaTemplate.send(message);
    }
}
