-- V045: person_type.description mapped in entity but missing from V002 schema
SET @s=(SELECT IF(COUNT(*)=0,'ALTER TABLE person_type ADD COLUMN description VARCHAR(150) NULL AFTER name','SELECT 1') FROM INFORMATION_SCHEMA.COLUMNS WHERE TABLE_SCHEMA=DATABASE() AND TABLE_NAME='person_type' AND COLUMN_NAME='description');
PREPARE p FROM @s; EXECUTE p; DEALLOCATE PREPARE p;
