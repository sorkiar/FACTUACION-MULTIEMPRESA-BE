-- V047: Add code_sunat and symbol to unit_measure
--   code_sunat backfilled from code (existing seeds already use SUNAT codes)
ALTER TABLE unit_measure
    ADD COLUMN code_sunat VARCHAR(4)  NOT NULL DEFAULT '' AFTER code,
    ADD COLUMN symbol     VARCHAR(10) NULL              AFTER name;

UPDATE unit_measure SET code_sunat = code WHERE code_sunat = '';

ALTER TABLE unit_measure ALTER COLUMN code_sunat DROP DEFAULT;
