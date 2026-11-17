package com.pe.finanzas.cuentas_convenio.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.pe.finanzas.cuentas_convenio.dto.CredCtaCteDto;
import com.pe.finanzas.cuentas_convenio.entity.CredCtaCte;
import com.pe.finanzas.cuentas_convenio.service.CredCtaCteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cred-ctacte")
@RequiredArgsConstructor
public class CredCtaCteController {

    private final CredCtaCteService service;

    @PostMapping
    public ResponseEntity<CredCtaCte> crear(@RequestBody CredCtaCteDto dto) {
        return ResponseEntity.ok(service.crearCuenta(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CredCtaCte> actualizar(@PathVariable Long id, @RequestBody CredCtaCteDto dto) {
        return ResponseEntity.ok(service.actualizarCuenta(id, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<CredCtaCte> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(service.obtenerPorId(id));
    }

    @GetMapping
    public ResponseEntity<List<CredCtaCte>> listarTodas() {
        return ResponseEntity.ok(service.listarTodas());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        service.eliminarCuenta(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/sequence/next/{idCompania}")
    public ResponseEntity<java.util.Map<String, String>> getNextSequence(@PathVariable Long idCompania) {
        String nro = service.previsualizarNroCtacte(idCompania);
        return ResponseEntity.ok(java.util.Map.of("nroCtacte", nro));
    }

    @GetMapping("/paciente/{idPaciente}")
    public ResponseEntity<List<CredCtaCte>> listarPorPaciente(@PathVariable Long idPaciente) {
        return ResponseEntity.ok(service.listarPorPaciente(idPaciente));
    }

    @GetMapping("/dni/{dni}")
    public ResponseEntity<List<CredCtaCte>> listarPorDni(@PathVariable String dni) {
        return ResponseEntity.ok(service.listarPorDni(dni));
    }
}
