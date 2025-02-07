package vdtry06.springboot.ecommerce.kafka;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.log4j.Log4j2;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.notification.dto.NotificationRequest;

@Service
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Log4j2
public class KafkaProducerService {
    KafkaTemplate<String, String> kafkaTemplate;
    KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate;

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
}
