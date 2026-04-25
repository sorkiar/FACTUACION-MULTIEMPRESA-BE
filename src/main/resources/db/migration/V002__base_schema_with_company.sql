-- ============================================================
-- V002: Schema base completo con company_id en tablas tenant-scoped
-- ============================================================

-- ── Catálogos GLOBALES (sin company_id) ─────────────────────

CREATE TABLE IF NOT EXISTS profile (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(30)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document_type (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS person_type (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS unit_measure (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS charge_unit (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(10)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS payment_method (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    code       VARCHAR(20)  NOT NULL UNIQUE,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document_type_sunat (
    code       VARCHAR(5)   NOT NULL PRIMARY KEY,
    name       VARCHAR(100) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS detraction_code (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    code        VARCHAR(3)    NOT NULL UNIQUE,
    description VARCHAR(500)  NOT NULL,
    percentage  DECIMAL(5,2)  NOT NULL,
    min_amount  DECIMAL(14,2) NOT NULL DEFAULT 700.00,
    category    VARCHAR(10)   NOT NULL DEFAULT 'SERVICIO',
    status      TINYINT(1)    NOT NULL DEFAULT 0,
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ubigeo (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    code         VARCHAR(10)  NOT NULL UNIQUE,
    department   VARCHAR(100) NOT NULL,
    province     VARCHAR(100) NOT NULL,
    district     VARCHAR(100) NOT NULL,
    created_at   DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS dni_record (
    doc_number             VARCHAR(8)   NOT NULL,
    full_name              VARCHAR(200),
    first_name             VARCHAR(100),
    last_name_paternal     VARCHAR(100),
    last_name_maternal     VARCHAR(100),
    verification_code      VARCHAR(10),
    address                VARCHAR(500),
    full_address           VARCHAR(500),
    ubigeo_reniec          VARCHAR(10),
    ubigeo_sunat           VARCHAR(10),
    ubigeo_dept            VARCHAR(10),
    ubigeo_prov            VARCHAR(10),
    ubigeo_dist            VARCHAR(10),
    created_at             DATETIME,
    created_by             VARCHAR(50)  NOT NULL DEFAULT 'system',
    updated_at             DATETIME,
    updated_by             VARCHAR(50),
    PRIMARY KEY (doc_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ruc_record (
    ruc                        VARCHAR(11)  NOT NULL,
    name                       VARCHAR(300),
    state                      VARCHAR(50),
    condition_record           VARCHAR(50),
    address                    VARCHAR(500),
    full_address               VARCHAR(500),
    department                 VARCHAR(100),
    province                   VARCHAR(100),
    district                   VARCHAR(100),
    ubigeo_sunat               VARCHAR(10),
    ubigeo_dept                VARCHAR(10),
    ubigeo_prov                VARCHAR(10),
    ubigeo_dist                VARCHAR(10),
    is_retention_agent         TINYINT(1)   NOT NULL DEFAULT 0,
    is_perception_agent        TINYINT(1)   NOT NULL DEFAULT 0,
    is_perception_fuel_agent   TINYINT(1)   NOT NULL DEFAULT 0,
    is_good_taxpayer           TINYINT(1)   NOT NULL DEFAULT 0,
    created_at                 DATETIME,
    created_by                 VARCHAR(50)  NOT NULL DEFAULT 'system',
    updated_at                 DATETIME,
    updated_by                 VARCHAR(50),
    PRIMARY KEY (ruc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ── Tablas TENANT-SCOPED (con company_id) ────────────────────

CREATE TABLE IF NOT EXISTS `user` (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id       BIGINT,
    document_type_id BIGINT       NOT NULL,
    profile_id       BIGINT       NOT NULL,
    document_number  VARCHAR(20)  NOT NULL,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    username         VARCHAR(100),
    password         VARCHAR(255),
    plain_password   VARCHAR(50),
    status           TINYINT      NOT NULL DEFAULT 1,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_at       DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50),
    deleted_at       DATETIME     DEFAULT NULL,
    deleted_by       VARCHAR(50),
    CONSTRAINT fk_user_company      FOREIGN KEY (company_id)       REFERENCES company (id),
    CONSTRAINT fk_user_doc_type     FOREIGN KEY (document_type_id) REFERENCES document_type (id),
    CONSTRAINT fk_user_profile      FOREIGN KEY (profile_id)       REFERENCES profile (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS client (
    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id       BIGINT       NOT NULL,
    person_type_id   BIGINT       NOT NULL,
    document_type_id BIGINT       NOT NULL,
    document_number  VARCHAR(20)  NOT NULL,
    first_name       VARCHAR(100),
    last_name        VARCHAR(100),
    birth_date       DATE,
    business_name    VARCHAR(200),
    contact_person_name VARCHAR(100),
    country_code_1   VARCHAR(10),
    phone_1          VARCHAR(25),
    country_code_2   VARCHAR(10),
    phone_2          VARCHAR(25),
    email_1          VARCHAR(150),
    email_2          VARCHAR(150),
    retention_agent  TINYINT(1)   NOT NULL DEFAULT 0,
    status           TINYINT      NOT NULL DEFAULT 1,
    created_at       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by       VARCHAR(50),
    updated_at       DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by       VARCHAR(50),
    deleted_at       DATETIME     DEFAULT NULL,
    deleted_by       VARCHAR(50),
    CONSTRAINT fk_client_company     FOREIGN KEY (company_id)       REFERENCES company (id),
    CONSTRAINT fk_client_person_type FOREIGN KEY (person_type_id)   REFERENCES person_type (id),
    CONSTRAINT fk_client_doc_type    FOREIGN KEY (document_type_id) REFERENCES document_type (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50),
    updated_at  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50),
    deleted_at  DATETIME     DEFAULT NULL,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_category_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS service_category (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id  BIGINT       NOT NULL,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(200),
    status      TINYINT      NOT NULL DEFAULT 1,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by  VARCHAR(50),
    updated_at  DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by  VARCHAR(50),
    deleted_at  DATETIME     DEFAULT NULL,
    deleted_by  VARCHAR(50),
    CONSTRAINT fk_service_category_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id          BIGINT       NOT NULL,
    sku                 VARCHAR(50)  NOT NULL,
    name                VARCHAR(200) NOT NULL,
    category_id         BIGINT       NOT NULL,
    unit_measure_id     BIGINT       NOT NULL,
    detraction_code_id  BIGINT,
    sale_price_pen      DECIMAL(12,2),
    estimated_cost_pen  DECIMAL(12,2),
    sale_price_usd      DECIMAL(12,2),
    estimated_cost_usd  DECIMAL(12,2),
    brand               VARCHAR(100),
    model               VARCHAR(100),
    short_description   VARCHAR(500) NOT NULL,
    technical_spec      TEXT         NOT NULL,
    main_image_url      VARCHAR(255) NOT NULL,
    technical_sheet_url VARCHAR(255),
    status              TINYINT      NOT NULL DEFAULT 1,
    created_at          DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50)  NOT NULL,
    updated_at          DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    deleted_at          DATETIME     DEFAULT NULL,
    deleted_by          VARCHAR(50),
    CONSTRAINT uk_product_sku_company     UNIQUE (company_id, sku),
    CONSTRAINT fk_product_company         FOREIGN KEY (company_id)         REFERENCES company (id),
    CONSTRAINT fk_product_category        FOREIGN KEY (category_id)        REFERENCES category (id),
    CONSTRAINT fk_product_unit_measure    FOREIGN KEY (unit_measure_id)     REFERENCES unit_measure (id),
    CONSTRAINT fk_product_detraction_code FOREIGN KEY (detraction_code_id)  REFERENCES detraction_code (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS service (
    id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id            BIGINT       NOT NULL,
    sku                   VARCHAR(50)  NOT NULL,
    name                  VARCHAR(200) NOT NULL,
    service_category_id   BIGINT       NOT NULL,
    charge_unit_id        BIGINT       NOT NULL,
    detraction_code_id    BIGINT,
    price_pen             DECIMAL(12,2),
    price_usd             DECIMAL(12,2),
    estimated_time        VARCHAR(50),
    expected_delivery     VARCHAR(200),
    requires_materials    TINYINT(1)   NOT NULL DEFAULT 0,
    requires_specification TINYINT(1)  NOT NULL DEFAULT 0,
    includes_description  TEXT,
    excludes_description  TEXT,
    conditions            TEXT,
    short_description     TEXT         NOT NULL,
    detailed_description  TEXT         NOT NULL,
    image_url             VARCHAR(255),
    technical_sheet_url   VARCHAR(255),
    status                TINYINT      NOT NULL DEFAULT 1,
    created_at            DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by            VARCHAR(50)  NOT NULL,
    updated_at            DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by            VARCHAR(50),
    deleted_at            DATETIME     DEFAULT NULL,
    deleted_by            VARCHAR(50),
    CONSTRAINT uk_service_sku_company      UNIQUE (company_id, sku),
    CONSTRAINT fk_service_company          FOREIGN KEY (company_id)          REFERENCES company (id),
    CONSTRAINT fk_service_service_category FOREIGN KEY (service_category_id) REFERENCES service_category (id),
    CONSTRAINT fk_service_charge_unit      FOREIGN KEY (charge_unit_id)       REFERENCES charge_unit (id),
    CONSTRAINT fk_service_detraction_code  FOREIGN KEY (detraction_code_id)   REFERENCES detraction_code (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document_series (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id               BIGINT       NOT NULL,
    document_type_sunat_code VARCHAR(5)   NOT NULL,
    series                   VARCHAR(4)   NOT NULL,
    current_sequence         INT          NOT NULL DEFAULT 0,
    status                   TINYINT      NOT NULL DEFAULT 1,
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50)  NOT NULL,
    updated_at               DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(50),
    deleted_at               DATETIME     DEFAULT NULL,
    deleted_by               VARCHAR(50),
    CONSTRAINT uk_doc_type_series_company UNIQUE (company_id, document_type_sunat_code, series),
    CONSTRAINT fk_doc_series_company      FOREIGN KEY (company_id)               REFERENCES company (id),
    CONSTRAINT fk_doc_series_sunat_type   FOREIGN KEY (document_type_sunat_code) REFERENCES document_type_sunat (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS client_address (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
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
    CONSTRAINT fk_client_address_client FOREIGN KEY (client_id) REFERENCES client (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id          BIGINT          NOT NULL,
    client_id           BIGINT,
    client_address_id   BIGINT,
    client_address      VARCHAR(500),
    sale_status         VARCHAR(20)     NOT NULL,
    subtotal_amount     DECIMAL(14,2)   NOT NULL DEFAULT 0,
    discount_amount     DECIMAL(14,2)   NOT NULL DEFAULT 0,
    tax_amount          DECIMAL(14,2)   NOT NULL DEFAULT 0,
    total_amount        DECIMAL(14,2)   NOT NULL DEFAULT 0,
    currency_code       VARCHAR(4)      NOT NULL,
    tax_percentage      DECIMAL(5,2)    NOT NULL,
    sale_date           DATETIME        NOT NULL,
    payment_type        VARCHAR(10)     NOT NULL DEFAULT 'CONTADO',
    purchase_order      VARCHAR(50),
    observations        TEXT,
    has_retention       TINYINT(1)      NOT NULL DEFAULT 0,
    retention_amount    DECIMAL(14,2),
    retention_rate      DECIMAL(5,2),
    has_detraction      TINYINT(1)      NOT NULL DEFAULT 0,
    detraction_code     VARCHAR(3),
    detraction_rate     DECIMAL(5,2),
    detraction_amount   DECIMAL(14,2),
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50)     NOT NULL,
    updated_at          DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by          VARCHAR(50),
    deleted_at          DATETIME        DEFAULT NULL,
    deleted_by          VARCHAR(50),
    CONSTRAINT fk_sale_company         FOREIGN KEY (company_id)       REFERENCES company (id),
    CONSTRAINT fk_sale_client          FOREIGN KEY (client_id)         REFERENCES client (id),
    CONSTRAINT fk_sale_client_address  FOREIGN KEY (client_address_id) REFERENCES client_address (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_item (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id         BIGINT          NOT NULL,
    product_id      BIGINT,
    service_id      BIGINT,
    item_type       VARCHAR(10)     NOT NULL DEFAULT 'PRODUCT',
    description     VARCHAR(500)    NOT NULL,
    quantity        DECIMAL(14,3)   NOT NULL,
    unit_price_pen  DECIMAL(14,4)   NOT NULL,
    unit_price_usd  DECIMAL(14,4),
    discount_amount DECIMAL(14,2)   NOT NULL DEFAULT 0,
    tax_amount      DECIMAL(14,2)   NOT NULL DEFAULT 0,
    total_amount    DECIMAL(14,2)   NOT NULL,
    unit_measure_sunat VARCHAR(10)  NOT NULL DEFAULT 'NIU',
    detraction_code_id BIGINT,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50)     NOT NULL,
    CONSTRAINT fk_sale_item_sale    FOREIGN KEY (sale_id)    REFERENCES sale (id),
    CONSTRAINT fk_sale_item_product FOREIGN KEY (product_id) REFERENCES product (id),
    CONSTRAINT fk_sale_item_service FOREIGN KEY (service_id) REFERENCES service (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_payment (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id         BIGINT          NOT NULL,
    payment_method_id BIGINT,
    amount          DECIMAL(14,2)   NOT NULL,
    payment_date    DATETIME        NOT NULL,
    reference       VARCHAR(100),
    notes           VARCHAR(500),
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50)     NOT NULL,
    CONSTRAINT fk_sale_payment_sale   FOREIGN KEY (sale_id)           REFERENCES sale (id),
    CONSTRAINT fk_sale_payment_method FOREIGN KEY (payment_method_id) REFERENCES payment_method (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_installment (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id             BIGINT          NOT NULL,
    installment_number  INT             NOT NULL,
    due_date            DATE            NOT NULL,
    amount              DECIMAL(14,2)   NOT NULL,
    paid                TINYINT(1)      NOT NULL DEFAULT 0,
    paid_at             DATETIME,
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50)     NOT NULL,
    CONSTRAINT fk_sale_installment_sale FOREIGN KEY (sale_id) REFERENCES sale (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sale_related_guide (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id         BIGINT          NOT NULL,
    guide_type      VARCHAR(10)     NOT NULL,
    series          VARCHAR(4)      NOT NULL,
    sequence        VARCHAR(8)      NOT NULL,
    created_at      DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50)     NOT NULL,
    CONSTRAINT fk_sale_related_guide_sale FOREIGN KEY (sale_id) REFERENCES sale (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS document (
    id                       BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id               BIGINT       NOT NULL,
    sale_id                  BIGINT       NOT NULL,
    document_type_sunat_code VARCHAR(5)   NOT NULL,
    document_series_id       BIGINT       NOT NULL,
    series                   VARCHAR(4)   NOT NULL,
    sequence                 VARCHAR(8)   NOT NULL,
    issue_date               DATETIME     NOT NULL,
    status                   VARCHAR(20),
    sunat_response_code      INT,
    sunat_message            TEXT,
    hash_code                VARCHAR(255),
    qr_code                  VARCHAR(500),
    xml_base64               TEXT,
    cdr_base64               TEXT,
    pdf_url                  VARCHAR(500),
    xml_url                  VARCHAR(500),
    cdr_url                  VARCHAR(500),
    created_at               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by               VARCHAR(50)  NOT NULL,
    updated_at               DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by               VARCHAR(50),
    deleted_at               DATETIME     DEFAULT NULL,
    deleted_by               VARCHAR(50),
    CONSTRAINT uk_document_unique      UNIQUE (document_series_id, sequence),
    CONSTRAINT fk_document_company     FOREIGN KEY (company_id)               REFERENCES company (id),
    CONSTRAINT fk_document_sale        FOREIGN KEY (sale_id)                  REFERENCES sale (id),
    CONSTRAINT fk_document_sunat_type  FOREIGN KEY (document_type_sunat_code) REFERENCES document_type_sunat (code),
    CONSTRAINT fk_document_series      FOREIGN KEY (document_series_id)       REFERENCES document_series (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS credit_debit_note_type (
    code       VARCHAR(5)   NOT NULL PRIMARY KEY,
    type       VARCHAR(10)  NOT NULL COMMENT 'CREDITO | DEBITO',
    name       VARCHAR(200) NOT NULL,
    status     TINYINT      NOT NULL DEFAULT 1,
    created_at DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS credit_debit_note (
    id                          BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id                  BIGINT          NOT NULL,
    sale_id                     BIGINT          NOT NULL,
    original_document_id        BIGINT          NOT NULL,
    document_type_sunat_code    VARCHAR(5)      NOT NULL,
    document_series_id          BIGINT          NOT NULL,
    series                      VARCHAR(4)      NOT NULL,
    sequence                    VARCHAR(8)      NOT NULL,
    issue_date                  DATETIME        NOT NULL,
    credit_debit_note_type_code VARCHAR(5)      NOT NULL,
    reason                      VARCHAR(500),
    subtotal_amount             DECIMAL(14,2)   NOT NULL DEFAULT 0,
    tax_amount                  DECIMAL(14,2)   NOT NULL DEFAULT 0,
    total_amount                DECIMAL(14,2)   NOT NULL DEFAULT 0,
    tax_percentage              DECIMAL(5,2)    NOT NULL DEFAULT 18,
    currency_code               VARCHAR(4)      NOT NULL DEFAULT 'PEN',
    status                      VARCHAR(20),
    sunat_response_code         INT,
    sunat_message               TEXT,
    hash_code                   VARCHAR(255),
    qr_code                     VARCHAR(500),
    xml_base64                  TEXT,
    cdr_base64                  TEXT,
    pdf_url                     VARCHAR(500),
    xml_url                     VARCHAR(500),
    cdr_url                     VARCHAR(500),
    created_at                  DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by                  VARCHAR(50)     NOT NULL,
    updated_at                  DATETIME        DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by                  VARCHAR(50),
    deleted_at                  DATETIME        DEFAULT NULL,
    deleted_by                  VARCHAR(50),
    CONSTRAINT fk_cdn_company     FOREIGN KEY (company_id)                  REFERENCES company (id),
    CONSTRAINT fk_cdn_sale        FOREIGN KEY (sale_id)                     REFERENCES sale (id),
    CONSTRAINT fk_cdn_orig_doc    FOREIGN KEY (original_document_id)        REFERENCES document (id),
    CONSTRAINT fk_cdn_sunat_type  FOREIGN KEY (document_type_sunat_code)    REFERENCES document_type_sunat (code),
    CONSTRAINT fk_cdn_series      FOREIGN KEY (document_series_id)          REFERENCES document_series (id),
    CONSTRAINT fk_cdn_type        FOREIGN KEY (credit_debit_note_type_code) REFERENCES credit_debit_note_type (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS credit_debit_note_item (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    credit_debit_note_id BIGINT         NOT NULL,
    description         VARCHAR(500)    NOT NULL,
    quantity            DECIMAL(14,3)   NOT NULL,
    unit_price          DECIMAL(14,4)   NOT NULL,
    total_amount        DECIMAL(14,2)   NOT NULL,
    unit_measure_sunat  VARCHAR(10)     NOT NULL DEFAULT 'NIU',
    created_at          DATETIME        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          VARCHAR(50)     NOT NULL,
    CONSTRAINT fk_cdni_note FOREIGN KEY (credit_debit_note_id) REFERENCES credit_debit_note (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS configuration (
    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id      BIGINT       NOT NULL,
    config_group    VARCHAR(100),
    config_key      VARCHAR(100),
    config_value    LONGTEXT,
    config_datatype VARCHAR(20),
    description     VARCHAR(255),
    editable        INT          NOT NULL DEFAULT 0,
    sort_order      INT          NOT NULL DEFAULT 0,
    col_span        INT          NOT NULL DEFAULT 2,
    created_at      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by      VARCHAR(50)  NOT NULL,
    updated_at      DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by      VARCHAR(50),
    deleted_at      DATETIME     DEFAULT NULL,
    deleted_by      VARCHAR(50),
    CONSTRAINT uk_config_group_key_company UNIQUE (company_id, config_group, config_key),
    CONSTRAINT fk_configuration_company    FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS exchange_rate (
    id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id BIGINT        NOT NULL,
    rate_date  DATE          NOT NULL,
    value      DECIMAL(10,3) NOT NULL,
    type       VARCHAR(1)    NOT NULL COMMENT 'C=Compra, V=Venta',
    created_at DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_exchange_rate_date_type_company UNIQUE (company_id, rate_date, type),
    CONSTRAINT fk_exchange_rate_company           FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS sunat_request_log (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    company_id    BIGINT       NOT NULL,
    document_type VARCHAR(5)   NOT NULL,
    series        VARCHAR(10)  NOT NULL,
    sequence      VARCHAR(10)  NOT NULL,
    triggered_by  VARCHAR(50)  NOT NULL,
    sent_at       DATETIME     NOT NULL,
    request_json  TEXT         NOT NULL,
    response_json TEXT,
    http_status   INT,
    success       TINYINT(1)   NOT NULL DEFAULT 0,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_sunat_log_company FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
