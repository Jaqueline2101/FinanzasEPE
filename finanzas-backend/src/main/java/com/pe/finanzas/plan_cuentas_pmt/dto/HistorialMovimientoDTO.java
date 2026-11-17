package com.pe.finanzas.plan_cuentas_pmt.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialMovimientoDTO {
    private LocalDateTime fecha;
    private String usuario;
    private String accion; // Ej: "Cambio de Cargo", "Ajuste Salarial", "Registro Inicial"
    private String detalle; // Ej: "De Enfermero a Jefe de Área"
    private String valorAnterior;
    private String valorNuevo;
}
