ALTER TABLE sale_related_guide
    ADD COLUMN guide_number VARCHAR(50) NULL AFTER sale_id;

UPDATE sale_related_guide
    SET guide_number = CONCAT(series, '-', LPAD(sequence, 8, '0'))
    WHERE guide_number IS NULL AND series IS NOT NULL AND sequence IS NOT NULL;

UPDATE sale_related_guide
    SET guide_number = 'UNKNOWN'
    WHERE guide_number IS NULL;

ALTER TABLE sale_related_guide
    MODIFY COLUMN guide_number VARCHAR(50) NOT NULL,
    DROP COLUMN guide_type,
    DROP COLUMN series,
    DROP COLUMN sequence,
    DROP COLUMN created_at,
    DROP COLUMN created_by;
