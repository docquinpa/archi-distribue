package fr.univrouen.maintenance.intervention.messaging;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedKafkaEventRepository extends JpaRepository<ProcessedKafkaEvent, String> {
}