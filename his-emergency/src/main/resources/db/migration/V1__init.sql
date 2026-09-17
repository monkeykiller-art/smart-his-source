CREATE TABLE emg_triage (
    id BIGINT NOT NULL,
    triage_no VARCHAR(32) NOT NULL,
    patient_id BIGINT NOT NULL,
    triage_level SMALLINT NOT NULL,
    triage_time TIMESTAMP NOT NULL,
    chief_complaint VARCHAR(500) NOT NULL,
    vital_signs TEXT,
    triage_nurse_id BIGINT NOT NULL,
    triage_nurse_name VARCHAR(64),
    target_dept_id BIGINT,
    target_dept_name VARCHAR(64),
    triage_status VARCHAR(20) NOT NULL,
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_triage PRIMARY KEY (id),
    CONSTRAINT uk_emg_triage_no UNIQUE (triage_no),
    CONSTRAINT ck_emg_triage_level CHECK (triage_level BETWEEN 1 AND 4)
);
CREATE INDEX idx_emg_triage_queue ON emg_triage (triage_status, triage_level, triage_time);

CREATE TABLE emg_resuscitation (
    id BIGINT NOT NULL,
    triage_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL,
    end_time TIMESTAMP,
    procedures TEXT,
    medications TEXT,
    outcome VARCHAR(20),
    outcome_summary TEXT,
    resuscitation_status VARCHAR(20) NOT NULL,
    created_by VARCHAR(64), created_time TIMESTAMP, updated_by VARCHAR(64), updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_resuscitation PRIMARY KEY (id)
);
CREATE INDEX idx_emg_resuscitation_triage ON emg_resuscitation (triage_id, start_time);

CREATE TABLE emg_observation (
    id BIGINT NOT NULL,
    triage_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    bed_no VARCHAR(20),
    admit_time TIMESTAMP NOT NULL,
    discharge_time TIMESTAMP,
    diagnosis VARCHAR(500),
    treatment_plan TEXT,
    observation_status VARCHAR(20) NOT NULL,
    discharge_summary TEXT,
    created_by VARCHAR(64), created_time TIMESTAMP, updated_by VARCHAR(64), updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_observation PRIMARY KEY (id)
);
CREATE INDEX idx_emg_observation_triage ON emg_observation (triage_id, admit_time);
