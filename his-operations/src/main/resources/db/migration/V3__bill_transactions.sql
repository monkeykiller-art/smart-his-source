-- Auditable cashier transactions for outpatient and emergency bills.
-- Keep these separate from ops_payment, which is tied to inpatient settlements.
ALTER TABLE ops_bill ADD COLUMN void_reason VARCHAR(256);

CREATE TABLE ops_bill_transaction (
    id              BIGINT          NOT NULL,
    bill_id         BIGINT          NOT NULL,
    transaction_no  VARCHAR(32)     NOT NULL,
    transaction_type VARCHAR(16)    NOT NULL,
    amount          DECIMAL(18,4)   NOT NULL,
    pay_method      VARCHAR(16),
    reference_no    VARCHAR(64),
    idempotency_key VARCHAR(64)     NOT NULL,
    reason          VARCHAR(256),
    transaction_time TIMESTAMP      NOT NULL,
    transaction_status VARCHAR(16)  NOT NULL DEFAULT 'SUCCESS',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_bill_transaction PRIMARY KEY (id),
    CONSTRAINT uq_ops_bill_transaction_no UNIQUE (transaction_no),
    CONSTRAINT uq_ops_bill_transaction_idempotency UNIQUE (idempotency_key),
    CONSTRAINT ck_ops_bill_transaction_amount CHECK (amount > 0),
    CONSTRAINT ck_ops_bill_transaction_type CHECK (transaction_type IN ('PAYMENT', 'REFUND'))
);
CREATE INDEX idx_ops_bill_transaction_bill ON ops_bill_transaction (bill_id, transaction_time);
