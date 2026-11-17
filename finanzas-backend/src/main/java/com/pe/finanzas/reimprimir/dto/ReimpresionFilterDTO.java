package com.pe.finanzas.reimprimir.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReimpresionFilterDTO {
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaDesde;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaHasta;
    private String tipoDocumento;
    private String serie;
    private String numero;
    private String numDocIdentidad;
    private String moduloOrigen;
    private String query;
}
