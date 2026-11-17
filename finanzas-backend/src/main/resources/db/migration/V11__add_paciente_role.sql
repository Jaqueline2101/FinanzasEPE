INSERT INTO roles (name, description, active, alcance, acceso_id)
SELECT 'PACIENTE', 'Paciente', true, 'GLOBAL', id
FROM acceso_main
WHERE nombre ILIKE 'SISTEMA'
AND NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PACIENTE');

INSERT INTO roles (name, description, active, alcance, acceso_id)
SELECT 'USUARIO', 'Usuario general', true, 'GLOBAL', id
FROM acceso_main
WHERE nombre ILIKE 'SISTEMA'
AND NOT EXISTS (SELECT 1 FROM roles WHERE name = 'USUARIO');
