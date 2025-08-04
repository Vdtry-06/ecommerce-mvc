package vdtry06.springboot.ecommerce.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaPaymentTopicConfig {

    @Bean
    public NewTopic paymentTopic() {
        return TopicBuilder
                .name("payment-topic")
                .partitions(3)
                .replicas(1) // Changed from 3 to 1 since we only have 1 Kafka broker
                .build();
    }

    @Bean
    public NewTopic paymentDlqTopic() {
        return TopicBuilder
                .name("payment-dlq-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cartExpirationTopic() {
        return TopicBuilder
                .name("cart-expiration-topic")
                .partitions(3)
                .replicas(1) // Changed from 3 to 1
                .build();
    }

    @Bean
    public NewTopic cartExpirationDlqTopic() {
        return TopicBuilder
                .name("cart-expiration-dlq-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic cartTopic() {
        return TopicBuilder
                .name("cart-topic")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic verificationCodesTopic() {
        return TopicBuilder
                .name("verification-codes")
                .partitions(3)
                .replicas(1)
                .build();
    }
}