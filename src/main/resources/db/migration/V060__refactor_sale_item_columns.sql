-- Rename unit_price_pen → unit_price and change precision to match entity
ALTER TABLE sale_item
    CHANGE COLUMN unit_price_pen unit_price DECIMAL(14, 2) NOT NULL;

-- Drop obsolete columns
ALTER TABLE sale_item
    DROP COLUMN unit_price_usd,
    DROP COLUMN discount_amount,
    DROP COLUMN unit_measure_sunat;

-- Add missing columns
ALTER TABLE sale_item
    ADD COLUMN discount_percentage DECIMAL(5, 2)  NOT NULL DEFAULT 0.00,
    ADD COLUMN subtotal_amount     DECIMAL(14, 2) NOT NULL DEFAULT 0.00,
    ADD COLUMN unit_measure_id     BIGINT         NULL,
    ADD CONSTRAINT fk_sale_item_unit_measure FOREIGN KEY (unit_measure_id) REFERENCES unit_measure (id);

-- Backfill subtotal_amount from total_amount for existing rows (total / 1.18)
UPDATE sale_item SET subtotal_amount = ROUND(total_amount / 1.18, 2) WHERE subtotal_amount = 0;
