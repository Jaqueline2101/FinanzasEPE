package com.pe.finanzas.caja_chica.controller;

import com.pe.finanzas.auth.service.JwtService;
import com.pe.finanzas.caja_chica.dto.*;
import com.pe.finanzas.caja_chica.services.CajaChicaService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/caja-chica")
@RequiredArgsConstructor
public class CajaChicaController {

    private final CajaChicaService cajaChicaService;
    private final JwtService jwtService;

    @GetMapping("/cajas")
    public ResponseEntity<Map<String, Object>> listarCajas() {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Cajas listadas correctamente",
                "data", cajaChicaService.listarCajas()));
    }

    @GetMapping("/abierta")
    public ResponseEntity<Map<String, Object>> obtenerCajaAbierta(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Long puntoId = null;
        Long sucursalId = null;
        String userId = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            Long idExtraido = jwtService.extractUserId(token);
            userId = (idExtraido != null) ? String.valueOf(idExtraido) : null;
            username = jwtService.extractUsername(token);
            puntoId = jwtService.extractPuntoId(token);
            sucursalId = jwtService.extractSucursalId(token);
        }

        java.util.Map<String, Object> response = new java.util.HashMap<>();
        response.put("success", true);
        response.put("message", "Caja abierta obtenida");
        response.put("data", cajaChicaService.obtenerCajaAbierta(puntoId, sucursalId, userId, username).orElse(null));

        return ResponseEntity.ok(response);
    }

    @PostMapping("/cajas")
    public ResponseEntity<Map<String, Object>> crearCaja(
            @RequestBody CajaChicaRequest requestBody,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caja creada correctamente",
                "data", cajaChicaService.crearCaja(requestBody, userId)));
    }

    @PostMapping("/movimientos")
    public ResponseEntity<Map<String, Object>> registrarMovimiento(
            @RequestBody MovimientoRequest requestBody,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimiento registrado correctamente",
                "data", cajaChicaService.registrarMovimiento(requestBody, userId)));
    }

    @GetMapping("/movimientos/{cajaId}")
    public ResponseEntity<Map<String, Object>> listarMovimientos(@PathVariable Long cajaId, Pageable pageable) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Movimientos listados correctamente",
                "data", cajaChicaService.listarMovimientos(cajaId, pageable)));
    }

    @GetMapping("/resumen/{id}")
    public ResponseEntity<Map<String, Object>> obtenerResumen(@PathVariable Long id) {
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Resumen de caja obtenido",
                "data", cajaChicaService.obtenerResumen(id)));
    }

    @PutMapping("/cajas/{id}/cerrar")
    public ResponseEntity<Map<String, Object>> cerrarCaja(
            @PathVariable Long id,
            @RequestParam BigDecimal saldoCierreReal,
            @RequestParam(defaultValue = "false") boolean transferirACajaGeneral,
            @RequestHeader("Authorization") String authHeader) {
        String userId = String.valueOf(jwtService.extractUserId(authHeader.substring(7)));
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Caja cerrada correctamente",
                "data", cajaChicaService.cerrarCaja(id, saldoCierreReal, transferirACajaGeneral, userId)));
    }
}
