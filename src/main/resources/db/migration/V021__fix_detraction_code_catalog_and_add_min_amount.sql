-- V021: Ajustes al catálogo 54 SUNAT.
-- ALTER TABLE omitido: min_amount ya incluido en detraction_code en V002.

-- ─── 1. Actualizar mínimo a 1/2 UIT para bienes del Anexo 1 ──────────────────
-- UIT 2026 = S/ 5,500 → 1/2 UIT = S/ 2,750
UPDATE detraction_code
SET min_amount = 2750.00
WHERE code IN ('001', '002', '012');

-- ─── 2. Corregir tasa de Movimiento de carga (4% → 10%, RS 071-2018/SUNAT) ────
UPDATE detraction_code
SET percentage = 10.00
WHERE code = '021';

-- ─── 3. Insertar códigos faltantes del catálogo vigente ──────────────────────
INSERT INTO detraction_code (code, description, percentage, min_amount, category, status) VALUES
('030', 'Contratos de construcción',             4.00, 700.00, 'SERVICIO', 0),
('039', 'Minerales no metálicos',               10.00, 700.00, 'BIEN',     0),
('040', 'Bien inmueble gravado con el IGV',       4.00, 700.00, 'BIEN',     0);
