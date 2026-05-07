-- Actualizar paths de Perfiles y Usuarios
UPDATE menu SET path = '/security/profiles' WHERE path = '/profiles';
UPDATE menu SET path = '/security/users'    WHERE path = '/users';

-- Reordenar Perfiles y Usuarios para dar espacio al nuevo Menús (orden 1)
UPDATE menu SET sort_order = 2 WHERE path = '/security/profiles';
UPDATE menu SET sort_order = 3 WHERE path = '/security/users';

-- Agregar submenú Menús bajo Seguridad
INSERT INTO menu (name, path, parent_id, sort_order, status)
SELECT 'Menús', '/security/menus', id, 1, 1
FROM menu
WHERE name = 'Seguridad' AND parent_id IS NULL;

-- Asignar el nuevo menú al perfil Administrador y SUPER_ADMIN
INSERT INTO profile_menu (profile_id, menu_id)
SELECT pm.profile_id, m.id
FROM menu m
JOIN profile_menu pm ON pm.menu_id = (SELECT id FROM menu WHERE name = 'Seguridad' AND parent_id IS NULL)
WHERE m.path = '/security/menus'
GROUP BY pm.profile_id, m.id;
