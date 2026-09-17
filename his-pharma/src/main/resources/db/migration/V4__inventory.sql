CREATE TABLE pha_inventory_batch (
    id                 BIGINT          NOT NULL,
    drug_id            BIGINT          NOT NULL,
    warehouse_code     VARCHAR(32)     NOT NULL,
    batch_no           VARCHAR(64)     NOT NULL,
    production_date    DATE,
    expiry_date        DATE            NOT NULL,
    unit_cost          DECIMAL(18,4)   NOT NULL,
    quantity           DECIMAL(18,4)   NOT NULL DEFAULT 0,
    available_quantity DECIMAL(18,4)   NOT NULL DEFAULT 0,
    locked_quantity    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    is_active          SMALLINT        NOT NULL DEFAULT 1,
    version            INT             NOT NULL DEFAULT 0,
    created_by         VARCHAR(64),
    created_time       TIMESTAMP,
    updated_by         VARCHAR(64),
    updated_time       TIMESTAMP,
    deleted            SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_inventory_batch PRIMARY KEY (id),
    CONSTRAINT uq_pha_inventory_batch UNIQUE (drug_id, warehouse_code, batch_no),
    CONSTRAINT ck_pha_inventory_quantity CHECK (quantity >= 0),
    CONSTRAINT ck_pha_inventory_available CHECK (available_quantity >= 0),
    CONSTRAINT ck_pha_inventory_locked CHECK (locked_quantity >= 0),
    CONSTRAINT ck_pha_inventory_balance CHECK (available_quantity + locked_quantity <= quantity)
);

CREATE INDEX idx_pha_inventory_drug ON pha_inventory_batch (drug_id, warehouse_code);
CREATE INDEX idx_pha_inventory_expiry ON pha_inventory_batch (expiry_date);

CREATE TABLE pha_inventory_transaction (
    id                 BIGINT          NOT NULL,
    transaction_no     VARCHAR(32)     NOT NULL,
    operation_type     VARCHAR(16)     NOT NULL,
    drug_id            BIGINT          NOT NULL,
    batch_id           BIGINT          NOT NULL,
    warehouse_code     VARCHAR(32)     NOT NULL,
    quantity_change    DECIMAL(18,4)   NOT NULL,
    quantity_before    DECIMAL(18,4)   NOT NULL,
    quantity_after     DECIMAL(18,4)   NOT NULL,
    reference_type     VARCHAR(32),
    reference_id       BIGINT,
    operator_id        BIGINT,
    operator_name      VARCHAR(64),
    reason             VARCHAR(256),
    occurred_time      TIMESTAMP       NOT NULL,
    created_by         VARCHAR(64),
    created_time       TIMESTAMP,
    updated_by         VARCHAR(64),
    updated_time       TIMESTAMP,
    deleted            SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_inventory_transaction PRIMARY KEY (id),
    CONSTRAINT uq_pha_inventory_transaction_no UNIQUE (transaction_no)
);

CREATE INDEX idx_pha_inventory_tx_drug ON pha_inventory_transaction (drug_id, occurred_time);
CREATE INDEX idx_pha_inventory_tx_batch ON pha_inventory_transaction (batch_id, occurred_time);
CREATE INDEX idx_pha_inventory_tx_reference ON pha_inventory_transaction (reference_type, reference_id);
