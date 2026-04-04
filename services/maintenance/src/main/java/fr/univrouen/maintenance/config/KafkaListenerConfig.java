package fr.univrouen.maintenance.config;

import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
@EnableKafka
@ConditionalOnProperty(name = "app.kafka.enabled", havingValue = "true", matchIfMissing = true)
public class KafkaListenerConfig {

        @Bean
        @ConditionalOnMissingBean(ProducerFactory.class)
        ProducerFactory<Object, Object> fallbackProducerFactory(
                        @Value("${spring.kafka.bootstrap-servers:localhost:29092}") String bootstrapServers
        ) {
                Map<String, Object> props = new HashMap<>();
                props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
                props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JacksonJsonSerializer.class);
                return new DefaultKafkaProducerFactory<>(props);
        }

        @Bean
        @ConditionalOnMissingBean(KafkaTemplate.class)
        KafkaTemplate<Object, Object> fallbackKafkaTemplate(ProducerFactory<Object, Object> producerFactory) {
                return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        @ConditionalOnMissingBean(ConsumerFactory.class)
        ConsumerFactory<Object, Object> fallbackConsumerFactory(
                        @Value("${spring.kafka.bootstrap-servers:localhost:29092}") String bootstrapServers,
                        @Value("${spring.kafka.consumer.group-id:maintenance-service}") String groupId,
                        @Value("${spring.kafka.consumer.properties.spring.json.trusted.packages:*}") String trustedPackages,
                        @Value("${spring.kafka.consumer.properties.spring.json.value.default.type:fr.univrouen.maintenance.intervention.messaging.VehicleEvent}")
                        String defaultValueType,
                        @Value("${spring.kafka.consumer.properties.spring.json.use.type.headers:false}") boolean useTypeHeaders
        ) {
                Map<String, Object> props = new HashMap<>();
                props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
                props.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
                props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
                props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JacksonJsonDeserializer.class);
                props.put("spring.json.trusted.packages", trustedPackages);
                props.put("spring.json.value.default.type", defaultValueType);
                props.put("spring.json.use.type.headers", useTypeHeaders);
                return new DefaultKafkaConsumerFactory<>(props);
        }

        @Bean(name = "kafkaListenerContainerFactory")
        @ConditionalOnMissingBean(name = "kafkaListenerContainerFactory")
        @ConditionalOnBean({ConsumerFactory.class, DefaultErrorHandler.class})
        ConcurrentKafkaListenerContainerFactory<Object, Object> kafkaListenerContainerFactory(
                        ConsumerFactory<Object, Object> consumerFactory,
                        DefaultErrorHandler kafkaErrorHandler
        ) {
                ConcurrentKafkaListenerContainerFactory<Object, Object> factory =
                                new ConcurrentKafkaListenerContainerFactory<>();
                factory.setConsumerFactory(consumerFactory);
                factory.setCommonErrorHandler(kafkaErrorHandler);
                return factory;
        }

    @Bean
    DeadLetterPublishingRecoverer deadLetterPublishingRecoverer(
            KafkaTemplate<?, ?> kafkaTemplate,
            @Value("${app.kafka.topic.vehicle-dlt:vehicule_topic.dlt}") String vehicleDltTopic
    ) {
        return new DeadLetterPublishingRecoverer(
                kafkaTemplate,
                (record, ex) -> new TopicPartition(vehicleDltTopic, record.partition())
        );
    }

    @Bean
    DefaultErrorHandler kafkaErrorHandler(
            DeadLetterPublishingRecoverer deadLetterPublishingRecoverer,
            @Value("${app.kafka.retry.interval-ms:1000}") long retryIntervalMs,
            @Value("${app.kafka.retry.max-attempts:2}") long retryMaxAttempts
    ) {
        long interval = Math.max(0L, retryIntervalMs);
        long attempts = Math.max(0L, retryMaxAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler(
                deadLetterPublishingRecoverer,
                new FixedBackOff(interval, attempts)
        );
        errorHandler.setCommitRecovered(true);
        return errorHandler;
    }
}