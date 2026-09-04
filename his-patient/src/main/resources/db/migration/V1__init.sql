-- his_patient schema — outpatient core tables
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ

-- 1. Patient master (EMPI)
CREATE TABLE pat_patient (
    id              BIGINT          NOT NULL,
    empi_no         VARCHAR(32)     NOT NULL,
    name            VARCHAR(64)     NOT NULL,
    name_pinyin     VARCHAR(128),
    gender          SMALLINT        NOT NULL DEFAULT 0,
    birth_date      DATE,
    age_display     VARCHAR(16),
    id_type         VARCHAR(16)     NOT NULL DEFAULT 'ID_CARD',
    id_no           VARCHAR(64),
    nationality     VARCHAR(32),
    nation          VARCHAR(32),
    marital_status  VARCHAR(16),
    occupation      VARCHAR(64),
    phone           VARCHAR(32),
    phone_backup    VARCHAR(32),
    address         VARCHAR(256),
    blood_type      VARCHAR(8),
    allergy_history TEXT,
    insurance_type  VARCHAR(16),
    insurance_no    VARCHAR(64),
    patient_type    VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    patient_status  VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    source          VARCHAR(32)     NOT NULL DEFAULT 'SELF',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_patient PRIMARY KEY (id),
    CONSTRAINT uq_pat_patient_empi UNIQUE (empi_no)
);
CREATE INDEX idx_pat_patient_name ON pat_patient (name);
CREATE INDEX idx_pat_patient_id ON pat_patient (id_type, id_no);
CREATE INDEX idx_pat_patient_phone ON pat_patient (phone);

-- 2. Patient identifiers (multiple cards)
CREATE TABLE pat_patient_identifier (
    id              BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL,
    id_type         VARCHAR(16)     NOT NULL,
    id_no           VARCHAR(64)     NOT NULL,
    issue_authority VARCHAR(128),
    issue_date      DATE,
    expiry_date     DATE,
    is_primary      SMALLINT        NOT NULL DEFAULT 0,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_patient_iden PRIMARY KEY (id),
    CONSTRAINT uq_pat_patient_iden UNIQUE (id_type, id_no)
);
CREATE INDEX idx_pat_patient_iden_pid ON pat_patient_identifier (patient_id);

-- 3. Patient merge log
CREATE TABLE pat_patient_merge_log (
    id                  BIGINT      NOT NULL,
    master_patient_id   BIGINT      NOT NULL,
    merged_patient_id   BIGINT      NOT NULL,
    merge_reason        VARCHAR(256),
    merged_by           VARCHAR(64),
    merged_time         TIMESTAMP,
    CONSTRAINT pk_pat_merge_log PRIMARY KEY (id)
);

-- 4. Department
CREATE TABLE pat_department (
    id              BIGINT          NOT NULL,
    dept_code       VARCHAR(32)     NOT NULL,
    dept_name       VARCHAR(64)     NOT NULL,
    dept_type       VARCHAR(16)     NOT NULL DEFAULT 'CLINICAL',
    parent_id       BIGINT,
    sort_order      INT             NOT NULL DEFAULT 0,
    dept_status     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    description     VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_dept PRIMARY KEY (id),
    CONSTRAINT uq_pat_dept_code UNIQUE (dept_code)
);

-- 5. Doctor
CREATE TABLE pat_doctor (
    id              BIGINT          NOT NULL,
    employee_no     VARCHAR(32)     NOT NULL,
    doctor_name     VARCHAR(64)     NOT NULL,
    name_pinyin     VARCHAR(128),
    gender          SMALLINT        NOT NULL DEFAULT 0,
    dept_id         BIGINT          NOT NULL,
    title           VARCHAR(32),
    specialty       VARCHAR(256),
    prescribe_right SMALLINT        NOT NULL DEFAULT 1,
    antibiotic_level SMALLINT       NOT NULL DEFAULT 1,
    phone           VARCHAR(32),
    doctor_status   VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_doctor PRIMARY KEY (id),
    CONSTRAINT uq_pat_doctor_empno UNIQUE (employee_no)
);
CREATE INDEX idx_pat_doctor_dept ON pat_doctor (dept_id);

-- 6. Schedule template
CREATE TABLE pat_schedule_template (
    id              BIGINT          NOT NULL,
    template_name   VARCHAR(64)     NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    day_of_week     SMALLINT        NOT NULL,
    time_period     VARCHAR(16)     NOT NULL,
    start_time      VARCHAR(8)      NOT NULL,
    end_time        VARCHAR(8)      NOT NULL,
    total_quota     INT             NOT NULL DEFAULT 30,
    reg_fee         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    reg_level       VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    template_status VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_sched_tpl PRIMARY KEY (id)
);
CREATE INDEX idx_pat_sched_tpl_doctor ON pat_schedule_template (doctor_id);
CREATE INDEX idx_pat_sched_tpl_dept ON pat_schedule_template (dept_id);

-- 7. Schedule (materialized from template)
CREATE TABLE pat_schedule (
    id              BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    schedule_date   DATE            NOT NULL,
    time_period     VARCHAR(16)     NOT NULL,
    start_time      VARCHAR(8)      NOT NULL,
    end_time        VARCHAR(8)      NOT NULL,
    total_quota     INT             NOT NULL DEFAULT 30,
    used_quota      INT             NOT NULL DEFAULT 0,
    reg_fee         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    reg_level       VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    schedule_status VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    revision        INT             NOT NULL DEFAULT 0,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_schedule PRIMARY KEY (id),
    CONSTRAINT uq_pat_schedule UNIQUE (doctor_id, schedule_date, time_period)
);
CREATE INDEX idx_pat_schedule_date ON pat_schedule (schedule_date);
CREATE INDEX idx_pat_schedule_dept ON pat_schedule (dept_id, schedule_date);

-- 8. Registration (挂号)
CREATE TABLE pat_registration (
    id              BIGINT          NOT NULL,
    reg_no          VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    schedule_id     BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    visit_seq       INT,
    reg_date        DATE            NOT NULL,
    time_period     VARCHAR(16)     NOT NULL,
    reg_fee         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    pay_status      VARCHAR(16)     NOT NULL DEFAULT 'UNPAID',
    pay_time        TIMESTAMP,
    reg_source      VARCHAR(16)     NOT NULL DEFAULT 'WINDOW',
    reg_status      VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    bill_id         BIGINT,
    cancel_reason   VARCHAR(256),
    cancel_time     TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_reg PRIMARY KEY (id),
    CONSTRAINT uq_pat_reg_no UNIQUE (reg_no)
);
CREATE INDEX idx_pat_reg_patient ON pat_registration (patient_id);
CREATE INDEX idx_pat_reg_doctor ON pat_registration (doctor_id, reg_date);
CREATE INDEX idx_pat_reg_date ON pat_registration (reg_date);

-- 9. Appointment (预约)
CREATE TABLE pat_appointment (
    id              BIGINT          NOT NULL,
    appt_no         VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    schedule_id     BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    appt_date       DATE            NOT NULL,
    time_period     VARCHAR(16)     NOT NULL,
    appt_source     VARCHAR(16)     NOT NULL DEFAULT 'ONLINE',
    appt_status     VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    confirm_time    TIMESTAMP,
    cancel_reason   VARCHAR(256),
    cancel_time     TIMESTAMP,
    reg_id          BIGINT,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_appt PRIMARY KEY (id),
    CONSTRAINT uq_pat_appt_no UNIQUE (appt_no)
);
CREATE INDEX idx_pat_appt_patient ON pat_appointment (patient_id);
CREATE INDEX idx_pat_appt_schedule ON pat_appointment (schedule_id);

-- 10. Triage (分诊)
CREATE TABLE pat_triage (
    id              BIGINT          NOT NULL,
    reg_id          BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT,
    visit_seq       INT             NOT NULL,
    triage_status   VARCHAR(16)     NOT NULL DEFAULT 'QUEUED',
    queue_no        INT,
    enqueue_time    TIMESTAMP,
    call_time       TIMESTAMP,
    finish_time     TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_triage PRIMARY KEY (id)
);
CREATE INDEX idx_pat_triage_reg ON pat_triage (reg_id);
CREATE INDEX idx_pat_triage_queue ON pat_triage (dept_id, doctor_id, triage_status);

-- 11. Encounter (就诊)
CREATE TABLE pat_encounter (
    id              BIGINT          NOT NULL,
    encounter_no    VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    reg_id          BIGINT,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    encounter_type  VARCHAR(16)     NOT NULL DEFAULT 'OUTPATIENT',
    encounter_status VARCHAR(16)    NOT NULL DEFAULT 'PLANNED',
    visit_date      DATE,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    chief_complaint TEXT,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_encounter PRIMARY KEY (id),
    CONSTRAINT uq_pat_encounter_no UNIQUE (encounter_no)
);
CREATE INDEX idx_pat_encounter_patient ON pat_encounter (patient_id);
CREATE INDEX idx_pat_encounter_reg ON pat_encounter (reg_id);
CREATE INDEX idx_pat_encounter_date ON pat_encounter (visit_date);
