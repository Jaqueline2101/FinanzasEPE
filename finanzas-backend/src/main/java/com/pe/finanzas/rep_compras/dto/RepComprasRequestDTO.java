package com.pe.finanzas.rep_compras.dto;

import lombok.Data;
import java.time.LocalDate;

@Data
public class RepComprasRequestDTO {
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Long idSucursal;
    private Long idProveedor;
    private Integer estado;
}
