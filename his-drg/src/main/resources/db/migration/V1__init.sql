-- his_drg schema — DRG grouping engine, case grouping, audit, cost analysis
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ

-- 1. DRG group dictionary (DRG分组字典)
CREATE TABLE drg_group (
    id              BIGINT          NOT NULL,
    group_code      VARCHAR(32)     NOT NULL,
    group_name      VARCHAR(200)    NOT NULL,
    mdc_code        VARCHAR(10)     NOT NULL,
    mdc_name        VARCHAR(100),
    adrg_code       VARCHAR(10)     NOT NULL,
    adrg_name       VARCHAR(100),
    drg_code        VARCHAR(10)     NOT NULL,
    drg_name        VARCHAR(100),
    drg_type        VARCHAR(20)     NOT NULL,
    weight          DECIMAL(10,4)   NOT NULL,
    avg_los         DECIMAL(10,2),
    low_trim        DECIMAL(10,2),
    high_trim       DECIMAL(10,2),
    standard_cost   DECIMAL(18,4),
    version         VARCHAR(20)     NOT NULL,
    effective_date  DATE,
    expiry_date     DATE,
    enabled         SMALLINT        NOT NULL DEFAULT 1,
    description     TEXT,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_drg_group PRIMARY KEY (id),
    CONSTRAINT uq_drg_group_code UNIQUE (group_code)
);
CREATE INDEX idx_drg_group_drg_code ON drg_group (drg_code);
CREATE INDEX idx_drg_group_mdc_code ON drg_group (mdc_code);
CREATE INDEX idx_drg_group_adrg_code ON drg_group (adrg_code);
CREATE INDEX idx_drg_group_drg_type ON drg_group (drg_type);
CREATE INDEX idx_drg_group_enabled ON drg_group (enabled);

-- 2. DRG case (DRG病例)
CREATE TABLE drg_case (
    id                          BIGINT          NOT NULL,
    case_no                     VARCHAR(32)     NOT NULL,
    patient_id                  BIGINT          NOT NULL,
    admission_id                BIGINT          NOT NULL,
    encounter_id                BIGINT,
    admission_date              DATE            NOT NULL,
    discharge_date              DATE,
    los                         INTEGER,
    primary_diagnosis_code      VARCHAR(32)     NOT NULL,
    primary_diagnosis_name      VARCHAR(200),
    secondary_diagnoses         TEXT,
    primary_procedure_code      VARCHAR(32),
    primary_procedure_name      VARCHAR(200),
    secondary_procedures        TEXT,
    patient_age                 INTEGER,
    patient_gender              SMALLINT,
    discharge_type              VARCHAR(20),
    total_cost                  DECIMAL(18,4)   NOT NULL DEFAULT 0,
    drg_group_id                BIGINT,
    drg_code                    VARCHAR(10),
    drg_name                    VARCHAR(100),
    weight                      DECIMAL(10,4),
    grouping_reason             TEXT,
    grouping_time               TIMESTAMP,
    case_status                 VARCHAR(20)     NOT NULL DEFAULT 'PENDING',
    auditor_id                  BIGINT,
    auditor_name                VARCHAR(64),
    audit_time                  TIMESTAMP,
    audit_opinion               TEXT,
    dept_id                     BIGINT,
    dept_name                   VARCHAR(64),
    doctor_id                   BIGINT,
    doctor_name                 VARCHAR(64),
    created_by                  VARCHAR(64),
    created_time                TIMESTAMP,
    updated_by                  VARCHAR(64),
    updated_time                TIMESTAMP,
    deleted                     SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_drg_case PRIMARY KEY (id),
    CONSTRAINT uq_drg_case_no UNIQUE (case_no)
);
CREATE INDEX idx_drg_case_patient ON drg_case (patient_id);
CREATE INDEX idx_drg_case_admission ON drg_case (admission_id);
CREATE INDEX idx_drg_case_drg_code ON drg_case (drg_code);
CREATE INDEX idx_drg_case_status ON drg_case (case_status);
CREATE INDEX idx_drg_case_discharge_date ON drg_case (discharge_date);
CREATE INDEX idx_drg_case_dept ON drg_case (dept_id);

-- 3. DRG cost analysis (DRG费用分析)
CREATE TABLE drg_cost_analysis (
    id              BIGINT          NOT NULL,
    analysis_period VARCHAR(20)     NOT NULL,
    dept_id         BIGINT,
    dept_name       VARCHAR(64),
    drg_code        VARCHAR(10),
    drg_name        VARCHAR(100),
    case_count      INTEGER         NOT NULL DEFAULT 0,
    total_cost      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    avg_cost        DECIMAL(18,4),
    standard_cost   DECIMAL(18,4),
    cost_variance   DECIMAL(18,4),
    drug_cost       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    surgery_cost    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    bed_cost        DECIMAL(18,4)   NOT NULL DEFAULT 0,
    exam_cost       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    lab_cost        DECIMAL(18,4)   NOT NULL DEFAULT 0,
    other_cost      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    avg_los         DECIMAL(10,2),
    cm_index        DECIMAL(10,4),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_drg_cost_analysis PRIMARY KEY (id)
);
CREATE INDEX idx_drg_cost_period ON drg_cost_analysis (analysis_period);
CREATE INDEX idx_drg_cost_dept ON drg_cost_analysis (dept_id);
CREATE INDEX idx_drg_cost_drg_code ON drg_cost_analysis (drg_code);
