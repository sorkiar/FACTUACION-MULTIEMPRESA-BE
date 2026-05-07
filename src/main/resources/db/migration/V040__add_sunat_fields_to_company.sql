-- Add SUNAT configuration fields to company table
ALTER TABLE company
    ADD COLUMN sunat_codigo_establecimiento     VARCHAR(10)  NULL,
    ADD COLUMN sunat_ley_amazonia               TINYINT(1)   NOT NULL DEFAULT 0,
    ADD COLUMN sunat_produccion                 TINYINT(1)   NOT NULL DEFAULT 0,
    ADD COLUMN sunat_certificado_llave_publica  TEXT         NULL,
    ADD COLUMN sunat_certificado_llave_privada  TEXT         NULL,
    ADD COLUMN sunat_usuario_secundario         VARCHAR(50)  NULL,
    ADD COLUMN sunat_clave_usuario_secundario   VARCHAR(100) NULL,
    ADD COLUMN sunat_guia_id                    VARCHAR(50)  NULL,
    ADD COLUMN sunat_guia_clave                 VARCHAR(100) NULL,
    ADD COLUMN ubig_departamento                VARCHAR(100) NULL,
    ADD COLUMN ubig_provincia                   VARCHAR(100) NULL,
    ADD COLUMN ubig_distrito                    VARCHAR(100) NULL,
    ADD COLUMN sunat_cuenta_detraccion          VARCHAR(20)  NULL;
