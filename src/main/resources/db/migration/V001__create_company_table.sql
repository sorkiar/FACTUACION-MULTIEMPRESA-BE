-- ============================================================
-- V001: Tabla Company (empresa/tenant)
-- ============================================================
CREATE TABLE IF NOT EXISTS company (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    ruc           VARCHAR(11)  NOT NULL UNIQUE,
    business_name VARCHAR(200) NOT NULL,
    trade_name    VARCHAR(200),
    address       VARCHAR(500),
    ubigeo        VARCHAR(10),
    phone         VARCHAR(30),
    email         VARCHAR(150),
    website       VARCHAR(200),
    logo_url      VARCHAR(500),
    status        TINYINT      NOT NULL DEFAULT 1,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by    VARCHAR(50),
    updated_at    DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP,
    updated_by    VARCHAR(50),
    deleted_at    DATETIME     DEFAULT NULL,
    deleted_by    VARCHAR(50)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
