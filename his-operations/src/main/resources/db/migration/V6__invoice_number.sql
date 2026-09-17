ALTER TABLE ops_bill ADD COLUMN invoice_no VARCHAR(32);
CREATE UNIQUE INDEX uq_ops_bill_invoice_no ON ops_bill (invoice_no) WHERE invoice_no IS NOT NULL;
