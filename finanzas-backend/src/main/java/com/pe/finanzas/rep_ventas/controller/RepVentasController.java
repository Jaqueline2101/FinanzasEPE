package com.pe.finanzas.rep_ventas.controller;

import com.pe.finanzas.rep_ventas.dto.RepVentasRequestDTO;
import com.pe.finanzas.rep_ventas.dto.RepVentasResumenDTO;
import com.pe.finanzas.rep_ventas.service.RepVentasService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/ventas")
@RequiredArgsConstructor
public class RepVentasController {

    private final RepVentasService repVentasService;

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<RepVentasResumenDTO>> generarReporte(
            @RequestBody RepVentasRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaDesc") String sortStr) {

        // Sorting logic based on string
        Sort sort;
        if ("fechaAsc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.ASC, "fecha");
        } else if ("montoDesc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.DESC, "total");
        } else if ("montoAsc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.ASC, "total");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "fecha"); // fechaDesc default
        }

        RepVentasResumenDTO resumen = repVentasService.generarReporte(request, page, size, sortStr);

        return ResponseEntity.ok(ApiResponse.success("Reporte generado con éxito", resumen));
    }
}
