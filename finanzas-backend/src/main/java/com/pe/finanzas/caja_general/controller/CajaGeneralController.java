package com.pe.finanzas.caja_general.controller;

import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoRequest;
import com.pe.finanzas.caja_general.dto.CajaGeneralMovimientoResponse;
import com.pe.finanzas.caja_general.dto.CajaGeneralResponse;
import com.pe.finanzas.caja_general.service.CajaGeneralService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/caja-general")
@RequiredArgsConstructor
public class CajaGeneralController {

    private final CajaGeneralService cajaGeneralService;

    @GetMapping("/sucursal/{idSucursal}")
    public ResponseEntity<Map<String, Object>> obtenerPorSucursal(@PathVariable Long idSucursal) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caja general obtenida",
                "data", cajaGeneralService.obtenerPorSucursal(idSucursal)));
    }

    @GetMapping("/sucursal/{idSucursal}/movimientos")
    public ResponseEntity<Map<String, Object>> listarMovimientos(
            @PathVariable Long idSucursal,
            @RequestParam(required = false) String metodoPago,
            Pageable pageable) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimientos obtenidos",
                "data", cajaGeneralService.listarMovimientos(idSucursal, metodoPago, pageable)));
    }

    @PostMapping("/movimiento")
    public ResponseEntity<Map<String, Object>> registrarMovimiento(
            @RequestBody CajaGeneralMovimientoRequest request) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimiento registrado",
                "data", cajaGeneralService.registrarMovimiento(request)));
    }

    @PutMapping("/movimiento/{id}")
    public ResponseEntity<Map<String, Object>> actualizarMovimiento(
            @PathVariable Long id,
            @RequestBody CajaGeneralMovimientoRequest request) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimiento actualizado",
                "data", cajaGeneralService.actualizarMovimiento(id, request)));
    }

    @DeleteMapping("/movimiento/{id}")
    public ResponseEntity<Map<String, Object>> eliminarMovimiento(@PathVariable Long id) {
        cajaGeneralService.eliminarMovimiento(id);
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimiento eliminado"));
    }
}
