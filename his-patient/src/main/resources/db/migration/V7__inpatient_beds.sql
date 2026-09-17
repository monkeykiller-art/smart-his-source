CREATE TABLE pat_inpatient_bed (
    id BIGINT NOT NULL,
    dept_id BIGINT NOT NULL,
    ward_id BIGINT NOT NULL,
    ward_name VARCHAR(64) NOT NULL,
    bed_no VARCHAR(20) NOT NULL,
    bed_type VARCHAR(20) NOT NULL DEFAULT 'STANDARD',
    bed_status VARCHAR(20) NOT NULL DEFAULT 'AVAILABLE',
    current_admission_id BIGINT,
    created_by VARCHAR(64), created_time TIMESTAMP,
    updated_by VARCHAR(64), updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_inpatient_bed PRIMARY KEY (id),
    CONSTRAINT uk_pat_inpatient_bed_no UNIQUE (ward_id, bed_no),
    CONSTRAINT ck_pat_inpatient_bed_status CHECK (bed_status IN ('AVAILABLE', 'OCCUPIED', 'MAINTENANCE'))
);
CREATE INDEX idx_pat_inpatient_bed_status ON pat_inpatient_bed (ward_id, bed_status);

INSERT INTO pat_inpatient_bed (id, dept_id, ward_id, ward_name, bed_no, bed_type, bed_status, created_by, created_time) VALUES
(81001, 1001, 1101, '内科一病区', '01', 'STANDARD', 'AVAILABLE', 'system', CURRENT_TIMESTAMP),
(81002, 1001, 1101, '内科一病区', '02', 'STANDARD', 'AVAILABLE', 'system', CURRENT_TIMESTAMP),
(82001, 1002, 1201, '外科一病区', '01', 'STANDARD', 'AVAILABLE', 'system', CURRENT_TIMESTAMP),
(82002, 1002, 1201, '外科一病区', '02', 'ICU', 'AVAILABLE', 'system', CURRENT_TIMESTAMP);
