-- Remove duplicate exchange_rate rows, keep one per (rate_date, type)
DELETE FROM exchange_rate
WHERE id NOT IN (
    SELECT min_id FROM (
        SELECT MIN(id) AS min_id FROM exchange_rate GROUP BY rate_date, type
    ) AS keep
);

-- Drop company FK and old unique constraint, then remove company_id column
ALTER TABLE exchange_rate
    DROP FOREIGN KEY fk_exchange_rate_company,
    DROP INDEX uk_exchange_rate_date_type_company,
    DROP COLUMN company_id,
    ADD CONSTRAINT uk_exchange_rate_date_type UNIQUE (rate_date, type);
