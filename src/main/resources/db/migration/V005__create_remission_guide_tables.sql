-- ============================================================
-- V005: Guías de Remisión
-- ============================================================

-- Tipo de documento SUNAT "09" ya insertado en V003 — noop
INSERT IGNORE INTO document_type_sunat (code, name, status, created_by)
VALUES ('09', 'Guía de Remisión Remitente', 1, 'system');

-- ============================================================
-- Tabla principal: guía de remisión
-- ============================================================
CREATE TABLE IF NOT EXISTS remission_guide (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id                  BIGINT          NOT NULL,

    -- Serie y numeración (reutiliza document_series tipo "09")
    document_series_id          BIGINT          NOT NULL,
    series                      VARCHAR(4)      NOT NULL,
    sequence                    VARCHAR(8)      NOT NULL,

    -- Fechas
    issue_date                  DATETIME        NOT NULL,
    transfer_date               DATE            NOT NULL,

    -- Motivo traslado (VENTA, COMPRA, TRASLADO_EMPRESA, OTROS, EXPORTACION, etc.)
    transfer_reason             VARCHAR(30)     NOT NULL,
    transfer_reason_description VARCHAR(255),

    -- Modalidad (TRANSPORTE_PUBLICO | TRANSPORTE_PRIVADO)
    transport_mode              VARCHAR(30)     NOT NULL,

    -- Peso y bultos
    gross_weight                DECIMAL(14, 3)  NOT NULL,
    weight_unit                 VARCHAR(10)     NOT NULL DEFAULT 'KGM',
    package_count               INT             NOT NULL DEFAULT 1,

    -- Punto de partida
    origin_address              VARCHAR(500)    NOT NULL,
    origin_ubigeo               VARCHAR(10)     NOT NULL,
    origin_local_code           VARCHAR(20),

    -- Punto de llegada
    destination_address         VARCHAR(500)    NOT NULL,
    destination_ubigeo          VARCHAR(10)     NOT NULL,
    destination_local_code      VARCHAR(20),

    -- Indicador de vehículo menor
    minor_vehicle_transfer      TINYINT(1)      NOT NULL DEFAULT 0,

    -- Destinatario
    recipient_doc_type          VARCHAR(5)      NOT NULL COMMENT 'DNI, RUC, etc.',
    recipient_doc_number        VARCHAR(20)     NOT NULL,
    recipient_name              VARCHAR(200)    NOT NULL,
    recipient_address           VARCHAR(500),

    -- Transportista (solo para TRANSPORTE_PUBLICO)
    carrier_doc_type            VARCHAR(5),
    carrier_doc_number          VARCHAR(20),
    carrier_name                VARCHAR(200),

    -- Observaciones
    observations                TEXT,

    -- Estado y respuesta SUNAT
    status                      VARCHAR(20)     NOT NULL DEFAULT 'PENDIENTE',
    sunat_response_code         INT,
    sunat_message               TEXT,
    hash_code                   VARCHAR(255),
    xml_base64                  TEXT,
    cdr_base64                  TEXT,
    xml_url                     VARCHAR(500),
    cdr_url                     VARCHAR(500),
    pdf_url                     VARCHAR(500),

    -- Auditoría
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(50)     NOT NULL,
    updated_at                  DATETIME,
    updated_by                  VARCHAR(50),
    deleted_at                  DATETIME,
    deleted_by                  VARCHAR(50),

    CONSTRAINT fk_rg_company        FOREIGN KEY (company_id)        REFERENCES company (id),
    CONSTRAINT fk_rg_document_series FOREIGN KEY (document_series_id) REFERENCES document_series (id),
    CONSTRAINT uk_rg_series_sequence UNIQUE (document_series_id, sequence)
);

-- ============================================================
-- Ítems de la guía (productos a trasladar)
-- ============================================================
CREATE TABLE IF NOT EXISTS remission_guide_item (
    id                  BIGINT          AUTO_INCREMENT PRIMARY KEY,
    remission_guide_id  BIGINT          NOT NULL,

    -- Referencia opcional al catálogo de productos
    product_id          BIGINT,

    description         VARCHAR(500)    NOT NULL,
    quantity            DECIMAL(14, 3)  NOT NULL,

    -- Unidad de medida SUNAT (ej: NIU, KGM, ZZ)
    unit_measure_sunat  VARCHAR(10)     NOT NULL DEFAULT 'NIU',

    -- Auditoría
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50)     NOT NULL,
    updated_at          DATETIME,
    updated_by          VARCHAR(50),
    deleted_at          DATETIME,
    deleted_by          VARCHAR(50),

    CONSTRAINT fk_rgi_guide   FOREIGN KEY (remission_guide_id) REFERENCES remission_guide (id),
    CONSTRAINT fk_rgi_product FOREIGN KEY (product_id)         REFERENCES product (id)
);

-- ============================================================
-- Conductores / vehículos (solo para TRANSPORTE_PRIVADO)
-- ============================================================
CREATE TABLE IF NOT EXISTS remission_guide_driver (
    id                      BIGINT          AUTO_INCREMENT PRIMARY KEY,
    remission_guide_id      BIGINT          NOT NULL,

    -- Conductor
    driver_doc_type         VARCHAR(5)      NOT NULL COMMENT 'DNI, etc.',
    driver_doc_number       VARCHAR(20)     NOT NULL,
    driver_first_name       VARCHAR(100)    NOT NULL,
    driver_last_name        VARCHAR(100)    NOT NULL,
    driver_license_number   VARCHAR(30)     NOT NULL,

    -- Vehículo
    vehicle_plate           VARCHAR(20)     NOT NULL,

    -- Auditoría
    created_at              DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by              VARCHAR(50)     NOT NULL,
    updated_at              DATETIME,
    updated_by              VARCHAR(50),
    deleted_at              DATETIME,
    deleted_by              VARCHAR(50),

    CONSTRAINT fk_rgd_guide FOREIGN KEY (remission_guide_id) REFERENCES remission_guide (id)
);
