-- ============================================================
-- V013: Restaurar punto de llegada en remission_guide
--       y limpiar campos de destino de recipient
-- ============================================================

-- 1. Agregar columnas de punto de llegada de vuelta a remission_guide
ALTER TABLE remission_guide
    ADD COLUMN destination_address   VARCHAR(500) NULL AFTER origin_local_code,
    ADD COLUMN destination_ubigeo    VARCHAR(10)  NULL AFTER destination_address,
    ADD COLUMN destination_local_code VARCHAR(20) NULL AFTER destination_ubigeo;

-- 2. Restaurar datos desde recipient (los tenía porque V012 los migró incorrectamente)
UPDATE remission_guide rg
INNER JOIN recipient r ON rg.recipient_id = r.id
SET rg.destination_address    = r.address,
    rg.destination_ubigeo     = r.ubigeo,
    rg.destination_local_code = r.local_code;

-- 3. Limpiar recipient.address (contenía datos de destino, no del propio destinatario)
UPDATE recipient SET address = NULL;

-- 4. Eliminar ubigeo y local_code de recipient (eran datos de punto de llegada)
ALTER TABLE recipient
    DROP COLUMN ubigeo,
    DROP COLUMN local_code;
