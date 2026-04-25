-- ============================================================
-- V033: Usuario SUPER_ADMIN inicial (sin empresa)
-- Credenciales por defecto:
--   username : superadmin
--   password : SuperAdmin2026!
-- Cambiar la contraseña tras el primer login.
-- ============================================================

INSERT INTO `user` (
    company_id,
    document_type_id,
    profile_id,
    document_number,
    first_name,
    last_name,
    username,
    password,
    plain_password,
    status,
    created_by
)
SELECT
    NULL,
    dt.id,
    p.id,
    '00000000',
    'Super',
    'Admin',
    'superadmin',
    '$2a$10$t/9qRCWsZ4kinryu9gKCGegIGAcgI13Oo0KmQ2tE2NPwHbBCfHYai',
    'SuperAdmin2026!',
    1,
    'system'
FROM
    document_type dt,
    profile p
WHERE
    dt.code = 'DNI'
    AND p.code = 'SUPER_ADMIN'
    AND NOT EXISTS (
        SELECT 1 FROM `user` WHERE username = 'superadmin'
    );
