-- V029: Agregar client_address (texto) a remission_guide.
-- sale.client_address_id y sale.client_address ya incluidos en V002.

ALTER TABLE remission_guide
    ADD COLUMN client_address VARCHAR(500) NULL;

-- Poblar guías existentes tomando destination_address como dirección del cliente
UPDATE remission_guide
SET client_address = destination_address
WHERE deleted_at IS NULL
  AND destination_address IS NOT NULL;
