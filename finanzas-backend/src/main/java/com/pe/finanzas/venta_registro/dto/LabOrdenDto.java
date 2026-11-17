package com.pe.finanzas.venta_registro.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LabOrdenDto {
    private Long idOrden;
    private Long idPaciente; // ID del Paciente (Ctacte)
    private String numero;
    private LocalDateTime fecha;
    private String estado;
    private Integer numExamenes;
    private java.math.BigDecimal totalEstimado; // Total estimado de la orden
    private String pacienteNombre; // Nombre del paciente (para el Dashboard)
    private String pacienteDoc; // Documento del paciente
    private String medicoNombre;
    private String origen;
    private String prioridad;

    // Patient Type Flags for POS
    private Boolean pacienteEsFamiliar;
    private Long pacienteIdEmpresa;
    private Long pacienteIdCompania;

    private List<LabOrdenDetalleDto> detalles;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class LabOrdenDetalleDto {
        private Long idOrdenDet;
        private Long idExamen; // ID Servicio
        private String codigo; // Código del servicio (Necesario para el POS)
        private String nombre; // Nombre del servicio
        private String estado;
        private java.math.BigDecimal precio; // Precio base del servicio
    }
}
