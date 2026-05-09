-- V050: Backfill profile_menu for all is_system=1 profiles.
-- Ensures every menu (including menus created since V034/V042) is assigned
-- to SUPER_ADMIN and all per-company SYSTEMS profiles.
INSERT IGNORE INTO profile_menu (profile_id, menu_id)
SELECT p.id, m.id
FROM profile p
         CROSS JOIN menu m
WHERE p.is_system = 1;
