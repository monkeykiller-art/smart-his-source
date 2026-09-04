-- 12. Admission (入院登记)
CREATE TABLE pat_admission (
    id                      BIGINT          NOT NULL,
    admission_no            VARCHAR(32)     NOT NULL,
    patient_id              BIGINT          NOT NULL,
    encounter_id            BIGINT,
    admission_type          VARCHAR(16)     NOT NULL DEFAULT 'ELECTIVE',
    admission_status        VARCHAR(16)     NOT NULL DEFAULT 'PLANNED',
    dept_id                 BIGINT          NOT NULL,
    doctor_id               BIGINT          NOT NULL,
    ward_id                 BIGINT,
    bed_id                  BIGINT,
    insurance_type          VARCHAR(32),
    insurance_no            VARCHAR(64),
    insurance_org           VARCHAR(128),
    emergency_contact_name  VARCHAR(64),
    emergency_contact_phone VARCHAR(32),
    emergency_contact_addr  VARCHAR(256),
    admission_date          DATE            NOT NULL,
    expected_discharge_date DATE,
    actual_discharge_date   DATE,
    chief_complaint         TEXT,
    preliminary_diagnosis   VARCHAR(512),
    deposit_amount          DECIMAL(18,4)   NOT NULL DEFAULT 0,
    total_deposit           DECIMAL(18,4)   NOT NULL DEFAULT 0,
    discharge_type          VARCHAR(16),
    discharge_summary       TEXT,
    created_by              VARCHAR(64),
    created_time            TIMESTAMP,
    updated_by              VARCHAR(64),
    updated_time            TIMESTAMP,
    deleted                 SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_admission PRIMARY KEY (id),
    CONSTRAINT uq_pat_admission_no UNIQUE (admission_no)
);
CREATE INDEX idx_pat_admission_patient ON pat_admission (patient_id);
CREATE INDEX idx_pat_admission_status ON pat_admission (admission_status);
CREATE INDEX idx_pat_admission_dept ON pat_admission (dept_id, admission_date);
CREATE INDEX idx_pat_admission_ward ON pat_admission (ward_id, admission_status);
