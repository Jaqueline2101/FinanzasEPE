-- Corregir nombre de tabla: la entidad MetodoPago mapea a 'metodos_pago'
-- pero V8 creó 'metodo_pago'. Creamos la tabla correcta y actualizamos FKs.

-- 1. Crear tabla correcta si no existe
CREATE TABLE IF NOT EXISTS metodos_pago (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL UNIQUE,
    estado INTEGER DEFAULT 1,
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_modificacion TIMESTAMP
);

-- 2. Migrar datos desde metodo_pago si existe
INSERT INTO metodos_pago (descripcion)
SELECT descripcion FROM metodo_pago
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE metodos_pago.descripcion = metodo_pago.descripcion);

-- 3. Insertar valores base si no se migró ninguno
INSERT INTO metodos_pago (descripcion)
SELECT d FROM (VALUES ('EFECTIVO'), ('YAPE'), ('PLIN'), ('TRANSFERENCIA_BCP'), ('TRANSFERENCIA_BBVA'), ('TRANSFERENCIA_INTERBANK'), ('TARJETA_CREDITO'), ('TARJETA_DEBITO')) AS v(d)
WHERE NOT EXISTS (SELECT 1 FROM metodos_pago WHERE metodos_pago.descripcion = v.d);

-- 4. Actualizar FKs de caja_chica_movimiento
ALTER TABLE caja_chica_movimiento DROP CONSTRAINT IF EXISTS fk_caja_chica_mov_metodo;
ALTER TABLE caja_chica_movimiento ADD CONSTRAINT fk_caja_chica_mov_metodo
    FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id);

-- 5. Actualizar FKs de caja_general_saldo
ALTER TABLE caja_general_saldo DROP CONSTRAINT IF EXISTS fk_caja_general_saldo_metodo;
ALTER TABLE caja_general_saldo ADD CONSTRAINT fk_caja_general_saldo_metodo
    FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id);

-- 6. Actualizar FKs de caja_general_movimiento
ALTER TABLE caja_general_movimiento DROP CONSTRAINT IF EXISTS fk_caja_general_mov_metodo;
ALTER TABLE caja_general_movimiento ADD CONSTRAINT fk_caja_general_mov_metodo
    FOREIGN KEY (id_metodo_pago) REFERENCES metodos_pago(id);
