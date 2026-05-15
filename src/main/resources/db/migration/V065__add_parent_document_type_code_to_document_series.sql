ALTER TABLE document_series
    ADD COLUMN parent_document_type_code VARCHAR(5) NULL AFTER document_type_sunat_code;
