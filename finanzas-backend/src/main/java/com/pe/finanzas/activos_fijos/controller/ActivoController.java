package com.pe.finanzas.activos_fijos.controller;

import com.pe.finanzas.activos_fijos.entity.Activo;
import com.pe.finanzas.activos_fijos.service.ActivoService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/activos")
@RequiredArgsConstructor
public class ActivoController {

    private final ActivoService activoService;

    @GetMapping
    public ResponseEntity<List<Activo>> getAll() {
        return ResponseEntity.ok(activoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Activo> getById(@PathVariable Long id) {
        return activoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<Activo> getByCodigoPatrimonial(@PathVariable String codigo) {
        return activoService.findByCodigoPatrimonial(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Activo> create(@RequestBody Activo activo) {
        return ResponseEntity.ok(activoService.save(activo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Activo> update(@PathVariable Long id, @RequestBody Activo activo) {
        try {
            return ResponseEntity.ok(activoService.update(id, activo));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        try {
            activoService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
