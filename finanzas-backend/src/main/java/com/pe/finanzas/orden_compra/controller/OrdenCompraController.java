package com.pe.finanzas.orden_compra.controller;

import com.pe.finanzas.orden_compra.dto.AnulacionRequest;
import com.pe.finanzas.orden_compra.dto.OrdenCompraRequest;
import com.pe.finanzas.orden_compra.dto.OrdenCompraResponse;
import com.pe.finanzas.orden_compra.service.OrdenCompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-compra")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class OrdenCompraController {

    private final OrdenCompraService ordenCompraService;

    @GetMapping
    public ResponseEntity<List<OrdenCompraResponse>> listar() {
        return ResponseEntity.ok(ordenCompraService.listar());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ordenCompraService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<OrdenCompraResponse> registrar(@RequestBody OrdenCompraRequest request) {
        return ResponseEntity.ok(ordenCompraService.registrar(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<OrdenCompraResponse> actualizar(@PathVariable Long id,
            @RequestBody OrdenCompraRequest request) {
        return ResponseEntity.ok(ordenCompraService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        ordenCompraService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/aprobar")
    public ResponseEntity<OrdenCompraResponse> aprobar(@PathVariable Long id, @RequestParam String usuario) {
        return ResponseEntity.ok(ordenCompraService.aprobar(id, usuario));
    }

    @PatchMapping("/{id}/anular")
    public ResponseEntity<OrdenCompraResponse> anular(@PathVariable Long id, @RequestParam String usuario,
            @RequestBody AnulacionRequest request) {
        return ResponseEntity.ok(ordenCompraService.anular(id, usuario, request.getMotivo()));
    }
}
