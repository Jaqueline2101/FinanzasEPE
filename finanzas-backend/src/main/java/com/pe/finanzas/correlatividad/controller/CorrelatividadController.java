package com.pe.finanzas.correlatividad.controller;

import com.pe.finanzas.correlatividad.dto.CorrelatividadDetalleRequestDTO;
import com.pe.finanzas.correlatividad.dto.CorrelatividadRequestDTO;
import com.pe.finanzas.correlatividad.dto.CorrelatividadResponseDTO;
import com.pe.finanzas.correlatividad.service.CorrelatividadService;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.venta_registro.dto.VentaRegistroDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/reportes/correlatividad")
@RequiredArgsConstructor
public class CorrelatividadController {

    private final CorrelatividadService correlatividadService;

    @PostMapping("/resumen")
    public ResponseEntity<ApiResponse<CorrelatividadResponseDTO>> obtenerResumen(
            @RequestBody CorrelatividadRequestDTO request) {
        CorrelatividadResponseDTO resumen = correlatividadService.obtenerResumen(request);
        return ResponseEntity.ok(ApiResponse.success("Resumen de correlatividad generado con éxito", resumen));
    }

    @PostMapping("/detalle")
    public ResponseEntity<ApiResponse<List<VentaRegistroDto>>> obtenerDetalle(
            @RequestBody CorrelatividadDetalleRequestDTO request) {
        List<VentaRegistroDto> detalle = correlatividadService.obtenerDetalle(request);
        return ResponseEntity.ok(ApiResponse.success("Detalle de correlatividad obtenido con éxito", detalle));
    }
}
