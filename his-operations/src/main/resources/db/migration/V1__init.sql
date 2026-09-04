-- his_operations schema — billing, settlement, deposit, fee management
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ
-- Covers: fee items, bills, deposits, settlements, payments, accounts
-- Aligned with 住院管理系统: 入出院系统, 住院记费, 出院结算, 住院缴款

-- 1. Fee item dictionary (费用项目字典)
CREATE TABLE ops_fee_item (
    id              BIGINT          NOT NULL,
    item_code       VARCHAR(32)     NOT NULL,
    item_name       VARCHAR(128)    NOT NULL,
    name_pinyin     VARCHAR(256),
    item_class      VARCHAR(16)     NOT NULL DEFAULT 'DRUG',
    item_category   VARCHAR(32),
    spec            VARCHAR(128),
    unit            VARCHAR(16),
    unit_price      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    dosage_form     VARCHAR(32),
    is_insurance    SMALLINT        NOT NULL DEFAULT 1,
    insurance_ratio DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    is_self_pay     SMALLINT        NOT NULL DEFAULT 0,
    execute_dept_type VARCHAR(16),
    need_confirm    SMALLINT        NOT NULL DEFAULT 0,
    sort_order      INT             NOT NULL DEFAULT 0,
    item_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_fee_item PRIMARY KEY (id),
    CONSTRAINT uq_ops_fee_item_code UNIQUE (item_code)
);
CREATE INDEX idx_ops_fee_item_name ON ops_fee_item (item_name);
CREATE INDEX idx_ops_fee_item_class ON ops_fee_item (item_class);

-- 2. Bill head (费用汇总 — 住院记费管理)
CREATE TABLE ops_bill (
    id              BIGINT          NOT NULL,
    bill_no         VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    encounter_id    BIGINT,
    visit_type      VARCHAR(16)     NOT NULL DEFAULT 'INPATIENT',
    dept_id         BIGINT          NOT NULL,
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    discount_amount DECIMAL(18,4)   NOT NULL DEFAULT 0,
    payable_amount  DECIMAL(18,4)   NOT NULL DEFAULT 0,
    paid_amount     DECIMAL(18,4)   NOT NULL DEFAULT 0,
    bill_status     VARCHAR(16)     NOT NULL DEFAULT 'UNSETTLED',
    bill_type       VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_bill PRIMARY KEY (id),
    CONSTRAINT uq_ops_bill_no UNIQUE (bill_no)
);
CREATE INDEX idx_ops_bill_patient ON ops_bill (patient_id);
CREATE INDEX idx_ops_bill_admission ON ops_bill (admission_id, bill_status);
CREATE INDEX idx_ops_bill_status ON ops_bill (bill_status);

-- 3. Bill item (费用明细)
CREATE TABLE ops_bill_item (
    id              BIGINT          NOT NULL,
    bill_id         BIGINT          NOT NULL,
    item_seq        INT             NOT NULL DEFAULT 1,
    fee_item_id     BIGINT,
    item_code       VARCHAR(32),
    item_name       VARCHAR(128)    NOT NULL,
    item_class      VARCHAR(16),
    spec            VARCHAR(128),
    unit            VARCHAR(16),
    unit_price      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    quantity        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    amount          DECIMAL(18,4)   NOT NULL DEFAULT 0,
    charge_dept_id  BIGINT,
    execute_dept_id BIGINT,
    order_id        BIGINT,
    order_item_id   BIGINT,
    presc_time      TIMESTAMP,
    charge_time     TIMESTAMP,
    charger_id      VARCHAR(64),
    is_refunded     SMALLINT        NOT NULL DEFAULT 0,
    refund_time     TIMESTAMP,
    refund_by       VARCHAR(64),
    refund_reason   VARCHAR(256),
    item_status     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_bill_item PRIMARY KEY (id)
);
CREATE INDEX idx_ops_bill_item_bill ON ops_bill_item (bill_id);
CREATE INDEX idx_ops_bill_item_patient ON ops_bill_item (item_code, charge_time);

-- 4. Deposit / prepayment (住院押金)
CREATE TABLE ops_deposit (
    id              BIGINT          NOT NULL,
    deposit_no      VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    receipt_no      VARCHAR(32),
    amount          DECIMAL(18,4)   NOT NULL DEFAULT 0,
    pay_method      VARCHAR(16)     NOT NULL DEFAULT 'CASH',
    deposit_type    VARCHAR(16)     NOT NULL DEFAULT 'PAYMENT',
    balance_before  DECIMAL(18,4)   NOT NULL DEFAULT 0,
    balance_after   DECIMAL(18,4)   NOT NULL DEFAULT 0,
    cashier_id      VARCHAR(64),
    cashier_name    VARCHAR(64),
    charge_time     TIMESTAMP,
    remark          VARCHAR(256),
    deposit_status  VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_deposit PRIMARY KEY (id),
    CONSTRAINT uq_ops_deposit_no UNIQUE (deposit_no)
);
CREATE INDEX idx_ops_deposit_patient ON ops_deposit (patient_id);
CREATE INDEX idx_ops_deposit_admission ON ops_deposit (admission_id);
CREATE INDEX idx_ops_deposit_cashier ON ops_deposit (cashier_id, charge_time);

-- 5. Patient deposit account (患者押金账户余额汇总)
CREATE TABLE ops_deposit_account (
    id              BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    total_deposit   DECIMAL(18,4)   NOT NULL DEFAULT 0,
    total_charged   DECIMAL(18,4)   NOT NULL DEFAULT 0,
    balance         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    frozen_amount   DECIMAL(18,4)   NOT NULL DEFAULT 0,
    account_status  VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_deposit_acct PRIMARY KEY (id)
);
CREATE INDEX idx_ops_deposit_acct_patient ON ops_deposit_account (patient_id);
CREATE INDEX idx_ops_deposit_acct_admission ON ops_deposit_account (admission_id);

-- 6. Settlement head (结算主表 — 出院结算)
CREATE TABLE ops_settlement (
    id              BIGINT          NOT NULL,
    settle_no       VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    encounter_id    BIGINT,
    visit_type      VARCHAR(16)     NOT NULL DEFAULT 'INPATIENT',
    patient_type    VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    invoice_no      VARCHAR(32),
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    insurance_amount DECIMAL(18,4)  NOT NULL DEFAULT 0,
    deposit_amount  DECIMAL(18,4)   NOT NULL DEFAULT 0,
    self_pay_amount DECIMAL(18,4)   NOT NULL DEFAULT 0,
    settle_balance  DECIMAL(18,4)   NOT NULL DEFAULT 0,
    settle_type     VARCHAR(16)     NOT NULL DEFAULT 'FINAL',
    pay_method      VARCHAR(16)     NOT NULL DEFAULT 'CASH',
    cashier_id      VARCHAR(64),
    cashier_name    VARCHAR(64),
    settle_time     TIMESTAMP,
    settle_status   VARCHAR(16)     NOT NULL DEFAULT 'SETTLED',
    cancel_time     TIMESTAMP,
    cancel_by       VARCHAR(64),
    cancel_reason   VARCHAR(256),
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_settlement PRIMARY KEY (id),
    CONSTRAINT uq_ops_settlement_no UNIQUE (settle_no)
);
CREATE INDEX idx_ops_settlement_patient ON ops_settlement (patient_id);
CREATE INDEX idx_ops_settlement_admission ON ops_settlement (admission_id);
CREATE INDEX idx_ops_settlement_cashier ON ops_settlement (cashier_id, settle_time);
CREATE INDEX idx_ops_settlement_type ON ops_settlement (settle_type, settle_status);

-- 7. Settlement item (结算明细 — 费用分类汇总)
CREATE TABLE ops_settlement_item (
    id              BIGINT          NOT NULL,
    settlement_id   BIGINT          NOT NULL,
    item_class      VARCHAR(16)     NOT NULL,
    item_count      INT             NOT NULL DEFAULT 0,
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    insurance_amount DECIMAL(18,4)  NOT NULL DEFAULT 0,
    self_pay_amount DECIMAL(18,4)   NOT NULL DEFAULT 0,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_settle_item PRIMARY KEY (id)
);
CREATE INDEX idx_ops_settle_item_settle ON ops_settlement_item (settlement_id);

-- 8. Payment record (支付记录 — 结算支付明细)
CREATE TABLE ops_payment (
    id              BIGINT          NOT NULL,
    settlement_id   BIGINT          NOT NULL,
    payment_no      VARCHAR(32)     NOT NULL,
    pay_method      VARCHAR(16)     NOT NULL DEFAULT 'CASH',
    pay_amount      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    pay_source      VARCHAR(16)     NOT NULL DEFAULT 'CASHIER',
    reference_no    VARCHAR(64),
    pay_time        TIMESTAMP,
    pay_status      VARCHAR(16)     NOT NULL DEFAULT 'SUCCESS',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_payment PRIMARY KEY (id),
    CONSTRAINT uq_ops_payment_no UNIQUE (payment_no)
);
CREATE INDEX idx_ops_payment_settle ON ops_payment (settlement_id);

-- 9. Account / reconciliation (缴款单 — 住院缴款)
CREATE TABLE ops_account (
    id              BIGINT          NOT NULL,
    account_no      VARCHAR(32)     NOT NULL,
    cashier_id      VARCHAR(64)     NOT NULL,
    cashier_name    VARCHAR(64),
    settle_type     VARCHAR(16),
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    cash_amount     DECIMAL(18,4)   NOT NULL DEFAULT 0,
    pos_amount      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    other_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    bill_count      INT             NOT NULL DEFAULT 0,
    account_date    DATE,
    submit_time     TIMESTAMP,
    receive_time    TIMESTAMP,
    receiver_id     VARCHAR(64),
    account_status  VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    receive_status  VARCHAR(16)     NOT NULL DEFAULT 'UNRECEIVED',
    print_count     INT             NOT NULL DEFAULT 0,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_account PRIMARY KEY (id),
    CONSTRAINT uq_ops_account_no UNIQUE (account_no)
);
CREATE INDEX idx_ops_account_cashier ON ops_account (cashier_id, account_date);
CREATE INDEX idx_ops_account_status ON ops_account (account_status);

-- 10. Bed fee binding (床位费用绑定 — from 床位维护)
CREATE TABLE ops_bed_fee_bind (
    id              BIGINT          NOT NULL,
    bed_id          BIGINT          NOT NULL,
    fee_item_id     BIGINT          NOT NULL,
    fee_item_code   VARCHAR(32),
    fee_item_name   VARCHAR(128),
    daily_fee       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_bed_fee_bind PRIMARY KEY (id)
);
CREATE INDEX idx_ops_bed_fee_bind_bed ON ops_bed_fee_bind (bed_id);

-- 11. Fee template (护士账单模板)
CREATE TABLE ops_fee_template (
    id              BIGINT          NOT NULL,
    template_name   VARCHAR(128)    NOT NULL,
    template_category VARCHAR(32),
    template_level  VARCHAR(16)     NOT NULL DEFAULT 'DEPARTMENT',
    dept_id         BIGINT,
    level_type      VARCHAR(16),
    sort_order      INT             NOT NULL DEFAULT 0,
    template_status SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_fee_tpl PRIMARY KEY (id)
);
CREATE INDEX idx_ops_fee_tpl_dept ON ops_fee_template (dept_id);

-- 12. Fee template item (护士账单模板明细)
CREATE TABLE ops_fee_template_item (
    id              BIGINT          NOT NULL,
    template_id     BIGINT          NOT NULL,
    item_type       VARCHAR(16)     NOT NULL DEFAULT 'FEE',
    item_code       VARCHAR(32),
    item_name       VARCHAR(128)    NOT NULL,
    quantity        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    unit            VARCHAR(16),
    execute_dept_id BIGINT,
    item_seq        INT             NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_ops_fee_tpl_item PRIMARY KEY (id)
);
CREATE INDEX idx_ops_fee_tpl_item_tpl ON ops_fee_template_item (template_id);
