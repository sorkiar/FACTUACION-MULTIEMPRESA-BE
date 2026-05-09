ALTER TABLE credit_debit_note_item
    ADD COLUMN item_type       VARCHAR(20)    NOT NULL DEFAULT 'PRODUCT' AFTER credit_debit_note_id,
    ADD COLUMN product_id      BIGINT         NULL     AFTER item_type,
    ADD COLUMN service_id      BIGINT         NULL     AFTER product_id,
    ADD COLUMN subtotal_amount DECIMAL(14,2)  NOT NULL DEFAULT 0 AFTER discount_percentage,
    ADD COLUMN tax_amount      DECIMAL(14,2)  NOT NULL DEFAULT 0 AFTER subtotal_amount,
    ADD COLUMN unit_measure_id BIGINT         NULL     AFTER total_amount,
    ADD CONSTRAINT fk_cdni_product      FOREIGN KEY (product_id)      REFERENCES product (id),
    ADD CONSTRAINT fk_cdni_service      FOREIGN KEY (service_id)      REFERENCES service (id),
    ADD CONSTRAINT fk_cdni_unit_measure FOREIGN KEY (unit_measure_id) REFERENCES unit_measure (id);
