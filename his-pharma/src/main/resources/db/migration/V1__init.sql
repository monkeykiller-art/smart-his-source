-- his_pharma schema — prescription review, drug safety knowledge, ADR reporting
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ
-- Covers: prescription review, drug interaction, contraindication, dose limit, ADR report

-- 1. Prescription review (处方审核)
CREATE TABLE pha_rx_review (
    id              BIGINT          NOT NULL,
    review_no       VARCHAR(32)     NOT NULL,
    order_id        BIGINT,
    encounter_id    BIGINT,
    admission_id    BIGINT,
    patient_id      BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    prescription_type VARCHAR(16)   NOT NULL DEFAULT 'WESTERN',
    review_status   VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    review_result   VARCHAR(16),
    reviewer_id     VARCHAR(64),
    reviewer_name   VARCHAR(64),
    review_time     TIMESTAMP,
    reject_reason   VARCHAR(512),
    warning_count   INT             NOT NULL DEFAULT 0,
    error_count     INT             NOT NULL DEFAULT 0,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_rx_review PRIMARY KEY (id),
    CONSTRAINT uq_pha_rx_review_no UNIQUE (review_no)
);
CREATE INDEX idx_pha_rx_review_order ON pha_rx_review (order_id);
CREATE INDEX idx_pha_rx_review_patient ON pha_rx_review (patient_id);
CREATE INDEX idx_pha_rx_review_status ON pha_rx_review (review_status);
CREATE INDEX idx_pha_rx_review_doctor ON pha_rx_review (doctor_id, review_time);

-- 2. Review detail (审核明细)
CREATE TABLE pha_rx_review_item (
    id              BIGINT          NOT NULL,
    review_id       BIGINT          NOT NULL,
    order_item_id   BIGINT,
    alert_type      VARCHAR(32)     NOT NULL,
    alert_level     VARCHAR(16)     NOT NULL DEFAULT 'WARNING',
    drug_code_a     VARCHAR(32),
    drug_name_a     VARCHAR(128),
    drug_code_b     VARCHAR(32),
    drug_name_b     VARCHAR(128),
    alert_desc      VARCHAR(512),
    suggestion      VARCHAR(256),
    is_overridden   SMALLINT        NOT NULL DEFAULT 0,
    override_reason VARCHAR(256),
    override_by     VARCHAR(64),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_rx_review_item PRIMARY KEY (id)
);
CREATE INDEX idx_pha_rx_review_item_review ON pha_rx_review_item (review_id);

-- 3. Drug interaction knowledge (药物相互作用)
CREATE TABLE pha_drug_interaction (
    id              BIGINT          NOT NULL,
    drug_code_a     VARCHAR(32)     NOT NULL,
    drug_code_b     VARCHAR(32)     NOT NULL,
    interaction_level VARCHAR(16)   NOT NULL DEFAULT 'WARNING',
    interaction_desc VARCHAR(512)   NOT NULL,
    suggestion      VARCHAR(256),
    reference       VARCHAR(256),
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_drug_interaction PRIMARY KEY (id)
);
CREATE INDEX idx_pha_drug_interaction_a ON pha_drug_interaction (drug_code_a);
CREATE INDEX idx_pha_drug_interaction_b ON pha_drug_interaction (drug_code_b);

-- 4. Drug contraindication (药物禁忌)
CREATE TABLE pha_drug_contraindication (
    id              BIGINT          NOT NULL,
    drug_code       VARCHAR(32)     NOT NULL,
    contraindication_type VARCHAR(32) NOT NULL,
    contraindication_code VARCHAR(32),
    contraindication_name VARCHAR(128) NOT NULL,
    severity_level  VARCHAR(16)     NOT NULL DEFAULT 'WARNING',
    description     VARCHAR(512)    NOT NULL,
    suggestion      VARCHAR(256),
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_drug_contra PRIMARY KEY (id)
);
CREATE INDEX idx_pha_drug_contra_code ON pha_drug_contraindication (drug_code);

-- 5. Dose limit (剂量限制)
CREATE TABLE pha_dose_limit (
    id              BIGINT          NOT NULL,
    drug_code       VARCHAR(32)     NOT NULL,
    patient_type    VARCHAR(16)     NOT NULL DEFAULT 'ADULT',
    age_min         INT,
    age_max         INT,
    route           VARCHAR(32),
    max_single_dose DECIMAL(18,4),
    max_single_unit VARCHAR(16),
    max_daily_dose  DECIMAL(18,4),
    max_daily_unit  VARCHAR(16),
    max_freq_per_day INT,
    description     VARCHAR(256),
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_dose_limit PRIMARY KEY (id)
);
CREATE INDEX idx_pha_dose_limit_drug ON pha_dose_limit (drug_code);

-- 6. Drug allergy cross (药物过敏交叉)
CREATE TABLE pha_drug_allergy_cross (
    id              BIGINT          NOT NULL,
    allergy_code    VARCHAR(32)     NOT NULL,
    allergy_name    VARCHAR(128)    NOT NULL,
    cross_drug_code VARCHAR(32)     NOT NULL,
    cross_drug_name VARCHAR(128)    NOT NULL,
    cross_level     VARCHAR(16)     NOT NULL DEFAULT 'WARNING',
    description     VARCHAR(256),
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_allergy_cross PRIMARY KEY (id)
);
CREATE INDEX idx_pha_allergy_cross_allergy ON pha_drug_allergy_cross (allergy_code);

-- 7. Adverse drug reaction report (不良反应报告)
CREATE TABLE pha_adr_report (
    id              BIGINT          NOT NULL,
    report_no       VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    drug_code       VARCHAR(32),
    drug_name       VARCHAR(128)    NOT NULL,
    batch_no        VARCHAR(64),
    adr_onset_time  TIMESTAMP,
    adr_type        VARCHAR(32),
    adr_level       VARCHAR(16)     NOT NULL DEFAULT 'MILD',
    adr_desc        TEXT            NOT NULL,
    adr_outcome     VARCHAR(32),
    reporter_id     VARCHAR(64)     NOT NULL,
    reporter_name   VARCHAR(64),
    report_dept_id  BIGINT,
    report_time     TIMESTAMP,
    report_status   VARCHAR(16)     NOT NULL DEFAULT 'SUBMITTED',
    review_comment  VARCHAR(512),
    reviewer_id     VARCHAR(64),
    review_time     TIMESTAMP,
    is_reported_to_authority SMALLINT NOT NULL DEFAULT 0,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_adr_report PRIMARY KEY (id),
    CONSTRAINT uq_pha_adr_report_no UNIQUE (report_no)
);
CREATE INDEX idx_pha_adr_report_patient ON pha_adr_report (patient_id);
CREATE INDEX idx_pha_adr_report_drug ON pha_adr_report (drug_code);
CREATE INDEX idx_pha_adr_report_status ON pha_adr_report (report_status);

-- 8. CDSS alert config (CDSS预警配置)
CREATE TABLE pha_cdss_alert_config (
    id              BIGINT          NOT NULL,
    alert_type      VARCHAR(32)     NOT NULL,
    alert_name      VARCHAR(128)    NOT NULL,
    alert_level     VARCHAR(16)     NOT NULL DEFAULT 'WARNING',
    is_enabled      SMALLINT        NOT NULL DEFAULT 1,
    dept_id         BIGINT,
    description     VARCHAR(256),
    sort_order      INT             NOT NULL DEFAULT 0,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_cdss_config PRIMARY KEY (id)
);
