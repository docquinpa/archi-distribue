package fr.univrouen.maintenance.intervention.messaging;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class KafkaEventDeduplicationService {

    private final ProcessedKafkaEventRepository repository;

    public KafkaEventDeduplicationService(ProcessedKafkaEventRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public boolean isProcessed(String eventId) {
        if (eventId == null || eventId.isBlank()) {
            return false;
        }

        return repository.existsById(eventId);
    }

    @Transactional
    public void markProcessed(String eventId, String topic, int partition, long offset) {
        if (eventId == null || eventId.isBlank()) {
            return;
        }

        ProcessedKafkaEvent event = new ProcessedKafkaEvent();
        event.setEventId(eventId);
        event.setTopic(topic == null ? "unknown" : topic);
        event.setPartitionId(partition);
        event.setOffsetValue(offset);

        try {
            repository.saveAndFlush(event);
        } catch (DataIntegrityViolationException ex) {
            // Duplicate insert indicates another consumer instance already marked this event.
        }
    }
}