package com.pe.finanzas.rep_descuentos.controller;

import com.pe.finanzas.shared.dto.ApiResponse;

import com.pe.finanzas.rep_descuentos.dto.RepDescuentoRequestDTO;
import com.pe.finanzas.rep_descuentos.dto.RepDescuentoResponseDTO;
import com.pe.finanzas.rep_descuentos.service.RepDescuentoService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/reportes/descuentos")
@RequiredArgsConstructor
public class RepDescuentoController {

    private final RepDescuentoService service;

    @GetMapping
    public ApiResponse<org.springframework.data.domain.Page<RepDescuentoResponseDTO>> obtenerReporte(
            RepDescuentoRequestDTO filter, 
            org.springframework.data.domain.Pageable pageable) {
        try {
            org.springframework.data.domain.Page<RepDescuentoResponseDTO> reportValue = service.generarReporte(filter, pageable);
            return ApiResponse.<org.springframework.data.domain.Page<RepDescuentoResponseDTO>>builder()
                    .success(true)
                    .message("Reporte generado con éxito")
                    .data(reportValue)
                    .build();
        } catch (Exception e) {
            return ApiResponse.<org.springframework.data.domain.Page<RepDescuentoResponseDTO>>builder()
                    .success(false)
                    .message("Error al generar el reporte: " + e.getMessage())
                    .build();
        }
    }
}
