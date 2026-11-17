package com.pe.finanzas.catalogo.dto.response;

import com.pe.finanzas.catalogo.entity.Catalogo.TipoPrecio;
import com.pe.finanzas.niveles.dto.NivelDto;
import com.pe.finanzas.catalogo.entity.Catalogo.Tipo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CatalogoResponse {

    private Long id;
    private String codigo;
    private String nombreServicio;
    private BigDecimal unidad;
    private String tipoUnidad;
    private Boolean honorariosMedicos;
    private BigDecimal tarifa;
    private BigDecimal precioSinIgv;
    private BigDecimal precioConIgv;
    private BigDecimal precioCompra;
    private BigDecimal margenGanancia;

    // Nivel
    private NivelDto nivel;
    private String nivelDescripcion; // Ej: "Nivel 1"

    // Tipo Servicio
    // Tipo Atencion
    private Long idTipoAtencion;
    private String tipoAtencionNombre;

    // Tipo Precio
    private TipoPrecio tipoPrecio;
    private String tipoPrecioDescripcion; // Ej: "Precio Factor."

    // Estado
    private Integer estado;
    private String estadoDescripcion; // Ej: "Activo" / "Inactivo"

    // Tipo (Servicio o Paquete)
    private Tipo tipo;
    private String tipoDescripcion; // Ej: "Paquete"

    private String codigoSeus;
    private String codDigemid;
    private String regSanitario;
    private Long idCategoria;
    private Long idTipoTemperatura;
    private String tipoTemperaturaNombre;
    private java.math.BigDecimal tempMin;
    private java.math.BigDecimal tempMax;
    private String moneda;
    private BigDecimal precioConvenio; // Precio calculado con descuento
    private BigDecimal porcentajeDescuento; // Porcentaje de descuento aplicado
    private BigDecimal montoDescuentoFijo; // Monto de descuento fijo aplicado
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
