CREATE TABLE transfer_reason (
    id     BIGINT       NOT NULL AUTO_INCREMENT,
    code   VARCHAR(5)   NOT NULL,
    name   VARCHAR(150) NOT NULL,
    status TINYINT      NOT NULL DEFAULT 1,
    PRIMARY KEY (id),
    UNIQUE KEY uk_transfer_reason_code (code)
);

INSERT INTO transfer_reason (code, name, status) VALUES
    ('01', 'Venta', 1),
    ('02', 'Compra', 1),
    ('03', 'Venta con entrega a terceros', 0),
    ('04', 'Traslado entre establecimientos de la misma empresa', 1),
    ('05', 'Consignación', 0),
    ('06', 'Devolución', 1),
    ('07', 'Traslado entre establecimientos', 0),
    ('08', 'Importación', 0),
    ('09', 'Exportación', 0),
    ('13', 'Otros', 1),
    ('14', 'Venta sujeta a confirmación del comprador', 0),
    ('17', 'Traslado de bienes para transformación', 1),
    ('18', 'Traslado emisor itinerante de comprobantes de pago', 0),
    ('19', 'Traslado a zona primaria', 0);

ALTER TABLE remission_guide
    ADD COLUMN transfer_reason_id BIGINT NULL,
    ADD CONSTRAINT fk_rg_transfer_reason
        FOREIGN KEY (transfer_reason_id) REFERENCES transfer_reason (id);

UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '01') WHERE transfer_reason = 'VENTA';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '02') WHERE transfer_reason = 'COMPRA';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '04') WHERE transfer_reason = 'TRASLADO_EMPRESA';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '06') WHERE transfer_reason = 'DEVOLUCION';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '17') WHERE transfer_reason = 'TRASLADO_TRANSFORMACION';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '13') WHERE transfer_reason = 'OTROS';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '09') WHERE transfer_reason = 'EXPORTACION';
UPDATE remission_guide SET transfer_reason_id = (SELECT id FROM transfer_reason WHERE code = '18') WHERE transfer_reason = 'TRASLADO_ITINERANTE';

ALTER TABLE remission_guide DROP COLUMN transfer_reason;
