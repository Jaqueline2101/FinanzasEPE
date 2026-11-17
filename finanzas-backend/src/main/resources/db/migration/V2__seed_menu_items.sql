INSERT INTO menu_items (id_acceso, titulo, icono, orden, tipo, tipo_control, activo)
SELECT id, 'CONFIGURACIONES', 'cog', 1, 'GRUPO', 'PUBLICO', true
FROM acceso_main WHERE nombre = 'Sistema'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'CONFIGURACIONES');

INSERT INTO menu_items (id_acceso, titulo, icono, orden, tipo, tipo_control, activo)
SELECT id, 'PROCESOS', 'cpu', 10, 'GRUPO', 'PUBLICO', true
FROM acceso_main WHERE nombre = 'Sistema'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'PROCESOS');

INSERT INTO menu_items (id_acceso, titulo, icono, orden, tipo, tipo_control, activo)
SELECT id, 'RR. HH.', 'users', 20, 'GRUPO', 'PUBLICO', true
FROM acceso_main WHERE nombre = 'Sistema'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'RR. HH.');

INSERT INTO menu_items (id_acceso, titulo, icono, orden, tipo, tipo_control, activo)
SELECT id, 'REPORTES', 'clipboard-plus', 80, 'GRUPO', 'PUBLICO', true
FROM acceso_main WHERE nombre = 'Sistema'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'REPORTES');

-- 2. Insertar sub-items básicos para evitar grupos vacíos
INSERT INTO menu_items (id_acceso, parent_id, titulo, ruta, icono, orden, tipo, tipo_control, modulo_requerido, activo)
SELECT m.id_acceso, m.id, 'Productos', '/admin/procesos/productos', 'package', 1, 'ITEM', 'MODULO', 'productos', true
FROM menu_items m WHERE m.titulo = 'PROCESOS'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'Productos' AND parent_id = m.id);

INSERT INTO menu_items (id_acceso, parent_id, titulo, ruta, icono, orden, tipo, tipo_control, modulo_requerido, activo)
SELECT m.id_acceso, m.id, 'Seguridad', '/admin/configuraciones/seguridad', 'shield', 1, 'ITEM', 'MODULO', 'usuarios', true
FROM menu_items m WHERE m.titulo = 'CONFIGURACIONES'
AND NOT EXISTS (SELECT 1 FROM menu_items WHERE titulo = 'Seguridad' AND parent_id = m.id);
