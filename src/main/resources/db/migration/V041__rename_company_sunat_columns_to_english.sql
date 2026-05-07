-- Rename Spanish-named SUNAT columns to English
ALTER TABLE company
    RENAME COLUMN sunat_codigo_establecimiento   TO sunat_establishment_code,
    RENAME COLUMN sunat_ley_amazonia             TO sunat_amazonia_law,
    RENAME COLUMN sunat_produccion               TO sunat_production,
    RENAME COLUMN sunat_certificado_llave_publica TO sunat_certificate_public_key,
    RENAME COLUMN sunat_certificado_llave_privada TO sunat_certificate_private_key,
    RENAME COLUMN sunat_usuario_secundario        TO sunat_secondary_user,
    RENAME COLUMN sunat_clave_usuario_secundario  TO sunat_secondary_user_password,
    RENAME COLUMN sunat_guia_id                   TO sunat_guide_id,
    RENAME COLUMN sunat_guia_clave                TO sunat_guide_password,
    RENAME COLUMN ubig_departamento               TO ubig_department,
    RENAME COLUMN ubig_provincia                  TO ubig_province,
    RENAME COLUMN ubig_distrito                   TO ubig_district,
    RENAME COLUMN sunat_cuenta_detraccion         TO sunat_detraction_account;
