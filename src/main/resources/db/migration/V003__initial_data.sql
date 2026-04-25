-- ============================================================
-- V003: Datos iniciales (catálogos globales)
-- ============================================================

-- Perfiles
INSERT IGNORE INTO profile (code, name, status, created_by) VALUES
('SUPER_ADMIN', 'Super Administrador', 1, 'system'),
('ADMIN',       'Administrador',       1, 'system'),
('MANAGER',     'Gerente',             1, 'system'),
('USER',        'Usuario',             1, 'system');

-- Tipos de documento de identidad
INSERT IGNORE INTO document_type (code, name, status, created_by) VALUES
('DNI',   'DNI',                      1, 'system'),
('RUC',   'RUC',                      1, 'system'),
('CE',    'Carnet de Extranjería',     1, 'system'),
('PASS',  'Pasaporte',                 1, 'system');

-- Tipos de persona
INSERT IGNORE INTO person_type (code, name, status, created_by) VALUES
('NAT', 'Persona Natural',  1, 'system'),
('JUR', 'Persona Jurídica', 1, 'system');

-- Unidades de medida SUNAT (más comunes)
INSERT IGNORE INTO unit_measure (code, name, status, created_by) VALUES
('NIU',  'Unidad (bienes)', 1, 'system'),
('KGM',  'Kilogramo',       1, 'system'),
('MTR',  'Metro',           1, 'system'),
('LTR',  'Litro',           1, 'system'),
('ZZ',   'Unidad (bienes no especificados)', 1, 'system'),
('GLL',  'Galón',           1, 'system'),
('BX',   'Caja',            1, 'system'),
('PK',   'Paquete',         1, 'system');

-- Unidades de cobro para servicios
INSERT IGNORE INTO charge_unit (code, name, status, created_by) VALUES
('UNI',  'Unidad',         1, 'system'),
('HR',   'Hora',           1, 'system'),
('DIA',  'Día',            1, 'system'),
('MES',  'Mes',            1, 'system'),
('PROJ', 'Proyecto',       1, 'system');

-- Métodos de pago
INSERT IGNORE INTO payment_method (code, name, status, created_by) VALUES
('EFECTIVO',       'Efectivo',           1, 'system'),
('TRANSFERENCIA',  'Transferencia',      1, 'system'),
('YAPE',           'Yape',               1, 'system'),
('PLIN',           'Plin',               1, 'system'),
('TARJETA_DEBITO', 'Tarjeta de Débito',  1, 'system'),
('TARJETA_CREDITO','Tarjeta de Crédito', 1, 'system'),
('CHEQUE',         'Cheque',             1, 'system'),
('DEPOSITO',       'Depósito Bancario',  1, 'system');

-- Tipos de documento SUNAT
INSERT IGNORE INTO document_type_sunat (code, name, status, created_by) VALUES
('01', 'Factura',                         1, 'system'),
('03', 'Boleta de Venta',                 1, 'system'),
('07', 'Nota de Crédito',                 1, 'system'),
('08', 'Nota de Débito',                  1, 'system'),
('09', 'Guía de Remisión Remitente',      1, 'system');

-- Tipos de nota de crédito/débito
INSERT IGNORE INTO credit_debit_note_type (code, type, name, status, created_by) VALUES
('01', 'CREDITO', 'Anulación de la operación',                           1, 'system'),
('02', 'CREDITO', 'Anulación por error en el RUC',                       1, 'system'),
('03', 'CREDITO', 'Corrección por error en la descripción',              1, 'system'),
('04', 'CREDITO', 'Descuento global',                                    1, 'system'),
('05', 'CREDITO', 'Descuento por ítem',                                  1, 'system'),
('06', 'CREDITO', 'Devolución total',                                    1, 'system'),
('07', 'CREDITO', 'Devolución por ítem',                                 1, 'system'),
('08', 'CREDITO', 'Bonificación',                                        1, 'system'),
('09', 'CREDITO', 'Disminución en el valor',                             1, 'system'),
('10', 'CREDITO', 'Otros conceptos',                                     1, 'system'),
('11', 'CREDITO', 'Ajustes de operaciones de exportación',               1, 'system'),
('12', 'CREDITO', 'Ajuste por onificaciones',                            1, 'system'),
('13', 'CREDITO', 'Corrección del IGV',                                  1, 'system'),
('01', 'DEBITO',  'Intereses por mora',                                  1, 'system'),
('02', 'DEBITO',  'Aumento en el valor',                                 1, 'system'),
('03', 'DEBITO',  'Penalidades/Otros conceptos',                         1, 'system');
