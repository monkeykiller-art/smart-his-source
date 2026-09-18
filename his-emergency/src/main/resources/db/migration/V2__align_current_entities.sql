-- Emergency V2: align schema with current service entities
-- Preserve historical data while supporting current business logic

-- emg_triage: add queue index and level check
CREATE INDEX idx_emg_triage_queue ON emg_triage (triage_status, triage_level, triage_time);
ALTER TABLE emg_triage ADD CONSTRAINT ck_emg_triage_level CHECK (triage_level BETWEEN 1 AND 4);
ALTER TABLE emg_triage ALTER COLUMN chief_complaint SET NOT NULL;
ALTER TABLE emg_triage ALTER COLUMN triage_nurse_id SET NOT NULL;

-- emg_resuscitation: relax NOT NULL on legacy unique fields
ALTER TABLE emg_resuscitation ALTER COLUMN resuscitation_no DROP NOT NULL;
DROP INDEX IF EXISTS idx_emg_resuscitation_triage;
CREATE INDEX idx_emg_resuscitation_triage ON emg_resuscitation (triage_id, start_time);

-- emg_observation: rename legacy columns to current names
ALTER TABLE emg_observation RENAME COLUMN actual_discharge_time TO discharge_time;
ALTER TABLE emg_observation RENAME COLUMN observation_diagnosis TO diagnosis;
ALTER TABLE emg_observation ALTER COLUMN observation_no DROP NOT NULL;
ALTER TABLE emg_observation ALTER COLUMN triage_id DROP NOT NULL;
DROP INDEX IF EXISTS idx_emg_observation_triage;
CREATE INDEX idx_emg_observation_triage ON emg_observation (triage_id, admit_time);
