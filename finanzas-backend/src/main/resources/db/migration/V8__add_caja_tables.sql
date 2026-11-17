CREATE TABLE metodo_pago (
    id BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE caja_chica (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    saldo_actual DECIMAL(15, 5) DEFAULT 0.00000,
    id_sucursal BIGINT,
    estado VARCHAR(255) DEFAULT 'ABIERTA',
    usuario_creacion VARCHAR(255),
    saldo_inicial DECIMAL(15, 5) DEFAULT 0.00000,
    saldo_cierre_real DECIMAL(15, 5),
    id_punto_venta BIGINT,
    id_usuario_cajero VARCHAR(255),
    fecha_cierre TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE caja_chica_movimiento (
    id BIGSERIAL PRIMARY KEY,
    id_caja_chica BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    monto DECIMAL(15, 5) NOT NULL,
    descripcion TEXT,
    referencia VARCHAR(255),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_metodo_pago BIGINT NOT NULL,
    usuario VARCHAR(255) NOT NULL,
    CONSTRAINT fk_caja_chica_mov_caja FOREIGN KEY (id_caja_chica) REFERENCES caja_chica(id),
    CONSTRAINT fk_caja_chica_mov_metodo FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id)
);

CREATE TABLE caja_general (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    id_sucursal BIGINT NOT NULL,
    saldo_actual DECIMAL(15, 5) NOT NULL DEFAULT 0.00000,
    usuario_creacion VARCHAR(255),
    fecha_creacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE caja_general_saldo (
    id BIGSERIAL PRIMARY KEY,
    id_caja_general BIGINT NOT NULL,
    id_metodo_pago BIGINT NOT NULL,
    saldo_actual DECIMAL(15, 5) NOT NULL DEFAULT 0.00000,
    fecha_actualizacion TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_caja_general_saldo_caja FOREIGN KEY (id_caja_general) REFERENCES caja_general(id),
    CONSTRAINT fk_caja_general_saldo_metodo FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id)
);

CREATE TABLE caja_general_movimiento (
    id BIGSERIAL PRIMARY KEY,
    id_caja_general BIGINT NOT NULL,
    tipo VARCHAR(50) NOT NULL,
    monto DECIMAL(15, 5) NOT NULL,
    descripcion TEXT,
    referencia VARCHAR(100),
    fecha TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    id_metodo_pago BIGINT NOT NULL,
    usuario VARCHAR(50) NOT NULL,
    CONSTRAINT fk_caja_general_mov_caja FOREIGN KEY (id_caja_general) REFERENCES caja_general(id),
    CONSTRAINT fk_caja_general_mov_metodo FOREIGN KEY (id_metodo_pago) REFERENCES metodo_pago(id)
);

-- Inserción de métodos de pago base
INSERT INTO metodo_pago (descripcion) VALUES 
('EFECTIVO'), ('YAPE'), ('PLIN'), ('TRANSFERENCIA_BCP'), ('TRANSFERENCIA_BBVA'), ('TRANSFERENCIA_INTERBANK'), ('TARJETA_CREDITO'), ('TARJETA_DEBITO');
