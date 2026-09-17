CREATE TABLE ops_saved_report_filter (
    id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    report_code VARCHAR(64) NOT NULL,
    filter_name VARCHAR(128) NOT NULL,
    config_json TEXT NOT NULL,
    created_by VARCHAR(64), created_time TIMESTAMP,
    updated_by VARCHAR(64), updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_saved_report_filter PRIMARY KEY (id),
    CONSTRAINT uk_ops_saved_report_filter UNIQUE (user_id, report_code, filter_name)
);
CREATE INDEX idx_ops_saved_report_filter_user ON ops_saved_report_filter (user_id, report_code);
