package vdtry06.springboot.ecommerce.configuration.kafka;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import vdtry06.springboot.ecommerce.dto.request.notification.NotificationRequest;
import vdtry06.springboot.ecommerce.dto.response.order.OrderConfirmation;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafka
public class KafkaProducerConfig {

    private static final String BOOTSTRAP_SERVERS = "localhost:9092";

    private <T> ProducerFactory<String, T> createProducerFactory(Class<T> valueClass) {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, BOOTSTRAP_SERVERS);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        if (valueClass.equals(String.class)) {
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        } else {
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        }
        return new DefaultKafkaProducerFactory<>(configProps);
    }

    private <T> KafkaTemplate<String, T> createKafkaTemplate(ProducerFactory<String, T> producerFactory) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public KafkaTemplate<String, String> kafkaTemplate() {
        return createKafkaTemplate(createProducerFactory(String.class));
    }

    @Bean
    public KafkaTemplate<String, OrderConfirmation> orderKafkaTemplate() {
        return createKafkaTemplate(createProducerFactory(OrderConfirmation.class));
    }

    @Bean
    public KafkaTemplate<String, NotificationRequest> notificationKafkaTemplate() {
        return createKafkaTemplate(createProducerFactory(NotificationRequest.class));
    }
}
