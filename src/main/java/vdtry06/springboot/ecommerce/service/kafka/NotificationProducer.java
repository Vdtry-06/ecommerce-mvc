package vdtry06.springboot.ecommerce.service.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;
import vdtry06.springboot.ecommerce.dto.request.notification.NotificationRequest;

import static org.springframework.kafka.support.KafkaHeaders.TOPIC;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationProducer {
    private final KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate;

    public void sendNotification(NotificationRequest request) {
        log.info("Sending notification request: {}", request);
        Message<NotificationRequest> message = MessageBuilder
                .withPayload(request)
                .setHeader(TOPIC, "payment-topic")
                .build();
        notificationKafkaTemplate.send(message);
    }
}
