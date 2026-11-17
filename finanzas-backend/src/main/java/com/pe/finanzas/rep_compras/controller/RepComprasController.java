package com.pe.finanzas.rep_compras.controller;

import com.pe.finanzas.rep_compras.dto.RepComprasRequestDTO;
import com.pe.finanzas.rep_compras.dto.RepComprasResumenDTO;
import com.pe.finanzas.rep_compras.service.RepComprasService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reportes/compras")
@RequiredArgsConstructor
public class RepComprasController {

    private final RepComprasService repComprasService;

    @PostMapping
    public ResponseEntity<RepComprasResumenDTO> generarReporte(
            @RequestBody RepComprasRequestDTO request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaEmision") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        return ResponseEntity.ok(repComprasService.generarReporte(request, pageable));
    }
}
