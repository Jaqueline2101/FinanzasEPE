package com.pe.finanzas.rep_comprobantes_anulados.dto;

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
public class RepAnuladosResponseDTO {
    private Long idVenta;
    private LocalDate fecha;
    private String comprobante;
    private String tipoDoc;
    private String paciente;
    private String metodoPago;
    private BigDecimal total;
    private String motivoAnulacion;
    private String usuarioAnulacion; // Nombre del usuario que anuló
}
