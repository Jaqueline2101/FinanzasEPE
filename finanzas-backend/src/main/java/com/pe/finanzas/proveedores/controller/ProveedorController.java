package com.pe.finanzas.proveedores.controller;

import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.service.ProveedorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/proveedores")
@RequiredArgsConstructor
public class ProveedorController {

    private final ProveedorService proveedorService;

    @GetMapping
    public ResponseEntity<List<Proveedor>> getAll() {
        return ResponseEntity.ok(proveedorService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Proveedor> getById(@PathVariable Long id) {
        return proveedorService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Proveedor> create(@RequestBody Proveedor proveedor) {
        return ResponseEntity.ok(proveedorService.save(proveedor));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Proveedor> update(@PathVariable Long id, @RequestBody Proveedor proveedor) {
        try {
            return ResponseEntity.ok(proveedorService.update(id, proveedor));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        proveedorService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/check-doc")
    public ResponseEntity<Boolean> checkDoc(@RequestParam String num, @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(proveedorService.checkDocDuplicity(num, excludeId));
    }

    @GetMapping("/check-razon-social")
    public ResponseEntity<Boolean> checkRazonSocial(@RequestParam String valor,
            @RequestParam(required = false) Long excludeId) {
        return ResponseEntity.ok(proveedorService.checkRazonSocialDuplicity(valor, excludeId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<Proveedor>> search(@RequestParam String query) {
        return ResponseEntity.ok(proveedorService.searchByNombre(query));
    }
}
