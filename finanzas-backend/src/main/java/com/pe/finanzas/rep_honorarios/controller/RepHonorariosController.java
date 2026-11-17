package com.pe.finanzas.rep_honorarios.controller;

import com.pe.finanzas.rep_honorarios.dto.RepHonorariosRequestDTO;
import com.pe.finanzas.rep_honorarios.dto.RepHonorariosResumenDTO;
import com.pe.finanzas.rep_honorarios.service.RepHonorariosService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/reportes/honorarios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RepHonorariosController {

    private final RepHonorariosService service;

    @GetMapping
    public ApiResponse<RepHonorariosResumenDTO> obtenerReporte(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            @RequestParam(required = false) String tipoPago,
            @RequestParam(required = false) String tipoBeneficiario,
            @RequestParam(required = false) String estadoPago,
            @RequestParam(required = false) String query,
            Pageable pageable) {

        RepHonorariosRequestDTO request = new RepHonorariosRequestDTO();
        request.setFechaInicio(fechaInicio);
        request.setFechaFin(fechaFin);
        request.setTipoPago(tipoPago);
        request.setTipoBeneficiario(tipoBeneficiario);
        request.setEstadoPago(estadoPago);
        request.setQuery(query);

        return ApiResponse.success("Reporte generado correctamente", service.generarReporte(request, pageable));
    }
}
