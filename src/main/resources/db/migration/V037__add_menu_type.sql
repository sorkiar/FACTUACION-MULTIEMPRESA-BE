-- V037: Add menu_type to distinguish sidebar, navbar and internal routes
ALTER TABLE menu
    ADD COLUMN menu_type VARCHAR(20) NOT NULL DEFAULT 'SIDEBAR'
        COMMENT 'SIDEBAR=visible en sidebar, NAVBAR=visible en navbar, INTERNAL=solo permisos';

-- All existing menus are sidebar items
UPDATE menu SET menu_type = 'SIDEBAR';
