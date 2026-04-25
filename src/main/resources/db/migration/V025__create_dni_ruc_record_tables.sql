-- V025: NOOP — dni_record y ruc_record ya creadas en V002 con esquema completo.
--        INSERT INTO configuration omitido: requiere company_id (NOT NULL);
--        se siembra al crear cada empresa desde CompanyServiceImpl.
SELECT 1;
