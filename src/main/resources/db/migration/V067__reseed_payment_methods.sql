SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE payment_method;
SET FOREIGN_KEY_CHECKS = 1;

INSERT INTO payment_method (id, code, name, requires_proof, status, created_at, created_by) VALUES
(6,  'CASH',     'Efectivo',                   0, 1, '2026-02-12 23:23:35', 'system'),
(7,  'TRANSFER', 'Transferencia Bancaria',      1, 1, '2026-02-12 23:23:35', 'system'),
(8,  'CARD',     'Tarjeta de crédito / débito', 1, 1, '2026-02-12 23:23:35', 'system'),
(9,  'YAPE',     'Yape / Plin',                 1, 1, '2026-02-12 23:23:35', 'system'),
(10, 'MIXED',    'Mixto',                       0, 1, '2026-02-12 23:23:35', 'system');
