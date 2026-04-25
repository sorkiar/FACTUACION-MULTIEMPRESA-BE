-- ============================================================
-- V027: Carrier & Driver master tables
--       Migrates existing remission_guide / remission_guide_driver
--       inline data to the new master tables before dropping columns.
-- ============================================================

-- 1. Carrier (transportista) master table
CREATE TABLE carrier (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id    BIGINT       NOT NULL,
    doc_type      VARCHAR(5)   NOT NULL DEFAULT 'RUC',
    doc_number    VARCHAR(20)  NOT NULL,
    business_name VARCHAR(200) NOT NULL,
    status        INT          NOT NULL DEFAULT 1 COMMENT '1=ACTIVO, 0=INACTIVO',
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(50)  NOT NULL,
    updated_at    DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50)  DEFAULT NULL,
    deleted_at    DATETIME     DEFAULT NULL,
    deleted_by    VARCHAR(50)  DEFAULT NULL,
    CONSTRAINT fk_carrier_company FOREIGN KEY (company_id) REFERENCES company (id)
);

-- 2. Driver (conductor) master table
CREATE TABLE driver (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    company_id     BIGINT       NOT NULL,
    doc_type       VARCHAR(5)   NOT NULL DEFAULT 'DNI',
    doc_number     VARCHAR(20)  NOT NULL,
    first_name     VARCHAR(100) NOT NULL,
    last_name      VARCHAR(100) NOT NULL,
    license_number VARCHAR(30)  NOT NULL,
    status         INT          NOT NULL DEFAULT 1 COMMENT '1=ACTIVO, 0=INACTIVO',
    created_at     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by     VARCHAR(50)  NOT NULL,
    updated_at     DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by     VARCHAR(50)  DEFAULT NULL,
    deleted_at     DATETIME     DEFAULT NULL,
    deleted_by     VARCHAR(50)  DEFAULT NULL,
    CONSTRAINT fk_driver_company FOREIGN KEY (company_id) REFERENCES company (id)
);

-- 3. Driver vehicle plates (one driver → many plates)
CREATE TABLE driver_vehicle (
    id         BIGINT      AUTO_INCREMENT PRIMARY KEY,
    driver_id  BIGINT      NOT NULL,
    plate      VARCHAR(20) NOT NULL,
    created_at DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50) NOT NULL,
    updated_at DATETIME    DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by VARCHAR(50) DEFAULT NULL,
    deleted_at DATETIME    DEFAULT NULL,
    deleted_by VARCHAR(50) DEFAULT NULL,
    CONSTRAINT fk_dv_driver FOREIGN KEY (driver_id) REFERENCES driver (id)
);

-- ============================================================
-- PARTE A: remission_guide → carrier
-- ============================================================

-- 4. Add carrier_id FK column (nullable while migrating)
ALTER TABLE remission_guide
    ADD COLUMN carrier_id BIGINT NULL AFTER minor_vehicle_transfer,
    ADD CONSTRAINT fk_rg_carrier FOREIGN KEY (carrier_id) REFERENCES carrier (id);

-- 5. Populate carrier table: one row per unique carrier_doc_number
--    When the same doc_number appears with different names, MIN() picks one.
INSERT INTO carrier (doc_type, doc_number, business_name, status, created_by)
SELECT
    COALESCE(NULLIF(TRIM(MIN(carrier_doc_type)), ''), 'RUC'),
    carrier_doc_number,
    MIN(carrier_name),
    1,
    'migration'
FROM remission_guide
WHERE carrier_doc_number IS NOT NULL
  AND TRIM(carrier_doc_number) != ''
GROUP BY carrier_doc_number;

-- 6. Point remission_guide.carrier_id to the migrated carrier records
UPDATE remission_guide rg
    JOIN carrier c ON c.doc_number = rg.carrier_doc_number
SET rg.carrier_id = c.id
WHERE rg.carrier_doc_number IS NOT NULL
  AND TRIM(rg.carrier_doc_number) != '';

-- 7. Drop old inline carrier columns
ALTER TABLE remission_guide
    DROP COLUMN carrier_doc_type,
    DROP COLUMN carrier_doc_number,
    DROP COLUMN carrier_name;

-- ============================================================
-- PARTE B: remission_guide_driver → driver + driver_vehicle
-- ============================================================

-- 8. Add new FK columns (nullable while migrating)
ALTER TABLE remission_guide_driver
    ADD COLUMN driver_id         BIGINT NULL AFTER remission_guide_id,
    ADD COLUMN driver_vehicle_id BIGINT NULL AFTER driver_id,
    ADD CONSTRAINT fk_rgd_driver  FOREIGN KEY (driver_id)         REFERENCES driver (id),
    ADD CONSTRAINT fk_rgd_vehicle FOREIGN KEY (driver_vehicle_id) REFERENCES driver_vehicle (id);

-- 9. Populate driver table: one row per unique driver_doc_number
--    When the same doc_number appears with different names, MIN() picks one.
INSERT INTO driver (doc_type, doc_number, first_name, last_name, license_number, status, created_by)
SELECT
    COALESCE(NULLIF(TRIM(MIN(driver_doc_type)), ''), 'DNI'),
    driver_doc_number,
    MIN(driver_first_name),
    MIN(driver_last_name),
    MIN(driver_license_number),
    1,
    'migration'
FROM remission_guide_driver
WHERE driver_doc_number IS NOT NULL
  AND TRIM(driver_doc_number) != ''
GROUP BY driver_doc_number;

-- 10. Populate driver_vehicle: one row per unique (driver, plate) pair
INSERT INTO driver_vehicle (driver_id, plate, created_by)
SELECT DISTINCT
    d.id,
    rgd.vehicle_plate,
    'migration'
FROM remission_guide_driver rgd
JOIN driver d ON d.doc_number = rgd.driver_doc_number
WHERE rgd.vehicle_plate IS NOT NULL
  AND TRIM(rgd.vehicle_plate) != '';

-- 11. Update remission_guide_driver with the new FK values
UPDATE remission_guide_driver rgd
    JOIN driver d  ON d.doc_number = rgd.driver_doc_number
    JOIN driver_vehicle dv ON dv.driver_id = d.id AND dv.plate = rgd.vehicle_plate
SET rgd.driver_id         = d.id,
    rgd.driver_vehicle_id = dv.id
WHERE rgd.driver_doc_number IS NOT NULL
  AND TRIM(rgd.driver_doc_number) != '';

-- 12. Remove any rows that could not be migrated (safety — should not occur)
DELETE FROM remission_guide_driver WHERE driver_id IS NULL;

-- 13. Enforce NOT NULL on driver_id now that all rows are populated
ALTER TABLE remission_guide_driver
    MODIFY COLUMN driver_id BIGINT NOT NULL;

-- 14. Drop old inline driver columns
ALTER TABLE remission_guide_driver
    DROP COLUMN driver_doc_type,
    DROP COLUMN driver_doc_number,
    DROP COLUMN driver_first_name,
    DROP COLUMN driver_last_name,
    DROP COLUMN driver_license_number,
    DROP COLUMN vehicle_plate;
