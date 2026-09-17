CREATE TABLE pha_dispense (
    id                 BIGINT          NOT NULL,
    dispense_no        VARCHAR(32)     NOT NULL,
    prescription_id    BIGINT          NOT NULL,
    rx_review_id       BIGINT          NOT NULL,
    patient_id         BIGINT          NOT NULL,
    warehouse_code     VARCHAR(32)     NOT NULL,
    dispense_status    VARCHAR(16)     NOT NULL DEFAULT 'DISPENSING',
    pharmacist_id      BIGINT,
    pharmacist_name    VARCHAR(64),
    dispense_time      TIMESTAMP,
    return_time        TIMESTAMP,
    remark             VARCHAR(256),
    created_by         VARCHAR(64),
    created_time       TIMESTAMP,
    updated_by         VARCHAR(64),
    updated_time       TIMESTAMP,
    deleted            SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_dispense PRIMARY KEY (id),
    CONSTRAINT uq_pha_dispense_no UNIQUE (dispense_no)
);

CREATE UNIQUE INDEX uq_pha_dispense_active_rx ON pha_dispense (prescription_id)
    WHERE deleted = 0 AND dispense_status <> 'RETURNED';
CREATE INDEX idx_pha_dispense_patient ON pha_dispense (patient_id, dispense_time);

CREATE TABLE pha_dispense_item (
    id                   BIGINT          NOT NULL,
    dispense_id          BIGINT          NOT NULL,
    prescription_item_id BIGINT,
    drug_id              BIGINT          NOT NULL,
    batch_id             BIGINT          NOT NULL,
    batch_no             VARCHAR(64)     NOT NULL,
    expiry_date          DATE            NOT NULL,
    quantity             DECIMAL(18,4)   NOT NULL,
    unit                 VARCHAR(16)     NOT NULL,
    created_by           VARCHAR(64),
    created_time         TIMESTAMP,
    updated_by           VARCHAR(64),
    updated_time         TIMESTAMP,
    deleted              SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_dispense_item PRIMARY KEY (id),
    CONSTRAINT ck_pha_dispense_item_quantity CHECK (quantity > 0)
);

CREATE INDEX idx_pha_dispense_item_dispense ON pha_dispense_item (dispense_id);
CREATE INDEX idx_pha_dispense_item_batch ON pha_dispense_item (batch_id);
