-- 1. Asegurar que la tabla existe (basado en la entidad TipoDocumento)
CREATE TABLE IF NOT EXISTS tipo_documento (
    id_tipo_documento BIGSERIAL PRIMARY KEY,
    codigo VARCHAR(20) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    requiere_ruc BOOLEAN NOT NULL DEFAULT FALSE,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. Seed de datos iniciales requeridos
INSERT INTO tipo_documento (codigo, nombre, descripcion, requiere_ruc)
VALUES 
('BOLETA', 'BOLETA DE VENTA', 'Comprobante para personas naturales', FALSE),
('FACTURA', 'FACTURA', 'Comprobante para personas jurídicas con RUC', TRUE),
('TICKET', 'TICKET', 'Ticket de venta interna', FALSE),
('NOTA_CREDITO', 'NOTA DE CRÉDITO', 'Anulación o devolución', FALSE),
('NOTA_DEBITO', 'NOTA DE DÉBITO', 'Ajuste de precio', FALSE)
ON CONFLICT (codigo) DO NOTHING;
