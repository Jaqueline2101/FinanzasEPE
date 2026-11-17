package com.pe.finanzas.valorizacion.controller;

import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.valorizacion.dto.ValorizacionResponseDTO;
import com.pe.finanzas.valorizacion.service.ValorizacionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/valorizacion")
@RequiredArgsConstructor
public class ValorizacionController {

    private final ValorizacionService valorizacionService;

    @GetMapping
    public ResponseEntity<ApiResponse<ValorizacionResponseDTO>> getReporte(
            @RequestParam(required = false) Long idSucursal,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String categoria,
            @RequestParam(required = false) String estadoStock,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Pageable pageable = PageRequest.of(page, size);
        ValorizacionResponseDTO response = valorizacionService.getReporte(idSucursal, search, categoria, estadoStock, pageable);

        return ResponseEntity.ok(ApiResponse.success("Reporte de valorización obtenido con éxito", response));
    }
}
