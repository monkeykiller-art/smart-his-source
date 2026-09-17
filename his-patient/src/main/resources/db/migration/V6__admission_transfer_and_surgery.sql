CREATE TABLE pat_admission_transfer (
    id BIGINT NOT NULL,
    admission_id BIGINT NOT NULL,
    from_dept_id BIGINT,
    from_ward_id BIGINT,
    from_bed_id BIGINT,
    to_dept_id BIGINT NOT NULL,
    to_ward_id BIGINT NOT NULL,
    to_bed_id BIGINT NOT NULL,
    transfer_reason VARCHAR(500) NOT NULL,
    transfer_time TIMESTAMP NOT NULL,
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_admission_transfer PRIMARY KEY (id)
);
CREATE INDEX idx_pat_admission_transfer_admission ON pat_admission_transfer (admission_id, transfer_time);

CREATE TABLE pat_surgery_case (
    id BIGINT NOT NULL,
    surgery_no VARCHAR(32) NOT NULL,
    admission_id BIGINT NOT NULL,
    patient_id BIGINT NOT NULL,
    surgery_name VARCHAR(128) NOT NULL,
    planned_start_time TIMESTAMP NOT NULL,
    operating_room VARCHAR(64),
    surgeon_id BIGINT NOT NULL,
    anesthetist_id BIGINT,
    anesthesia_method VARCHAR(64),
    surgery_status VARCHAR(20) NOT NULL DEFAULT 'APPLIED',
    operative_note TEXT,
    created_by VARCHAR(64),
    created_time TIMESTAMP,
    updated_by VARCHAR(64),
    updated_time TIMESTAMP,
    deleted SMALLINT NOT NULL DEFAULT 0,
    CONSTRAINT pk_pat_surgery_case PRIMARY KEY (id),
    CONSTRAINT uk_pat_surgery_no UNIQUE (surgery_no)
);
CREATE INDEX idx_pat_surgery_admission ON pat_surgery_case (admission_id, planned_start_time);
