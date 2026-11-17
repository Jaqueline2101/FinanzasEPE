package com.pe.finanzas.otros_pagos.controller;

import com.pe.finanzas.otros_pagos.dto.OtroPagoRequestDTO;
import com.pe.finanzas.otros_pagos.dto.OtroPagoResponseDTO;
import com.pe.finanzas.otros_pagos.service.OtrosPagosService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/otros-pagos")
@RequiredArgsConstructor
public class OtrosPagosController {

    private final OtrosPagosService otrosPagosService;

    @PostMapping
    public ResponseEntity<Object> registrarOtroPago(@RequestBody OtroPagoRequestDTO dto) {
        return ResponseEntity.ok(otrosPagosService.registrarOtroPago(dto));
    }

    // Ahora devuelve un objeto genérico o DTO de movimiento
    @GetMapping
    public ResponseEntity<Page<OtroPagoResponseDTO>> listarOtrosPagos(
            @RequestParam Long idSucursal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(otrosPagosService.listarOtrosPagos(idSucursal, PageRequest.of(page, size)));
    }
}