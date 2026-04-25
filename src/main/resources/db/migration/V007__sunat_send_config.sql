-- V007: NOOP — configuration requiere company_id (NOT NULL).
-- En multi-empresa, la configuración inicial se siembra al crear cada empresa
-- desde la capa de servicio (CompanyServiceImpl), no en migraciones globales.
SELECT 1;
