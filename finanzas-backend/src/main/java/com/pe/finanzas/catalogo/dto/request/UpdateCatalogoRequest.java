package com.pe.finanzas.catalogo.dto.request;

import com.pe.finanzas.catalogo.entity.Catalogo.TipoPrecio;
import com.pe.finanzas.catalogo.entity.Catalogo.Tipo;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateCatalogoRequest {

    @Size(max = 50, message = "El código no puede exceder los 50 caracteres")
    private String codigo;

    @Size(max = 255, message = "El nombre del servicio no puede exceder los 255 caracteres")
    private String nombreServicio;

    @DecimalMin(value = "0.01", message = "La unidad debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal unidad;

    private Boolean honorariosMedicos;

    @DecimalMin(value = "0.00", message = "La tarifa no puede ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal tarifa;

    @DecimalMin(value = "0.01", message = "El precio sin IGV debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precioSinIgv;

    @DecimalMin(value = "0.00", message = "El precio con IGV no puede ser negativo")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precioConIgv;
    private BigDecimal precioCompra;
    private BigDecimal margenGanancia;

    private Long idNivel;

    private Long idTipoAtencion;

    private TipoPrecio tipoPrecio;

    private Integer estado;

    private Tipo tipo;

    @Size(max = 50, message = "El código SEUS no puede exceder los 50 caracteres")
    @Size(max = 50, message = "El código SEUS no puede exceder los 50 caracteres")
    private String codigoSeus;

    @Size(max = 50, message = "El código DIGEMID no puede exceder los 50 caracteres")
    private String codDigemid;

    private String moneda;

    @Size(max = 50, message = "El tipo de unidad no puede exceder los 50 caracteres")
    private String tipoUnidad;

    private Long idCategoria;
    private Long idTipoTemperatura;
    private String regSanitario;
}
