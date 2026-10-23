package com.example.demo.publishing;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.util.backoff.FixedBackOff;
import java.util.HashMap;
import java.util.Map;

/** Kafka needs a producer, a consumer, and JSON converters to send our Java record as a message. */
@Configuration
@EnableKafka
public class KafkaConfiguration {
    @Bean
    ProducerFactory<Object, Object> producerFactory(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> p = new HashMap<>();
        p.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        p.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        p.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        // These settings let Kafka retry temporary broker hiccups while sending.
        p.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        p.put(ProducerConfig.RETRIES_CONFIG, 10);
        p.put(ProducerConfig.RETRY_BACKOFF_MS_CONFIG, 500);
        return new DefaultKafkaProducerFactory<>(p);
    }

    @Bean
    KafkaTemplate<Object, Object> kafkaTemplate(ProducerFactory<Object, Object> factory) {
        return new KafkaTemplate<>(factory);
    }

    @Bean
    ConsumerFactory<String, PublishEvent> consumerFactory(@Value("${spring.kafka.bootstrap-servers}") String brokers) {
        Map<String, Object> p = new HashMap<>();
        p.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, brokers);
        p.put(ConsumerConfig.GROUP_ID_CONFIG, "social-publisher");
        p.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        return new DefaultKafkaConsumerFactory<>(p, new StringDeserializer(),
                new JsonDeserializer<>(PublishEvent.class, false));
    }

    @Bean
    ConcurrentKafkaListenerContainerFactory<String, PublishEvent> kafkaListenerContainerFactory(
            ConsumerFactory<String, PublishEvent> factory,
            KafkaTemplate<Object, Object> kafkaTemplate) {
        var f = new ConcurrentKafkaListenerContainerFactory<String, PublishEvent>();
        f.setConsumerFactory(factory);
        // Try a failed event twice more, one second apart, then send it to publishing.jobs.DLT.
        var recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate,
                (record, error) -> new org.apache.kafka.common.TopicPartition("publishing.jobs.DLT", record.partition()));
        f.setCommonErrorHandler(new DefaultErrorHandler(recoverer, new FixedBackOff(1000L, 2L)));
        return f;
    }
}
