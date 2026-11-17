package com.pe.finanzas.empleados.dto;

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
public class EmpleadoDTO {

    private Long id;
    private String nombre;
    private String apepat;
    private String apemat;
    private String numdoc;
    private String ruc;
    private String direcc;
    private String fono1;
    private String email;
    private LocalDate nacfec;
    private String sexo;
    private String estadoCivil;

    private Long idOcupacion;
    private String nombreOcupacion;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate fechaFin;
    private String centroLabor;
    private String modalidadTrabajo;
    private String tipoContrato;
    private Boolean renovacion;
    private java.math.BigDecimal sueldoBase;
    private Boolean estado;

    private Integer cantidadFamiliares;

    private java.util.List<String> roles;
}
