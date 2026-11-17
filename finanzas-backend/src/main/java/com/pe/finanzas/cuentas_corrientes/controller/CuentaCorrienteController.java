package com.pe.finanzas.cuentas_corrientes.controller;

import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.service.CuentaCorrienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/cuentas-corrientes")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CuentaCorrienteController {

    private final CuentaCorrienteService service;

    @GetMapping
    public ResponseEntity<List<CuentaCorriente>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/activas")
    public ResponseEntity<List<CuentaCorriente>> getActive() {
        return ResponseEntity.ok(service.findActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CuentaCorriente> getById(@PathVariable Long id) {
        return service.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CuentaCorriente> create(@RequestBody CuentaCorriente cuenta) {
        return ResponseEntity.ok(service.save(cuenta));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CuentaCorriente> update(@PathVariable Long id, @RequestBody CuentaCorriente cuenta) {
        try {
            return ResponseEntity.ok(service.update(id, cuenta));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok().build();
    }
}
