-- 1. Hacer id_servicio opcional (ya que la tabla servicios está vacía y no se usa en el código actual)
ALTER TABLE cdi_atenciones_examenes ALTER COLUMN id_servicio DROP NOT NULL;

-- 2. Función para el trigger que auto-crea la atención en CDI
CREATE OR REPLACE FUNCTION fn_crear_atencion_cdi()
RETURNS TRIGGER AS $$
DECLARE
    v_num_doc VARCHAR;
    v_id_sucursal BIGINT;
    v_num_nivel VARCHAR;
BEGIN
    -- Obtener información de la venta y el nivel del artículo
    SELECT v.numdoc, v.id_sucursal, n.num_nivel 
    INTO v_num_doc, v_id_sucursal, v_num_nivel
    FROM venta_registro v
    JOIN catalogo c ON NEW.id_articulo = c.id
    JOIN niveles n ON c.id_nivel = n.id_nivel
    WHERE v.id_venta = NEW.id_venta;

    -- Solo insertar si es de la rama 322 (CDI)
    IF v_num_nivel LIKE '322%' THEN
        INSERT INTO cdi_atenciones_examenes (
            id_venta_detalle,
            id_catalogo,
            id_sucursal,
            fecha,
            estado,
            prioridad,
            num_orden,
            id_usuario_crea,
            fecha_creacion
        ) VALUES (
            NEW.id_movart,
            NEW.id_articulo,
            v_id_sucursal,
            CURRENT_DATE,
            'PENDIENTE',
            'NORMAL',
            CASE 
                WHEN LENGTH(v_num_doc) > 7 THEN RIGHT(v_num_doc, 7)
                ELSE v_num_doc
            END,
            'SISTEMA',
            CURRENT_TIMESTAMP
        );
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- 3. Crear el trigger en la tabla venta_detalle
DROP TRIGGER IF EXISTS trg_crear_atencion_cdi ON venta_detalle;
CREATE TRIGGER trg_crear_atencion_cdi
AFTER INSERT ON venta_detalle
FOR EACH ROW
EXECUTE FUNCTION fn_crear_atencion_cdi();
