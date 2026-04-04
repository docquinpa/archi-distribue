package fr.univrouen.maintenance.intervention.messaging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import fr.univrouen.maintenance.intervention.service.InterventionService;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.kafka.clients.consumer.Consumer;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Bean;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.listener.MessageListenerContainer;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerEndpointRegistry;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.ContainerTestUtils;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@EmbeddedKafka(partitions = 1, topics = {"vehicule_topic", "vehicule_topic.dlt"})
@TestPropertySource(properties = {
        "app.kafka.enabled=true",
        "app.kafka.topic.vehicle=vehicule_topic",
        "app.kafka.topic.vehicle-dlt=vehicule_topic.dlt",
        "app.kafka.group-id=maintenance-int-test",
        "app.kafka.retry.interval-ms=100",
        "app.kafka.retry.max-attempts=1",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}",
        "spring.kafka.consumer.auto-offset-reset=earliest"
})
class VehicleEventKafkaIntegrationTest {

    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private EmbeddedKafkaBroker embeddedKafkaBroker;
    @Autowired
    private InterventionService interventionService;
    @Autowired
    private ProcessedKafkaEventRepository processedKafkaEventRepository;
    @Autowired
    private KafkaListenerEndpointRegistry kafkaListenerEndpointRegistry;

    @BeforeEach
    void resetMocks() {
        reset(interventionService);

        assertThat(kafkaListenerEndpointRegistry.getListenerContainers()).isNotEmpty();
        for (MessageListenerContainer container : kafkaListenerEndpointRegistry.getListenerContainers()) {
            container.start();
            ContainerTestUtils.waitForAssignment(container, embeddedKafkaBroker.getPartitionsPerTopic());
        }
    }

    @Test
    void shouldProcessDuplicateEventOnlyOnce() {
        VehicleEvent eventPayload = buildVehicleDeletedEvent(42, "VIN-42", "evt-dup-int", "saga-dup-int", "corr-dup-int");

        kafkaTemplate.send("vehicule_topic", "42", eventPayload);
        kafkaTemplate.send("vehicule_topic", "42", eventPayload);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(10))
                .untilAsserted(() -> verify(interventionService, times(1))
                        .cancelForVehicleDeletion(42, "saga-dup-int", "corr-dup-int"));

        assertThat(processedKafkaEventRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldRouteToDltWhenListenerKeepsFailing() {
        doThrow(new RuntimeException("forced-failure"))
                .when(interventionService)
                .cancelForVehicleDeletion(eq(99), anyString(), anyString());

        VehicleEvent eventPayload = buildVehicleDeletedEvent(99, "VIN-99", "evt-dlt-int", "saga-dlt-int", "corr-dlt-int");

        kafkaTemplate.send("vehicule_topic", "99", eventPayload);
        kafkaTemplate.flush();

        Awaitility.await()
                .atMost(Duration.ofSeconds(15))
                .untilAsserted(() -> verify(interventionService, atLeastOnce())
                        .cancelForVehicleDeletion(eq(99), anyString(), anyString()));

        AtomicReference<ConsumerRecord<String, String>> dltRecord = new AtomicReference<>();

        try (Consumer<String, String> consumer = createDltConsumer("maintenance-dlt-check")) {
            Awaitility.await()
                    .atMost(Duration.ofSeconds(20))
                    .untilAsserted(() -> {
                        ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(500));
                        assertThat(records.isEmpty()).isFalse();
                        dltRecord.set(records.iterator().next());
                    });
        }

        assertThat(dltRecord.get()).isNotNull();
        assertThat(dltRecord.get().value()).contains("\"event_id\":\"evt-dlt-int\"");
        assertThat(dltRecord.get().value()).contains("\"vehicle_id\":99");
    }

    private Consumer<String, String> createDltConsumer(String groupId) {
        Map<String, Object> props = KafkaTestUtils.consumerProps(groupId, "true", embeddedKafkaBroker);
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        DefaultKafkaConsumerFactory<String, String> factory =
                new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), new StringDeserializer());

        Consumer<String, String> consumer = factory.createConsumer();
        embeddedKafkaBroker.consumeFromAnEmbeddedTopic(consumer, "vehicule_topic.dlt");
        return consumer;
    }

    private VehicleEvent buildVehicleDeletedEvent(
            int vehicleId,
            String vin,
            String eventId,
            String sagaId,
            String correlationId
    ) {
        return new VehicleEvent(
                vehicleId,
                vin,
                false,
                "VEHICLE_DELETED",
                null,
                "1.0",
                eventId,
                sagaId,
                correlationId
        );
    }

    @TestConfiguration
    static class KafkaProducerTestConfig {

        @Bean
        @Primary
        InterventionService interventionServiceMock() {
            return mock(InterventionService.class);
        }

        @Bean
        ProducerFactory<String, Object> producerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = KafkaTestUtils.producerProps(embeddedKafkaBroker);
            props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            return new DefaultKafkaProducerFactory<>(
                props,
                new StringSerializer(),
                new JsonSerializer<>()
            );
        }

        @Bean
        KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
            return new KafkaTemplate<>(producerFactory);
        }

        @Bean
        ConsumerFactory<String, VehicleEvent> consumerFactory(EmbeddedKafkaBroker embeddedKafkaBroker) {
            Map<String, Object> props = KafkaTestUtils.consumerProps("maintenance-int-test", "true", embeddedKafkaBroker);
            props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            JsonDeserializer<VehicleEvent> valueDeserializer = new JsonDeserializer<>(VehicleEvent.class);
            valueDeserializer.addTrustedPackages("fr.univrouen.maintenance.intervention.messaging");
            valueDeserializer.setUseTypeHeaders(false);

            return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), valueDeserializer);
        }

        @Bean("kafkaListenerContainerFactory")
        ConcurrentKafkaListenerContainerFactory<String, VehicleEvent> kafkaListenerContainerFactory(
                ConsumerFactory<String, VehicleEvent> consumerFactory,
                DefaultErrorHandler kafkaErrorHandler
        ) {
            ConcurrentKafkaListenerContainerFactory<String, VehicleEvent> factory =
                    new ConcurrentKafkaListenerContainerFactory<>();
            factory.setConsumerFactory(consumerFactory);
            factory.setCommonErrorHandler(kafkaErrorHandler);
            return factory;
        }

    }
}
