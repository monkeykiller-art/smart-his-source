-- his_resource schema — bed/ward management, drug catalog, stock, dispensing
-- Follows D4 portable SQL rules: BIGINT PK, DECIMAL(18,4) money, SMALLINT flags, TIMESTAMP no TZ
-- Covers: ward, bed, bed record, drug catalog, drug price, stock, stock movement, dispense

-- 1. Ward (病区)
CREATE TABLE res_ward (
    id              BIGINT          NOT NULL,
    ward_code       VARCHAR(32)     NOT NULL,
    ward_name       VARCHAR(64)     NOT NULL,
    dept_id         BIGINT          NOT NULL,
    ward_type       VARCHAR(16)     NOT NULL DEFAULT 'GENERAL',
    floor_location  VARCHAR(128),
    bed_count       INT             NOT NULL DEFAULT 0,
    nurse_station   VARCHAR(64),
    head_nurse_id   BIGINT,
    ward_status     VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    description     VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_ward PRIMARY KEY (id),
    CONSTRAINT uq_res_ward_code UNIQUE (ward_code)
);
CREATE INDEX idx_res_ward_dept ON res_ward (dept_id);

-- 2. Bed (床位)
CREATE TABLE res_bed (
    id              BIGINT          NOT NULL,
    bed_no          VARCHAR(16)     NOT NULL,
    ward_id         BIGINT          NOT NULL,
    room_no         VARCHAR(16),
    bed_type        VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    bed_rank        VARCHAR(16)     NOT NULL DEFAULT 'THIRD_CLASS',
    floor_no        VARCHAR(8),
    bed_status      VARCHAR(16)     NOT NULL DEFAULT 'AVAILABLE',
    is_male         SMALLINT        NOT NULL DEFAULT 1,
    fee_item_id     BIGINT,
    daily_fee       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    sort_order      INT             NOT NULL DEFAULT 0,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_bed PRIMARY KEY (id),
    CONSTRAINT uq_res_bed_ward_no UNIQUE (ward_id, bed_no)
);
CREATE INDEX idx_res_bed_ward ON res_bed (ward_id);
CREATE INDEX idx_res_bed_status ON res_bed (bed_status);

-- 3. Bed record (床位占用记录 — 床位一览)
CREATE TABLE res_bed_record (
    id              BIGINT          NOT NULL,
    bed_id          BIGINT          NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT          NOT NULL,
    ward_id         BIGINT          NOT NULL,
    bed_no          VARCHAR(16),
    admit_time      TIMESTAMP       NOT NULL,
    discharge_time  TIMESTAMP,
    expected_stay   INT,
    record_status   VARCHAR(16)     NOT NULL DEFAULT 'OCCUPIED',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_bed_record PRIMARY KEY (id)
);
CREATE INDEX idx_res_bed_record_bed ON res_bed_record (bed_id, record_status);
CREATE INDEX idx_res_bed_record_patient ON res_bed_record (patient_id);
CREATE INDEX idx_res_bed_record_ward ON res_bed_record (ward_id, record_status);
CREATE INDEX idx_res_bed_record_admission ON res_bed_record (admission_id);

-- 4. Drug catalog (药品目录)
CREATE TABLE res_drug (
    id              BIGINT          NOT NULL,
    drug_code       VARCHAR(32)     NOT NULL,
    drug_name       VARCHAR(128)    NOT NULL,
    name_pinyin     VARCHAR(256),
    generic_name    VARCHAR(128),
    dosage_form     VARCHAR(32),
    spec            VARCHAR(128),
    unit            VARCHAR(16)     NOT NULL,
    pack_unit       VARCHAR(16),
    pack_qty        DECIMAL(18,4)   NOT NULL DEFAULT 1,
    manufacturer    VARCHAR(128),
    approval_no     VARCHAR(64),
    bar_code        VARCHAR(32),
    drug_type       VARCHAR(16)     NOT NULL DEFAULT 'WESTERN',
    is_insurance    SMALLINT        NOT NULL DEFAULT 1,
    insurance_ratio DECIMAL(5,2)    NOT NULL DEFAULT 100.00,
    is_narcotic     SMALLINT        NOT NULL DEFAULT 0,
    is_psychotropic SMALLINT        NOT NULL DEFAULT 0,
    is_antibiotic   SMALLINT        NOT NULL DEFAULT 0,
    antibiotic_level SMALLINT       NOT NULL DEFAULT 0,
    need_skin_test  SMALLINT        NOT NULL DEFAULT 0,
    max_single_dose DECIMAL(18,4),
    max_daily_dose  DECIMAL(18,4),
    storage_condition VARCHAR(64),
    sort_order      INT             NOT NULL DEFAULT 0,
    drug_status     SMALLINT        NOT NULL DEFAULT 1,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_drug PRIMARY KEY (id),
    CONSTRAINT uq_res_drug_code UNIQUE (drug_code)
);
CREATE INDEX idx_res_drug_name ON res_drug (drug_name);
CREATE INDEX idx_res_drug_pinyin ON res_drug (name_pinyin);
CREATE INDEX idx_res_drug_type ON res_drug (drug_type);

-- 5. Drug price (药品价格 — 多药房价格)
CREATE TABLE res_drug_price (
    id              BIGINT          NOT NULL,
    drug_id         BIGINT          NOT NULL,
    pharmacy_id     BIGINT,
    price           DECIMAL(18,4)   NOT NULL DEFAULT 0,
    retail_price    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    is_active       SMALLINT        NOT NULL DEFAULT 1,
    effective_from  TIMESTAMP,
    effective_to    TIMESTAMP,
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_drug_price PRIMARY KEY (id)
);
CREATE INDEX idx_res_drug_price_drug ON res_drug_price (drug_id);

-- 6. Stock (库存)
CREATE TABLE res_stock (
    id              BIGINT          NOT NULL,
    drug_id         BIGINT          NOT NULL,
    pharmacy_id     BIGINT          NOT NULL,
    batch_no        VARCHAR(64),
    quantity         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    unit_cost       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    produce_date    DATE,
    expiry_date     DATE,
    supplier_id     BIGINT,
    warehouse_area  VARCHAR(64),
    stock_status    VARCHAR(16)     NOT NULL DEFAULT 'NORMAL',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_stock PRIMARY KEY (id)
);
CREATE INDEX idx_res_stock_drug ON res_stock (drug_id);
CREATE INDEX idx_res_stock_pharmacy ON res_stock (pharmacy_id, drug_id);
CREATE INDEX idx_res_stock_expiry ON res_stock (expiry_date);

-- 7. Stock movement (库存变动)
CREATE TABLE res_stock_movement (
    id              BIGINT          NOT NULL,
    movement_no     VARCHAR(32)     NOT NULL,
    drug_id         BIGINT          NOT NULL,
    pharmacy_id     BIGINT          NOT NULL,
    batch_no        VARCHAR(64),
    movement_type   VARCHAR(16)     NOT NULL,
    quantity         DECIMAL(18,4)   NOT NULL DEFAULT 0,
    unit_cost       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    before_qty      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    after_qty       DECIMAL(18,4)   NOT NULL DEFAULT 0,
    reference_type  VARCHAR(32),
    reference_id    BIGINT,
    operator_id     VARCHAR(64),
    movement_time   TIMESTAMP,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_stock_movement PRIMARY KEY (id),
    CONSTRAINT uq_res_stock_movement_no UNIQUE (movement_no)
);
CREATE INDEX idx_res_stock_movement_drug ON res_stock_movement (drug_id);
CREATE INDEX idx_res_stock_movement_pharmacy ON res_stock_movement (pharmacy_id, movement_time);

-- 8. Dispense record (发药记录)
CREATE TABLE res_dispense (
    id              BIGINT          NOT NULL,
    dispense_no     VARCHAR(32)     NOT NULL,
    patient_id      BIGINT          NOT NULL,
    admission_id    BIGINT,
    encounter_id    BIGINT,
    pharmacy_id     BIGINT          NOT NULL,
    order_id        BIGINT,
    bill_id         BIGINT,
    dispense_type   VARCHAR(16)     NOT NULL DEFAULT 'OUTPATIENT',
    dispense_status VARCHAR(16)     NOT NULL DEFAULT 'PENDING',
    total_amount    DECIMAL(18,4)   NOT NULL DEFAULT 0,
    dispense_time   TIMESTAMP,
    dispenser_id    VARCHAR(64),
    dispenser_name  VARCHAR(64),
    reviewer_id     VARCHAR(64),
    review_time     TIMESTAMP,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_dispense PRIMARY KEY (id),
    CONSTRAINT uq_res_dispense_no UNIQUE (dispense_no)
);
CREATE INDEX idx_res_dispense_patient ON res_dispense (patient_id);
CREATE INDEX idx_res_dispense_pharmacy ON res_dispense (pharmacy_id, dispense_status);
CREATE INDEX idx_res_dispense_order ON res_dispense (order_id);

-- 9. Dispense item (发药明细)
CREATE TABLE res_dispense_item (
    id              BIGINT          NOT NULL,
    dispense_id     BIGINT          NOT NULL,
    drug_id         BIGINT          NOT NULL,
    drug_code       VARCHAR(32),
    drug_name       VARCHAR(128)    NOT NULL,
    spec            VARCHAR(128),
    quantity         DECIMAL(18,4)   NOT NULL DEFAULT 1,
    unit            VARCHAR(16),
    unit_price      DECIMAL(18,4)   NOT NULL DEFAULT 0,
    amount          DECIMAL(18,4)   NOT NULL DEFAULT 0,
    batch_no        VARCHAR(64),
    usage_method    VARCHAR(32),
    frequency       VARCHAR(32),
    days            INT,
    remark          VARCHAR(256),
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_dispense_item PRIMARY KEY (id)
);
CREATE INDEX idx_res_dispense_item_dispense ON res_dispense_item (dispense_id);

-- 10. Pharmacy (药房)
CREATE TABLE res_pharmacy (
    id              BIGINT          NOT NULL,
    pharmacy_code   VARCHAR(32)     NOT NULL,
    pharmacy_name   VARCHAR(64)     NOT NULL,
    pharmacy_type   VARCHAR(16)     NOT NULL DEFAULT 'OUTPATIENT',
    dept_id         BIGINT          NOT NULL,
    location        VARCHAR(128),
    phone           VARCHAR(32),
    pharmacy_status VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_pharmacy PRIMARY KEY (id),
    CONSTRAINT uq_res_pharmacy_code UNIQUE (pharmacy_code)
);
CREATE INDEX idx_res_pharmacy_dept ON res_pharmacy (dept_id);

-- 11. Supplier (供应商)
CREATE TABLE res_supplier (
    id              BIGINT          NOT NULL,
    supplier_code   VARCHAR(32)     NOT NULL,
    supplier_name   VARCHAR(128)    NOT NULL,
    contact_person  VARCHAR(64),
    contact_phone   VARCHAR(32),
    address         VARCHAR(256),
    license_no      VARCHAR(64),
    supplier_type   VARCHAR(16)     NOT NULL DEFAULT 'DRUG',
    supplier_status VARCHAR(16)     NOT NULL DEFAULT 'ACTIVE',
    created_by      VARCHAR(64),
    created_time    TIMESTAMP,
    updated_by      VARCHAR(64),
    updated_time    TIMESTAMP,
    deleted         SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_res_supplier PRIMARY KEY (id),
    CONSTRAINT uq_res_supplier_code UNIQUE (supplier_code)
);
