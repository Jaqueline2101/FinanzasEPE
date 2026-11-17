package com.pe.finanzas.rep_comprobantes_anulados.controller;

import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosRequestDTO;
import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosResumenDTO;
import com.pe.finanzas.rep_comprobantes_anulados.service.RepAnuladosService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/comprobantes-anulados")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class RepAnuladosController {

    private final RepAnuladosService repAnuladosService;

    @PostMapping("/generar")
    public ResponseEntity<ApiResponse<RepAnuladosResumenDTO>> generarReporte(
            @RequestBody RepAnuladosRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaDesc") String sortStr) {

        Sort sort;
        if ("fechaAsc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.ASC, "fecha");
        } else if ("montoDesc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.DESC, "total");
        } else if ("montoAsc".equalsIgnoreCase(sortStr)) {
            sort = Sort.by(Sort.Direction.ASC, "total");
        } else {
            sort = Sort.by(Sort.Direction.DESC, "fecha");
        }

        Pageable pageable = PageRequest.of(page, size, sort);
        RepAnuladosResumenDTO resumen = repAnuladosService.generarReporte(request, pageable);

        return ResponseEntity.ok(ApiResponse.success("Reporte de comprobantes anulados generado con éxito", resumen));
    }
}
