CREATE TABLE IF NOT EXISTS interventions (
    id SERIAL PRIMARY KEY,
    vehicle_id INTEGER NOT NULL,
    state VARCHAR(32) NOT NULL,
    scheduled_date DATE NOT NULL,
    completed_date DATE,
    prix NUMERIC(12,2) NOT NULL,
    mileage_threshold INTEGER,
    current_mileage INTEGER,
    notes VARCHAR(1000),
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_interventions_vehicle_id ON interventions(vehicle_id);
CREATE INDEX IF NOT EXISTS idx_interventions_state ON interventions(state);
CREATE INDEX IF NOT EXISTS idx_interventions_scheduled_date ON interventions(scheduled_date);

CREATE TABLE IF NOT EXISTS maintenance_history (
    id BIGSERIAL PRIMARY KEY,
    intervention_id INTEGER NOT NULL,
    vehicle_id INTEGER NOT NULL,
    event_type VARCHAR(64) NOT NULL,
    details VARCHAR(2000),
    saga_id VARCHAR(64),
    occurred_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_maintenance_history_intervention_id ON maintenance_history(intervention_id);
CREATE INDEX IF NOT EXISTS idx_maintenance_history_occurred_at ON maintenance_history(occurred_at);
