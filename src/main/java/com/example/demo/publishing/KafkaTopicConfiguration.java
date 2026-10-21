package com.example.demo.publishing;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfiguration {
    @Bean
    NewTopic publishingJobsTopic() {
        return TopicBuilder.name("publishing.jobs").partitions(3).replicas(1).build();
    }
}
