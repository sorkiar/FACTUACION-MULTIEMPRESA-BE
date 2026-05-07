-- Add missing columns to document_type to match entity mapping
ALTER TABLE document_type
    ADD COLUMN person_type_id BIGINT        NULL AFTER id,
    ADD COLUMN length         INT           NULL AFTER name,
    ADD COLUMN description    VARCHAR(150)  NULL AFTER length,
    ADD COLUMN updated_at     DATETIME      DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER created_by,
    ADD COLUMN updated_by     VARCHAR(50)   NULL AFTER updated_at,
    ADD COLUMN deleted_at     DATETIME      DEFAULT NULL AFTER updated_by,
    ADD COLUMN deleted_by     VARCHAR(50)   NULL AFTER deleted_at,
    ADD CONSTRAINT fk_document_type_person_type FOREIGN KEY (person_type_id) REFERENCES person_type (id);
