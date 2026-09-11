-- Smart HIS platform schema init

CREATE TABLE plt_master_data (
    id              BIGINT        NOT NULL,
    data_type       VARCHAR(64)   NOT NULL,
    data_code       VARCHAR(64)   NOT NULL,
    data_name       VARCHAR(128)  NOT NULL,
    data_value      TEXT,
    parent_code     VARCHAR(64),
    sort_order      INTEGER       NOT NULL DEFAULT 0,
    data_version    VARCHAR(32)   NOT NULL DEFAULT '1',
    data_status     VARCHAR(16)   NOT NULL DEFAULT 'DRAFT',
    effective_time  TIMESTAMP,
    expiry_time     TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_plt_master_data PRIMARY KEY (id),
    CONSTRAINT uq_plt_master_data_code UNIQUE (data_type, data_code, data_version)
);
CREATE INDEX idx_plt_master_data_status ON plt_master_data (data_type, data_status);

CREATE TABLE plt_data_mapping (
    id              BIGINT        NOT NULL,
    mapping_type    VARCHAR(32)   NOT NULL,
    source_system   VARCHAR(64)   NOT NULL,
    source_code     VARCHAR(128)  NOT NULL,
    target_system   VARCHAR(64)   NOT NULL,
    target_code     VARCHAR(128)  NOT NULL,
    mapping_status  VARCHAR(16)   NOT NULL DEFAULT 'ACTIVE',
    description     VARCHAR(500),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT      NOT NULL DEFAULT 0,
    CONSTRAINT pk_plt_data_mapping PRIMARY KEY (id),
    CONSTRAINT uq_plt_data_mapping_source UNIQUE (mapping_type, source_system, source_code, target_system)
);
CREATE INDEX idx_plt_data_mapping_target ON plt_data_mapping (target_system, target_code);

CREATE TABLE plt_exchange_record (
    id              BIGINT        NOT NULL,
    exchange_no     VARCHAR(32)   NOT NULL,
    direction       VARCHAR(16)   NOT NULL,
    source_system   VARCHAR(64)   NOT NULL,
    target_system   VARCHAR(64)   NOT NULL,
    standard_type   VARCHAR(32),
    resource_type   VARCHAR(64),
    resource_id     VARCHAR(128),
    endpoint        VARCHAR(256),
    payload         TEXT,
    response_payload TEXT,
    exchange_status VARCHAR(16)   NOT NULL DEFAULT 'PENDING',
    retry_count     INTEGER       NOT NULL DEFAULT 0,
    error_message   TEXT,
    requested_time  TIMESTAMP     NOT NULL,
    completed_time  TIMESTAMP,
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_plt_exchange_record PRIMARY KEY (id),
    CONSTRAINT uq_plt_exchange_record_no UNIQUE (exchange_no)
);
CREATE INDEX idx_plt_exchange_status ON plt_exchange_record (exchange_status, requested_time);
CREATE INDEX idx_plt_exchange_resource ON plt_exchange_record (resource_type, resource_id);

CREATE TABLE plt_quality_result (
    id              BIGINT        NOT NULL,
    exchange_id     BIGINT,
    rule_code       VARCHAR(64)   NOT NULL,
    resource_type   VARCHAR(64)   NOT NULL,
    resource_id     VARCHAR(128),
    quality_level   VARCHAR(16)   NOT NULL,
    passed          SMALLINT      NOT NULL,
    result_detail   TEXT,
    checked_time    TIMESTAMP     NOT NULL,
    created_time    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_plt_quality_result PRIMARY KEY (id)
);
CREATE INDEX idx_plt_quality_exchange ON plt_quality_result (exchange_id);
CREATE INDEX idx_plt_quality_resource ON plt_quality_result (resource_type, resource_id);

CREATE TABLE plt_audit_log (
    id              BIGINT        NOT NULL,
    trace_id        VARCHAR(64),
    operator_id     BIGINT,
    operator_name   VARCHAR(64),
    operation_type  VARCHAR(32)   NOT NULL,
    resource_type   VARCHAR(64)   NOT NULL,
    resource_id     VARCHAR(128),
    request_method  VARCHAR(16),
    request_path    VARCHAR(256),
    client_ip       VARCHAR(64),
    operation_result VARCHAR(16)  NOT NULL,
    operation_detail TEXT,
    operated_time   TIMESTAMP     NOT NULL,
    CONSTRAINT pk_plt_audit_log PRIMARY KEY (id)
);
CREATE INDEX idx_plt_audit_operator ON plt_audit_log (operator_id, operated_time);
CREATE INDEX idx_plt_audit_resource ON plt_audit_log (resource_type, resource_id);
CREATE INDEX idx_plt_audit_trace ON plt_audit_log (trace_id);
