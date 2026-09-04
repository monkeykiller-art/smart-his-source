-- Smart HIS Emergency Module Schema

-- ============================================================
-- Table: emg_triage
-- ============================================================
CREATE TABLE emg_triage (
    id                BIGINT        NOT NULL,
    triage_no         VARCHAR(32)   NOT NULL,
    patient_id        BIGINT        NOT NULL,
    encounter_id      BIGINT,
    triage_level      SMALLINT      NOT NULL,
    triage_time       TIMESTAMP     NOT NULL,
    chief_complaint   VARCHAR(500),
    vital_signs       TEXT,
    triage_nurse_id   BIGINT,
    triage_nurse_name VARCHAR(64),
    target_dept_id    BIGINT,
    target_dept_name  VARCHAR(64),
    wait_time_minutes INTEGER,
    triage_status     VARCHAR(20)   NOT NULL,
    created_by        VARCHAR(64),
    created_time      TIMESTAMP,
    updated_by        VARCHAR(64),
    updated_time      TIMESTAMP,
    deleted           SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_triage PRIMARY KEY (id),
    CONSTRAINT uk_emg_triage_no UNIQUE (triage_no)
);

CREATE INDEX idx_emg_triage_patient_id ON emg_triage (patient_id);
CREATE INDEX idx_emg_triage_triage_time ON emg_triage (triage_time);
CREATE INDEX idx_emg_triage_triage_level ON emg_triage (triage_level);

COMMENT ON TABLE emg_triage IS '急诊分诊表';
COMMENT ON COLUMN emg_triage.triage_level IS '分诊级别: 1-濒死 2-危重 3-急症 4-非急症';
COMMENT ON COLUMN emg_triage.triage_status IS '分诊状态: WAITING/IN_TREATMENT/COMPLETED/CANCELLED';
COMMENT ON COLUMN emg_triage.vital_signs IS '生命体征JSON: temperature, pulse, respiration, bloodPressure, spo2';

-- ============================================================
-- Table: emg_resuscitation
-- ============================================================
CREATE TABLE emg_resuscitation (
    id                      BIGINT        NOT NULL,
    resuscitation_no        VARCHAR(32)   NOT NULL,
    patient_id              BIGINT        NOT NULL,
    encounter_id            BIGINT,
    triage_id               BIGINT,
    start_time              TIMESTAMP     NOT NULL,
    end_time                TIMESTAMP,
    resuscitation_type      VARCHAR(20),
    team_leader_id          BIGINT,
    team_leader_name        VARCHAR(64),
    team_members            TEXT,
    procedures              TEXT,
    medications             TEXT,
    outcome                 VARCHAR(20),
    outcome_summary         TEXT,
    resuscitation_status    VARCHAR(20)   NOT NULL,
    created_by              VARCHAR(64),
    created_time            TIMESTAMP,
    updated_by              VARCHAR(64),
    updated_time            TIMESTAMP,
    deleted                 SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_resuscitation PRIMARY KEY (id),
    CONSTRAINT uk_emg_resuscitation_no UNIQUE (resuscitation_no)
);

CREATE INDEX idx_emg_resuscitation_patient_id ON emg_resuscitation (patient_id);
CREATE INDEX idx_emg_resuscitation_start_time ON emg_resuscitation (start_time);

COMMENT ON TABLE emg_resuscitation IS '急诊抢救记录表';
COMMENT ON COLUMN emg_resuscitation.resuscitation_type IS '抢救类型: CARDIAC_ARREST/RESPIRATORY_FAILURE/SHOCK/TRAUMA/OTHER';
COMMENT ON COLUMN emg_resuscitation.outcome IS '抢救结果: SUCCESS/FAILED/TRANSFERRED/DEAD';
COMMENT ON COLUMN emg_resuscitation.resuscitation_status IS '抢救状态: IN_PROGRESS/COMPLETED/CANCELLED';

-- ============================================================
-- Table: emg_green_channel
-- ============================================================
CREATE TABLE emg_green_channel (
    id                  BIGINT        NOT NULL,
    channel_no          VARCHAR(32)   NOT NULL,
    patient_id          BIGINT        NOT NULL,
    encounter_id        BIGINT,
    triage_id           BIGINT,
    channel_type        VARCHAR(30)   NOT NULL,
    activate_time       TIMESTAMP     NOT NULL,
    activate_reason     VARCHAR(500),
    target_dept_id      BIGINT,
    target_dept_name    VARCHAR(64),
    coordinator_id      BIGINT,
    coordinator_name    VARCHAR(64),
    key_timepoints      TEXT,
    channel_status      VARCHAR(20)   NOT NULL,
    close_time          TIMESTAMP,
    close_summary       TEXT,
    created_by          VARCHAR(64),
    created_time        TIMESTAMP,
    updated_by          VARCHAR(64),
    updated_time        TIMESTAMP,
    deleted             SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_green_channel PRIMARY KEY (id),
    CONSTRAINT uk_emg_green_channel_no UNIQUE (channel_no)
);

CREATE INDEX idx_emg_green_channel_patient_id ON emg_green_channel (patient_id);
CREATE INDEX idx_emg_green_channel_activate_time ON emg_green_channel (activate_time);
CREATE INDEX idx_emg_green_channel_channel_type ON emg_green_channel (channel_type);

COMMENT ON TABLE emg_green_channel IS '急诊绿色通道表';
COMMENT ON COLUMN emg_green_channel.channel_type IS '通道类型: STROKE/CHEST_PAIN/TRAUMA/PREGNANCY/OTHER';
COMMENT ON COLUMN emg_green_channel.channel_status IS '通道状态: ACTIVATED/IN_PROGRESS/COMPLETED/CLOSED';
COMMENT ON COLUMN emg_green_channel.key_timepoints IS '关键时间节点JSON数组';

-- ============================================================
-- Table: emg_observation
-- ============================================================
CREATE TABLE emg_observation (
    id                      BIGINT        NOT NULL,
    observation_no          VARCHAR(32)   NOT NULL,
    patient_id              BIGINT        NOT NULL,
    encounter_id            BIGINT,
    triage_id               BIGINT,
    bed_no                  VARCHAR(20),
    admit_time              TIMESTAMP     NOT NULL,
    expected_discharge_time TIMESTAMP,
    actual_discharge_time   TIMESTAMP,
    observation_diagnosis   VARCHAR(500),
    treatment_plan          TEXT,
    nurse_station           VARCHAR(64),
    doctor_id               BIGINT,
    doctor_name             VARCHAR(64),
    observation_status      VARCHAR(20)   NOT NULL,
    discharge_summary       TEXT,
    created_by              VARCHAR(64),
    created_time            TIMESTAMP,
    updated_by              VARCHAR(64),
    updated_time            TIMESTAMP,
    deleted                 SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_emg_observation PRIMARY KEY (id),
    CONSTRAINT uk_emg_observation_no UNIQUE (observation_no)
);

CREATE INDEX idx_emg_observation_patient_id ON emg_observation (patient_id);
CREATE INDEX idx_emg_observation_admit_time ON emg_observation (admit_time);

COMMENT ON TABLE emg_observation IS '急诊留观记录表';
COMMENT ON COLUMN emg_observation.observation_status IS '留观状态: ADMITTED/DISCHARGED/TRANSFERRED/CANCELLED';
