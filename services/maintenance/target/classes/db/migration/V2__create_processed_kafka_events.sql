CREATE TABLE IF NOT EXISTS processed_kafka_events (
    event_id VARCHAR(128) PRIMARY KEY,
    topic VARCHAR(255) NOT NULL,
    partition_id INTEGER NOT NULL,
    offset_value BIGINT NOT NULL,
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_processed_kafka_events_processed_at
    ON processed_kafka_events(processed_at);