-- ============================================================
-- V034: Menu & Profile permissions
--   1. Make profile.code nullable (allows profiles without code)
--   2. Create menu table (IF NOT EXISTS)
--   3. Create profile_menu join table (IF NOT EXISTS)
--   4. Seed menus from frontend route/sidebar structure (idempotent)
--   5. Assign all menus to existing profiles (INSERT IGNORE)
-- ============================================================

-- 1. Make profile.code nullable
ALTER TABLE profile
    MODIFY COLUMN code VARCHAR(20) NULL;

-- 2. Menu table
CREATE TABLE IF NOT EXISTS menu (
    id         BIGINT       AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    path       VARCHAR(200) NULL,
    parent_id  BIGINT       NULL,
    sort_order INT          NOT NULL DEFAULT 1,
    CONSTRAINT fk_menu_parent FOREIGN KEY (parent_id) REFERENCES menu (id)
);

-- 3. Profile-menu join table
CREATE TABLE IF NOT EXISTS profile_menu (
    profile_id BIGINT NOT NULL,
    menu_id    BIGINT NOT NULL,
    PRIMARY KEY (profile_id, menu_id),
    CONSTRAINT fk_pm_profile FOREIGN KEY (profile_id) REFERENCES profile (id),
    CONSTRAINT fk_pm_menu    FOREIGN KEY (menu_id)    REFERENCES menu (id)
);

-- 4. Seed menus (idempotent: insert only if name+parent combo does not exist)

-- Top-level: Inicio
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Inicio', '/', NULL, 1 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Inicio' AND parent_id IS NULL);

-- Top-level groups
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Inventario', NULL, NULL, 2 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Inventario' AND parent_id IS NULL);

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'General', NULL, NULL, 3 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'General' AND parent_id IS NULL);

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Facturación', NULL, NULL, 4 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Facturación' AND parent_id IS NULL);

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Reportes', NULL, NULL, 5 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Reportes' AND parent_id IS NULL);

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Configuración', '/configuration', NULL, 6 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Configuración' AND parent_id IS NULL);

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Seguridad', NULL, NULL, 7 FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Seguridad' AND parent_id IS NULL);

-- Inventario children
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Productos', '/products', p.id, 1
FROM menu p WHERE p.name = 'Inventario' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Productos');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Servicios', '/services', p.id, 2
FROM menu p WHERE p.name = 'Inventario' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Servicios');

-- General children
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Clientes', '/clients', p.id, 1
FROM menu p WHERE p.name = 'General' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Clientes');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Transportistas', '/carriers', p.id, 2
FROM menu p WHERE p.name = 'General' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Transportistas');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Conductores', '/drivers', p.id, 3
FROM menu p WHERE p.name = 'General' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Conductores');

-- Facturación children
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Ventas', '/sales', p.id, 1
FROM menu p WHERE p.name = 'Facturación' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Ventas');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Notas de Crédito / Débito', '/credit-debit-notes', p.id, 2
FROM menu p WHERE p.name = 'Facturación' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Notas de Crédito / Débito');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Guías de Remisión', '/remission-guides', p.id, 3
FROM menu p WHERE p.name = 'Facturación' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Guías de Remisión');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Adm. Comprobantes', '/sunat-documents', p.id, 4
FROM menu p WHERE p.name = 'Facturación' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Adm. Comprobantes');

-- Reportes children
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Reporte de Ventas', '/reports/sales', p.id, 1
FROM menu p WHERE p.name = 'Reportes' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Reporte de Ventas');

-- Seguridad children
INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Perfiles', '/profiles', p.id, 1
FROM menu p WHERE p.name = 'Seguridad' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Perfiles');

INSERT INTO menu (name, path, parent_id, sort_order)
SELECT 'Usuarios', '/users', p.id, 2
FROM menu p WHERE p.name = 'Seguridad' AND p.parent_id IS NULL
AND NOT EXISTS (SELECT 1 FROM menu WHERE name = 'Usuarios');

-- 5. Assign all menus to all existing profiles (INSERT IGNORE skips duplicates)
INSERT IGNORE INTO profile_menu (profile_id, menu_id)
SELECT p.id, m.id
FROM profile p, menu m;
