-- Smart HIS cdss schema init

CREATE TABLE cdss_knowledge_rule (
    id              BIGINT        NOT NULL,
    rule_code       VARCHAR(64)   NOT NULL,
    rule_name       VARCHAR(128)  NOT NULL,
    rule_type       VARCHAR(32)   NOT NULL,
    clinical_scene  VARCHAR(32)   NOT NULL,
    rule_expression TEXT          NOT NULL,
    severity        VARCHAR(16)   NOT NULL,
    alert_message   VARCHAR(500)  NOT NULL,
    recommendation  TEXT,
    rule_version    VARCHAR(32)   NOT NULL,
    rule_status     VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    effective_time  TIMESTAMP,
    expiry_time     TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_cdss_knowledge_rule PRIMARY KEY (id),
    CONSTRAINT uq_cdss_knowledge_rule_code UNIQUE (rule_code, rule_version)
);
CREATE INDEX idx_cdss_rule_scene ON cdss_knowledge_rule (clinical_scene, rule_status);

CREATE TABLE cdss_decision_request (
    id              BIGINT        NOT NULL,
    request_no      VARCHAR(32)   NOT NULL,
    patient_id      BIGINT        NOT NULL,
    encounter_id    BIGINT,
    requester_id    BIGINT,
    requester_name  VARCHAR(64),
    clinical_scene  VARCHAR(32)   NOT NULL,
    request_payload TEXT          NOT NULL,
    request_status  VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    requested_time  TIMESTAMP     NOT NULL,
    completed_time  TIMESTAMP,
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cdss_decision_request PRIMARY KEY (id),
    CONSTRAINT uq_cdss_decision_request_no UNIQUE (request_no)
);
CREATE INDEX idx_cdss_request_patient ON cdss_decision_request (patient_id, requested_time);
CREATE INDEX idx_cdss_request_status ON cdss_decision_request (request_status, requested_time);

CREATE TABLE cdss_decision_result (
    id              BIGINT        NOT NULL,
    request_id      BIGINT        NOT NULL,
    rule_id         BIGINT,
    result_type     VARCHAR(32)   NOT NULL,
    severity        VARCHAR(16),
    result_summary  VARCHAR(500)  NOT NULL,
    result_detail   TEXT,
    recommendation  TEXT,
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cdss_decision_result PRIMARY KEY (id)
);
CREATE INDEX idx_cdss_result_request ON cdss_decision_result (request_id);
CREATE INDEX idx_cdss_result_rule ON cdss_decision_result (rule_id);

CREATE TABLE cdss_alert (
    id              BIGINT        NOT NULL,
    alert_no        VARCHAR(32)   NOT NULL,
    patient_id      BIGINT        NOT NULL,
    encounter_id    BIGINT,
    request_id      BIGINT,
    rule_id         BIGINT,
    alert_type      VARCHAR(32)   NOT NULL,
    severity        VARCHAR(16)   NOT NULL,
    alert_message   VARCHAR(500)  NOT NULL,
    alert_detail    TEXT,
    alert_status    VARCHAR(16)   NOT NULL DEFAULT 'OPEN',
    triggered_time  TIMESTAMP     NOT NULL,
    acknowledged_by BIGINT,
    acknowledged_time TIMESTAMP,
    acknowledge_note VARCHAR(500),
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_cdss_alert PRIMARY KEY (id),
    CONSTRAINT uq_cdss_alert_no UNIQUE (alert_no)
);
CREATE INDEX idx_cdss_alert_patient ON cdss_alert (patient_id, triggered_time);
CREATE INDEX idx_cdss_alert_status ON cdss_alert (alert_status, severity);
