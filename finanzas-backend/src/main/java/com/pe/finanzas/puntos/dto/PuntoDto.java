package com.pe.finanzas.puntos.dto;

import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PuntoDto {

    private Long idPunto;
    private String punto;
    private String nombre;
    private String tipo;
    private String valido;

    // Información de la sucursal
    private Long idSucursal;
    private String nombreSucursal;

    // Campos adicionales relevantes
    private String imprimeComp;
    private String agrupaServ;
    private String buscaXIniciales;
    private String cargaMedico;
    private Integer cargaCuenta;
    private String cobra;
    private String ipAccesoModulo;
    private Integer modulo;
    private String arqueo;
    private String fe;

    // Documentos asignados
    private List<PuntosDocumentoDto> documentos;

    // Auditoría
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
