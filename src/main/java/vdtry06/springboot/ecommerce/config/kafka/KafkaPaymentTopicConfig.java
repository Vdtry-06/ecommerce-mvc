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
                .partitions(6)
                .replicas(3)
                .build();
    }

    @Bean
    public NewTopic paymentDlqTopic() {
        return TopicBuilder
                .name("payment-dlq-topic")
                .partitions(6)
                .replicas(1)
                .build();
    }
}