-- ============================================================
-- V043: Add missing audit columns to profile table
--   profile was created in V002 with only created_at/created_by.
--   The entity maps updated_at, updated_by, deleted_at, deleted_by
--   but those columns were never added to the schema.
-- ============================================================

ALTER TABLE profile
    ADD COLUMN updated_at DATETIME     DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP AFTER created_by,
    ADD COLUMN updated_by VARCHAR(50)  NULL                                      AFTER updated_at,
    ADD COLUMN deleted_at DATETIME     DEFAULT NULL                              AFTER updated_by,
    ADD COLUMN deleted_by VARCHAR(50)  NULL                                      AFTER deleted_at;
