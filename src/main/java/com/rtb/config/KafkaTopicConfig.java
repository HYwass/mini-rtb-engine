package com.rtb.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic bidEventTopic(
            @Value("${rtb.kafka.topic}") String topic) {

        return TopicBuilder.name(topic)
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic billingDeadLetterTopic(
            @Value("${rtb.kafka.topic}") String topic) {

        return TopicBuilder.name(topic + ".billing.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic hourlyDeadLetterTopic(
            @Value("${rtb.kafka.topic}") String topic) {

        return TopicBuilder.name(topic + ".hourly.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic freqDeadLetterTopic(
            @Value("${rtb.kafka.topic}") String topic) {

        return TopicBuilder.name(topic + ".freq.DLT")
                .partitions(3)
                .replicas(1)
                .build();
    }
}
