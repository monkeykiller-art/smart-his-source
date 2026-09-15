ALTER TABLE ops_bill ADD COLUMN source_type VARCHAR(32);
ALTER TABLE ops_bill ADD COLUMN source_id BIGINT;

CREATE UNIQUE INDEX uq_ops_bill_source
    ON ops_bill (source_type, source_id)
    WHERE source_type IS NOT NULL AND source_id IS NOT NULL;
