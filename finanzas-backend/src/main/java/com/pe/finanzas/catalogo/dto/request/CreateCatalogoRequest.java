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
public class CreateCatalogoRequest {

    @Size(max = 50, message = "El código no puede exceder los 50 caracteres")
    private String codigo;

    @NotBlank(message = "El nombre del servicio es obligatorio")
    @Size(max = 255, message = "El nombre del servicio no puede exceder los 255 caracteres")
    private String nombreServicio;

    @NotNull(message = "La unidad es obligatoria")
    @DecimalMin(value = "0.01", message = "La unidad debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal unidad;

    @NotNull(message = "El campo de honorarios médicos es obligatorio")
    private Boolean honorariosMedicos;

    @DecimalMin(value = "0.00", message = "La tarifa no puede ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal tarifa;

    @NotNull(message = "El precio sin IGV es obligatorio")
    @DecimalMin(value = "0.01", message = "El precio sin IGV debe ser mayor a 0")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precioSinIgv;

    @DecimalMin(value = "0.00", message = "La tarifa no puede ser negativa")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precioConIgv;

    private BigDecimal precioCompra;
    private BigDecimal margenGanancia;

    @NotNull(message = "El nivel es obligatorio")
    private Long idNivel;

    private Long idTipoAtencion;

    @NotNull(message = "El tipo de precio es obligatorio")
    private TipoPrecio tipoPrecio;

    @NotNull(message = "El estado es obligatorio")
    private Integer estado; // Cambiado a Integer para coincidir con la Entidad

    @NotNull(message = "El tipo es obligatorio")
    private Tipo tipo;

    @Size(max = 50, message = "El código SEUS no puede exceder los 50 caracteres")
    @Size(max = 50, message = "El código SEUS no puede exceder los 50 caracteres")
    private String codigoSeus;

    @Size(max = 50, message = "El código DIGEMID no puede exceder los 50 caracteres")
    private String codDigemid;

    @Builder.Default
    private String moneda = "PEN";

    @Size(max = 50, message = "El tipo de unidad no puede exceder los 50 caracteres")
    private String tipoUnidad;

    private Long idCategoria;
    private Long idTipoTemperatura;
    private String regSanitario;
}
