-- ============================================================
-- V012: Crear tabla recipient y migrar datos de remission_guide
-- ============================================================

-- 1. Crear tabla recipient (con columna temporal para migración)
CREATE TABLE IF NOT EXISTS recipient (
    id              BIGINT          AUTO_INCREMENT PRIMARY KEY,
    doc_type        VARCHAR(5)      NOT NULL,
    doc_number      VARCHAR(20)     NOT NULL,
    name            VARCHAR(200)    NOT NULL,
    address         VARCHAR(500),
    ubigeo          VARCHAR(10),
    local_code      VARCHAR(20),
    status          TINYINT         NOT NULL DEFAULT 1,
    source_guide_id BIGINT          NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50)     NOT NULL,
    updated_at      DATETIME,
    updated_by      VARCHAR(50),
    deleted_at      DATETIME,
    deleted_by      VARCHAR(50)
);

-- 2. Migrar datos existentes de remission_guide a recipient
INSERT INTO recipient (doc_type, doc_number, name, address, ubigeo, local_code, source_guide_id, created_by)
SELECT
    recipient_doc_type,
    recipient_doc_number,
    recipient_name,
    destination_address,
    destination_ubigeo,
    destination_local_code,
    id,
    COALESCE(created_by, 'system')
FROM (
         SELECT *,
                ROW_NUMBER() OVER (PARTITION BY recipient_doc_number ORDER BY id) AS rn
         FROM remission_guide
     ) AS sub
WHERE rn = 1;

-- 3. Agregar columna recipient_id a remission_guide
ALTER TABLE remission_guide ADD COLUMN recipient_id BIGINT NULL;

-- 4. Actualizar recipient_id en remission_guide
UPDATE remission_guide rg
INNER JOIN recipient r ON r.source_guide_id = rg.id
SET rg.recipient_id = r.id
WHERE rg.deleted_at IS NULL;

-- 5. Eliminar columna temporal source_guide_id de recipient
ALTER TABLE recipient DROP COLUMN source_guide_id;

-- 6. Agregar FK
ALTER TABLE remission_guide
    ADD CONSTRAINT fk_rg_recipient
        FOREIGN KEY (recipient_id) REFERENCES recipient(id);

-- 7. Eliminar columnas antiguas de remission_guide
ALTER TABLE remission_guide
    DROP COLUMN recipient_doc_type,
    DROP COLUMN recipient_doc_number,
    DROP COLUMN recipient_name,
    DROP COLUMN recipient_address,
    DROP COLUMN destination_address,
    DROP COLUMN destination_ubigeo,
    DROP COLUMN destination_local_code;
