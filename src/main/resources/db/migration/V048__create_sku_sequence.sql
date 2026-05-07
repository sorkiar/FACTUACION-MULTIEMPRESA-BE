-- V048: SKU sequence table per company
CREATE TABLE sku_sequence (
    id             BIGINT      AUTO_INCREMENT PRIMARY KEY,
    company_id     BIGINT      NOT NULL,
    type           VARCHAR(10) NOT NULL,
    last_seq_value INT         NOT NULL DEFAULT 0,
    CONSTRAINT uk_sku_seq_company_type UNIQUE (company_id, type),
    CONSTRAINT fk_sku_seq_company      FOREIGN KEY (company_id) REFERENCES company (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
