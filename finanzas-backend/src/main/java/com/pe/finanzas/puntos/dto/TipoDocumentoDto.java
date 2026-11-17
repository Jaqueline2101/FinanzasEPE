package com.pe.finanzas.puntos.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TipoDocumentoDto {

    private Long idTipoDocumento;
    private String codigo;
    private String nombre;
    private String descripcion;
    private Boolean requiereRuc;
    private Boolean activo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
