CREATE TABLE pha_drug_catalog (
    id                BIGINT          NOT NULL,
    drug_code         VARCHAR(32)     NOT NULL,
    generic_name      VARCHAR(128)    NOT NULL,
    trade_name        VARCHAR(128),
    pinyin_code       VARCHAR(32),
    dosage_form       VARCHAR(32)     NOT NULL,
    strength          VARCHAR(64)     NOT NULL,
    manufacturer      VARCHAR(128)    NOT NULL,
    approval_no       VARCHAR(64),
    package_unit      VARCHAR(16)     NOT NULL,
    min_unit          VARCHAR(16)     NOT NULL,
    conversion_factor DECIMAL(18,4)  NOT NULL,
    purchase_price    DECIMAL(18,4)  NOT NULL,
    retail_price      DECIMAL(18,4)  NOT NULL,
    prescription_type VARCHAR(16)     NOT NULL DEFAULT 'RX',
    antibiotic_level VARCHAR(16),
    is_active         SMALLINT        NOT NULL DEFAULT 1,
    created_by        VARCHAR(64),
    created_time      TIMESTAMP,
    updated_by        VARCHAR(64),
    updated_time      TIMESTAMP,
    deleted           SMALLINT        NOT NULL DEFAULT 0,
    CONSTRAINT pk_pha_drug_catalog PRIMARY KEY (id),
    CONSTRAINT uq_pha_drug_catalog_code UNIQUE (drug_code),
    CONSTRAINT ck_pha_drug_conversion CHECK (conversion_factor > 0),
    CONSTRAINT ck_pha_drug_purchase_price CHECK (purchase_price >= 0),
    CONSTRAINT ck_pha_drug_retail_price CHECK (retail_price >= 0)
);

CREATE INDEX idx_pha_drug_catalog_name ON pha_drug_catalog (generic_name);
CREATE INDEX idx_pha_drug_catalog_pinyin ON pha_drug_catalog (pinyin_code);
CREATE INDEX idx_pha_drug_catalog_active ON pha_drug_catalog (is_active);
