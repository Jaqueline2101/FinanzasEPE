package com.pe.finanzas.rep_ventas.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RepVentasResponseDTO {
    private Long idVenta;
    private LocalDate fecha;
    private String comprobante;
    private String tipoDoc;
    private String paciente;
    private String tipoAtencion;
    private String metodoPago;
    private BigDecimal total;
    private String estado;
    private Integer cajeroId;
    private String cajeroNombre; // Para un futuro join si se necesita, o enviar vacío
}
