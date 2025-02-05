package vdtry06.springboot.ecommerce.service.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
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

    public void sendNotification(String topic, NotificationRequest request) {
        log.info("Sending notification request: {}", request);
        notificationKafkaTemplate.send(topic, request);
    }

    public void sendOrderConfirmation(String topic, OrderConfirmation orderConfirmation) {
        log.info("Sending order confirmation: {}", orderConfirmation);
        orderKafkaTemplate.send(topic, orderConfirmation);
    }
}
