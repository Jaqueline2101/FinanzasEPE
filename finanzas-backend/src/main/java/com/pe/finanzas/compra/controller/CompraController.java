package com.pe.finanzas.compra.controller;

import com.pe.finanzas.compra.dto.CompraDTO;
import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PostMapping
    public ResponseEntity<Compra> registrarCompra(@RequestBody CompraDTO dto) {
        Compra nuevaCompra = compraService.registrarCompra(dto);
        return ResponseEntity.ok(nuevaCompra);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compra> obtenerCompra(@PathVariable Long id) {
        return compraService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<Page<Compra>> listarPorSucursal(
            @PathVariable Long idSucursal,
            @RequestParam(required = false) String term,
            @RequestParam(required = false) String searchType,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String fechaDesde,
            @RequestParam(required = false) String fechaHasta,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fechaEmision") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Page<Compra> compras = compraService.listBySucursal(
                idSucursal, term, searchType, estado, fechaDesde, fechaHasta,
                PageRequest.of(page, size, sort));

        return ResponseEntity.ok(compras);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarCompra(@PathVariable Long id) {
        compraService.eliminarCompra(id);
        return ResponseEntity.noContent().build();
    }
}
