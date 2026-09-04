-- his_clinical schema — clinical core tables
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ
-- Covers: ICD-10 dictionary, medical records, diagnoses, orders (医嘱), order templates,
--         skin tests, exam/lab requests — aligned with 住院管理系统 & 智慧HIS建设方案

-- 1. ICD-10 diagnosis code dictionary
CREATE TABLE cli_icd10 (
    id              BIGINT          NOT NULL,
    icd_code        VARCHAR(16)     NOT NULL,
    icd_name        VARCHAR(256)    NOT NULL,
    name_pinyin     VARCHAR(512),
    chapter         VARCHAR(64),
    block           VARCHAR(64),
    category        VARCHAR(16),
    sub_category    VARCHAR(16),
    is_infectious   SMALLINT        NOT NULL DEFAULT 0,
    is_chronic      SMALLINT        NOT NULL DEFAULT 0,
    is_tcm          SMALLINT        NOT NULL DEFAULT 0,
    sort_order      INT             NOT NULL DEFAULT 0,
    dict_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_icd10 PRIMARY KEY (id),
    CONSTRAINT uq_cli_icd10_code UNIQUE (icd_code)
);
CREATE INDEX idx_cli_icd10_name ON cli_icd10 (icd_name);
CREATE INDEX idx_cli_icd10_pinyin ON cli_icd10 (name_pinyin);

-- 2. TCM diagnosis dictionary (中医病证编码)
CREATE TABLE cli_tcm_diagnosis (
    id              BIGINT          NOT NULL,
    tcm_code        VARCHAR(16)     NOT NULL,
    tcm_name        VARCHAR(256)    NOT NULL,
    name_pinyin     VARCHAR(512),
    syndrome_code   VARCHAR(16),
    syndrome_name   VARCHAR(256),
    category        VARCHAR(32),
    sort_order      INT             NOT NULL DEFAULT 0,
    dict_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_tcm PRIMARY KEY (id),
    CONSTRAINT uq_cli_tcm_code UNIQUE (tcm_code)
);
CREATE INDEX idx_cli_tcm_name ON cli_tcm_diagnosis (tcm_name);

-- 3. Medical record document (病历文书)
CREATE TABLE cli_medical_record (
    id              BIGINT          NOT NULL,
    record_no       VARCHAR(32)     NOT NULL,
    encounter_id    BIGINT,
    admission_id    BIGINT,
    patient_id      BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    record_type     VARCHAR(32)     NOT NULL DEFAULT 'OUTPATIENT',
    template_id     BIGINT,
    title           VARCHAR(128),
    chief_complaint TEXT,
    present_illness TEXT,
    past_history    TEXT,
    allergy_history TEXT,
    physical_exam   TEXT,
    auxiliary_exam  TEXT,
    diagnosis_desc  TEXT,
    treatment_plan  TEXT,
    record_content  TEXT,
    record_status   VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    sign_time       TIMESTAMP,
    sign_cert_sn    VARCHAR(128),
    quality_score   DECIMAL(5,2),
    quality_result  VARCHAR(16),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_medical_record PRIMARY KEY (id),
    CONSTRAINT uq_cli_medical_record_no UNIQUE (record_no)
);
CREATE INDEX idx_cli_medical_record_patient ON cli_medical_record (patient_id);
CREATE INDEX idx_cli_medical_record_encounter ON cli_medical_record (encounter_id);
CREATE INDEX idx_cli_medical_record_doctor ON cli_medical_record (doctor_id, record_status);

-- 4. Record template (病历模板)
CREATE TABLE cli_record_template (
    id              BIGINT          NOT NULL,
    template_name   VARCHAR(128)    NOT NULL,
    template_type   VARCHAR(32)     NOT NULL DEFAULT 'DEPARTMENT',
    dept_id         BIGINT,
    disease_code    VARCHAR(16),
    record_type     VARCHAR(32)     NOT NULL DEFAULT 'OUTPATIENT',
    template_content TEXT,
    sort_order      INT             NOT NULL DEFAULT 0,
    template_status SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_record_tpl PRIMARY KEY (id)
);
CREATE INDEX idx_cli_record_tpl_dept ON cli_record_template (dept_id);
CREATE INDEX idx_cli_record_tpl_type ON cli_record_template (template_type, record_type);

-- 5. Diagnosis (诊断记录 — linked to encounter or admission)
CREATE TABLE cli_diagnosis (
    id              BIGINT          NOT NULL,
    encounter_id    BIGINT,
    admission_id    BIGINT,
    patient_id      BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    icd10_id        BIGINT,
    icd_code        VARCHAR(16),
    diagnosis_name  VARCHAR(256)    NOT NULL,
    diagnosis_type  VARCHAR(16)     NOT NULL DEFAULT 'WESTERN',
    is_primary      SMALLINT        NOT NULL DEFAULT 0,
    is_confirmed    SMALLINT        NOT NULL DEFAULT 0,
    diagnosis_seq   INT             NOT NULL DEFAULT 1,
    onset_date      DATE,
    diagnosis_desc  TEXT,
    diagnosis_status VARCHAR(16)    NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_diagnosis PRIMARY KEY (id)
);
CREATE INDEX idx_cli_diagnosis_patient ON cli_diagnosis (patient_id);
CREATE INDEX idx_cli_diagnosis_encounter ON cli_diagnosis (encounter_id);
CREATE INDEX idx_cli_diagnosis_admission ON cli_diagnosis (admission_id);
CREATE INDEX idx_cli_diagnosis_icd ON cli_diagnosis (icd_code);

-- 6. Medical order (医嘱 — core of doctor/nurse station)
CREATE TABLE cli_order (
    id              BIGINT          NOT NULL,
    order_no        VARCHAR(32)     NOT NULL,
    encounter_id    BIGINT,
    admission_id    BIGINT,
    patient_id      BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    order_type      VARCHAR(16)     NOT NULL DEFAULT 'MEDICINE',
    order_category  VARCHAR(16)     NOT NULL DEFAULT 'ROUTINE',
    order_status    VARCHAR(16)     NOT NULL DEFAULT 'DRAFT',
    priority        SMALLINT        NOT NULL DEFAULT 0,
    is_stat         SMALLINT        NOT NULL DEFAULT 0,
    is_prn          SMALLINT        NOT NULL DEFAULT 0,
    start_time      TIMESTAMP,
    end_time        TIMESTAMP,
    order_time      TIMESTAMP,
    verify_nurse_id BIGINT,
    verify_time     TIMESTAMP,
    cancel_nurse_id BIGINT,
    cancel_time     TIMESTAMP,
    cancel_reason   VARCHAR(256),
    execute_dept_id BIGINT,
    remark          VARCHAR(512),
    group_no        VARCHAR(32),
    bill_id         BIGINT,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_order PRIMARY KEY (id),
    CONSTRAINT uq_cli_order_no UNIQUE (order_no)
);
CREATE INDEX idx_cli_order_patient ON cli_order (patient_id);
CREATE INDEX idx_cli_order_admission ON cli_order (admission_id, order_status);
CREATE INDEX idx_cli_order_encounter ON cli_order (encounter_id);
CREATE INDEX idx_cli_order_doctor ON cli_order (doctor_id, order_time);
CREATE INDEX idx_cli_order_dept ON cli_order (execute_dept_id, order_status);

-- 7. Order item (医嘱明细)
CREATE TABLE cli_order_item (
    id              BIGINT          NOT NULL,
    order_id        BIGINT          NOT NULL,
    item_seq        INT             NOT NULL DEFAULT 1,
    item_code       VARCHAR(32),
    item_name       VARCHAR(128)    NOT NULL,
    item_type       VARCHAR(16)     NOT NULL DEFAULT 'DRUG',
    spec            VARCHAR(128),
    dose            DECIMAL(18,4),
    dose_unit       VARCHAR(16),
    usage_method   VARCHAR(32),
    frequency       VARCHAR(32),
    days            INT,
    quantity        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    quantity_unit   VARCHAR(16),
    unit_price      DECIMAL(18,4),
    amount          DECIMAL(18,4),
    is_first_day    SMALLINT        NOT NULL DEFAULT 0,
    drip_rate       VARCHAR(32),
    skin_test_result VARCHAR(16),
    remark          VARCHAR(256),
    item_status     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_order_item PRIMARY KEY (id)
);
CREATE INDEX idx_cli_order_item_order ON cli_order_item (order_id);

-- 8. Order template (医嘱模板 — from 住院医嘱模板)
CREATE TABLE cli_order_template (
    id              BIGINT          NOT NULL,
    template_name   VARCHAR(128)    NOT NULL,
    template_level  VARCHAR(16)     NOT NULL DEFAULT 'DEPARTMENT',
    dept_id         BIGINT,
    template_category VARCHAR(32),
    level_type      VARCHAR(16),
    order_type      VARCHAR(16)     NOT NULL DEFAULT 'MEDICINE',
    sort_order      INT             NOT NULL DEFAULT 0,
    template_status SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_order_tpl PRIMARY KEY (id)
);
CREATE INDEX idx_cli_order_tpl_dept ON cli_order_template (dept_id);

-- 9. Order template item (医嘱模板明细)
CREATE TABLE cli_order_template_item (
    id              BIGINT          NOT NULL,
    template_id     BIGINT          NOT NULL,
    group_no        INT             NOT NULL DEFAULT 1,
    item_code       VARCHAR(32),
    item_name       VARCHAR(128)    NOT NULL,
    spec            VARCHAR(128),
    dose            DECIMAL(18,4),
    dose_unit       VARCHAR(16),
    usage_method   VARCHAR(32),
    frequency       VARCHAR(32),
    is_first_day    SMALLINT        NOT NULL DEFAULT 0,
    quantity        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    quantity_unit   VARCHAR(16),
    drip_rate       VARCHAR(32),
    execute_dept_id BIGINT,
    doctor_advice   VARCHAR(256),
    order_category  VARCHAR(16)     NOT NULL DEFAULT 'ROUTINE',
    item_seq        INT             NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_order_tpl_item PRIMARY KEY (id)
);
CREATE INDEX idx_cli_order_tpl_item_tpl ON cli_order_template_item (template_id);

-- 10. Skin test record (皮试记录)
CREATE TABLE cli_skin_test (
    id              BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    order_item_id   BIGINT,
    drug_code       VARCHAR(32),
    drug_name       VARCHAR(128)    NOT NULL,
    batch_no        VARCHAR(64),
    test_result     VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    test_time       TIMESTAMP,
    test_nurse_id   BIGINT,
    judge_time      TIMESTAMP,
    judge_nurse_id  BIGINT,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_skin_test PRIMARY KEY (id)
);
CREATE INDEX idx_cli_skin_test_patient ON cli_skin_test (patient_id);
CREATE INDEX idx_cli_skin_test_admission ON cli_skin_test (admission_id);

-- 11. Exam/lab request (检查检验申请)
CREATE TABLE cli_exam_request (
    id              BIGINT          NOT NULL,
    request_no      VARCHAR(32)     NOT NULL,
    encounter_id    BIGINT,
    admission_id    BIGINT,
    patient_id      BIGINT          NOT NULL,
    dept_id         BIGINT          NOT NULL,
    doctor_id       BIGINT          NOT NULL,
    request_type    VARCHAR(16)     NOT NULL DEFAULT 'LAB',
    is_urgent       SMALLINT        NOT NULL DEFAULT 0,
    clinical_diagnosis VARCHAR(256),
    clinical_info   TEXT,
    request_dept_id BIGINT,
    execute_dept_id BIGINT,
    request_status  VARCHAR(16)     NOT NULL DEFAULT 'SUBMITTED',
    request_time    TIMESTAMP,
    result_time     TIMESTAMP,
    result_summary  TEXT,
    report_no       VARCHAR(64),
    is_printed      SMALLINT        NOT NULL DEFAULT 0,
    print_time      TIMESTAMP,
    remark          VARCHAR(512),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_exam_request PRIMARY KEY (id),
    CONSTRAINT uq_cli_exam_request_no UNIQUE (request_no)
);
CREATE INDEX idx_cli_exam_request_patient ON cli_exam_request (patient_id);
CREATE INDEX idx_cli_exam_request_admission ON cli_exam_request (admission_id);
CREATE INDEX idx_cli_exam_request_dept ON cli_exam_request (execute_dept_id, request_status);

-- 12. Exam request item (申请单明细)
CREATE TABLE cli_exam_request_item (
    id              BIGINT          NOT NULL,
    request_id      BIGINT          NOT NULL,
    item_seq        INT             NOT NULL DEFAULT 1,
    item_code       VARCHAR(32),
    item_name       VARCHAR(128)    NOT NULL,
    item_type       VARCHAR(16),
    spec            VARCHAR(128),
    quantity        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    unit_price      DECIMAL(18,4),
    amount          DECIMAL(18,4),
    body_part       VARCHAR(128),
    method_desc     VARCHAR(256),
    item_status     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    confirm_status  VARCHAR(16)     NOT NULL DEFAULT 'UNCONFIRMED',
    confirm_time    TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_exam_req_item PRIMARY KEY (id)
);
CREATE INDEX idx_cli_exam_req_item_request ON cli_exam_request_item (request_id);

-- 13. Drug usage dictionary (用法字典)
CREATE TABLE cli_drug_usage_dict (
    id              BIGINT          NOT NULL,
    usage_code      VARCHAR(16)     NOT NULL,
    usage_name      VARCHAR(64)     NOT NULL,
    name_pinyin     VARCHAR(128),
    usage_desc      VARCHAR(256),
    is_injection    SMALLINT        NOT NULL DEFAULT 0,
    need_skin_test  SMALLINT        NOT NULL DEFAULT 0,
    sort_order      INT             NOT NULL DEFAULT 0,
    dict_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_drug_usage PRIMARY KEY (id),
    CONSTRAINT uq_cli_drug_usage_code UNIQUE (usage_code)
);

-- 14. Frequency dictionary (频次字典)
CREATE TABLE cli_frequency_dict (
    id              BIGINT          NOT NULL,
    freq_code       VARCHAR(16)     NOT NULL,
    freq_name       VARCHAR(64)     NOT NULL,
    name_pinyin     VARCHAR(128),
    daily_times     INT             NOT NULL DEFAULT 1,
    freq_desc       VARCHAR(128),
    sort_order      INT             NOT NULL DEFAULT 0,
    dict_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_cli_frequency PRIMARY KEY (id),
    CONSTRAINT uq_cli_frequency_code UNIQUE (freq_code)
);
