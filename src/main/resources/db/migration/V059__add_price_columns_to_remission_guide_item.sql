ALTER TABLE remission_guide_item
    ADD COLUMN unit_price      DECIMAL(14,2) NULL AFTER unit_measure_sunat,
    ADD COLUMN subtotal_amount DECIMAL(14,2) NULL AFTER unit_price,
    ADD COLUMN tax_amount      DECIMAL(14,2) NULL AFTER subtotal_amount,
    ADD COLUMN total_amount    DECIMAL(14,2) NULL AFTER tax_amount;
