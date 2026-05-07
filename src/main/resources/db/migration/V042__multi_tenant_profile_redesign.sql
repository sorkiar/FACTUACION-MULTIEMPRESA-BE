-- ============================================================
-- V042: Multi-tenant profile redesign
--   1. Drop unique index on profile.code (was global; now per-company profiles allowed with same code)
--   2. Add company_id (FK) and is_system columns to profile
--   3. Create generic company (RUC 00000000000) to house superadmin
--   4. Move existing SUPER_ADMIN profile to generic company, mark is_system=1
--   5. Move superadmin user to generic company
--   6. For each existing non-generic company: create SYSTEMS profile + assign all menus + create superadmin user
-- ============================================================

-- 1. Drop unique index on profile.code (name is auto-generated, resolve dynamically)
SET @idx_name = (
    SELECT INDEX_NAME
    FROM INFORMATION_SCHEMA.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME = 'profile'
      AND COLUMN_NAME = 'code'
      AND NON_UNIQUE = 0
    LIMIT 1
);
SET @drop_idx = IF(
    @idx_name IS NOT NULL,
    CONCAT('ALTER TABLE profile DROP INDEX `', @idx_name, '`'),
    'SELECT 1'
);
PREPARE stmt FROM @drop_idx;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. Add company_id (nullable) and is_system to profile
ALTER TABLE profile
    ADD COLUMN company_id BIGINT     NULL               AFTER id,
    ADD COLUMN is_system  TINYINT(1) NOT NULL DEFAULT 0 AFTER company_id;

-- 3. Create generic company for superadmin (idempotent)
INSERT INTO company (ruc, business_name, status, created_by)
VALUES ('00000000000', 'SISTEMA', 1, 'system')
ON DUPLICATE KEY UPDATE id = id;

-- 4. Set existing SUPER_ADMIN profile to generic company + mark as system
UPDATE profile
SET company_id = (SELECT id FROM company WHERE ruc = '00000000000'),
    is_system  = 1
WHERE code = 'SUPER_ADMIN';

-- 5. Move superadmin user to generic company
UPDATE `user`
SET company_id = (SELECT id FROM company WHERE ruc = '00000000000')
WHERE username = 'superadmin'
  AND company_id IS NULL;

-- 6. Add FK constraint now that existing data is populated
ALTER TABLE profile
    ADD CONSTRAINT fk_profile_company
        FOREIGN KEY (company_id) REFERENCES company (id);

-- 7. For each existing non-generic company: create a SYSTEMS profile (is_system=1)
INSERT INTO profile (company_id, code, name, is_system, status, created_by)
SELECT c.id, 'SYSTEMS', 'SISTEMAS', 1, 1, 'system'
FROM company c
WHERE c.ruc != '00000000000'
  AND NOT EXISTS (
      SELECT 1 FROM profile p WHERE p.company_id = c.id AND p.is_system = 1
  );

-- 8. Assign all menus to the newly created SYSTEMS profiles
INSERT IGNORE INTO profile_menu (profile_id, menu_id)
SELECT p.id, m.id
FROM profile p
         JOIN company c ON p.company_id = c.id
         CROSS JOIN menu m
WHERE p.is_system = 1
  AND c.ruc != '00000000000';

-- 9. Create superadmin user in each non-generic company that doesn't already have one
--    (same credentials as the global superadmin so operators can log in immediately)
INSERT INTO `user` (company_id, document_type_id, profile_id,
                    document_number, first_name, last_name,
                    username, password, plain_password,
                    status, created_by)
SELECT c.id,
       (SELECT id FROM document_type WHERE code = 'DNI' LIMIT 1),
       p.id,
       '00000000',
       'Super',
       'Admin',
       'superadmin',
       '$2a$10$t/9qRCWsZ4kinryu9gKCGegIGAcgI13Oo0KmQ2tE2NPwHbBCfHYai',
       'SuperAdmin2026!',
       1,
       'system'
FROM company c
         JOIN profile p ON p.company_id = c.id AND p.is_system = 1
WHERE c.ruc != '00000000000'
  AND NOT EXISTS (
    SELECT 1 FROM `user` u
    WHERE u.company_id = c.id AND u.username = 'superadmin'
);
