-- =============================================================
-- V026: Crear client_address, agregar client_id a remission_guide,
--       y eliminar tabla recipient (migración estructural).
-- Nota: las migraciones de datos (recipient → client) se omiten
-- porque en multi-empresa la base siempre arranca sin datos en recipient.
-- =============================================================

-- 1. Crear tabla client_address (IF NOT EXISTS: ya creada en V002)
CREATE TABLE IF NOT EXISTS client_address (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    client_id   BIGINT       NOT NULL,
    address     VARCHAR(500) NOT NULL,
    ubigeo      VARCHAR(10)  NULL,
    description VARCHAR(255) NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50)  NOT NULL DEFAULT 'system',
    updated_at  DATETIME     NULL,
    updated_by  VARCHAR(50)  NULL,
    deleted_at  DATETIME     NULL,
    deleted_by  VARCHAR(50)  NULL,
    CONSTRAINT fk_client_address_client FOREIGN KEY (client_id) REFERENCES client(id)
);

-- 2. Agregar columnas client_id y client_address_id a remission_guide
ALTER TABLE remission_guide
    ADD COLUMN client_id         BIGINT NULL,
    ADD COLUMN client_address_id BIGINT NULL;

-- 3. FK de remission_guide.client_id → client
ALTER TABLE remission_guide
    ADD CONSTRAINT fk_rg_client FOREIGN KEY (client_id) REFERENCES client(id);

-- 4. FK de remission_guide.client_address_id → client_address
ALTER TABLE remission_guide
    ADD CONSTRAINT fk_rg_client_address FOREIGN KEY (client_address_id) REFERENCES client_address(id);

-- 5. Eliminar FK y columna recipient_id (agregados en V012)
ALTER TABLE remission_guide DROP FOREIGN KEY fk_rg_recipient;
ALTER TABLE remission_guide DROP COLUMN recipient_id;

-- 6. Eliminar tabla recipient
DROP TABLE IF EXISTS recipient;
