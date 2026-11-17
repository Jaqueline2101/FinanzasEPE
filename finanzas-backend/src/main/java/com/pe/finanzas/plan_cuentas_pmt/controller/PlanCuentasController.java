package com.pe.finanzas.plan_cuentas_pmt.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pe.finanzas.plan_cuentas_pmt.dto.HistorialMovimientoDTO;
import com.pe.finanzas.plan_cuentas_pmt.dto.PlanCuentasRegistroDTO;
import com.pe.finanzas.plan_cuentas_pmt.service.PlanCuentasService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/plan-cuentas")
@RequiredArgsConstructor
public class PlanCuentasController {

    private final PlanCuentasService planCuentasService;

    @PostMapping
    public ResponseEntity<PlanCuentasRegistroDTO> registrar(@RequestBody PlanCuentasRegistroDTO dto) {
        return ResponseEntity.ok(planCuentasService.registrar(dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PlanCuentasRegistroDTO> obtenerPorId(
            @PathVariable Long id,
            @RequestParam(required = false) String tipo) {
        return ResponseEntity.ok(planCuentasService.obtenerPorId(id, tipo));
    }

    @GetMapping("/{id}/historial")
    public ResponseEntity<List<HistorialMovimientoDTO>> obtenerHistorial(@PathVariable Long id) {
        return ResponseEntity.ok(planCuentasService.obtenerHistorial(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        planCuentasService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<PlanCuentasRegistroDTO>> listar() {
        return ResponseEntity.ok(planCuentasService.listarTodos());
    }
}
