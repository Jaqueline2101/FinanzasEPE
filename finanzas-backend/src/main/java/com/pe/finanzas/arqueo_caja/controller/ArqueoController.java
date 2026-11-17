package com.pe.finanzas.arqueo_caja.controller;

import com.pe.finanzas.arqueo_caja.dto.ArqueoCajaDTO;
import com.pe.finanzas.arqueo_caja.service.ArqueoCajaService;
import com.pe.finanzas.auth.service.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/arqueos")
@RequiredArgsConstructor
public class ArqueoController {

    private final ArqueoCajaService arqueoService;
    private final JwtService jwtService;

    @GetMapping("/saldo-esperado")
    public ResponseEntity<BigDecimal> obtenerSaldoEsperado(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Long idUsuario = null;
        Long idSucursal = null;
        Long idPunto = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            idUsuario = jwtService.extractUserId(token);
            idSucursal = jwtService.extractSucursalId(token);
            idPunto = jwtService.extractPuntoId(token);
        }

        return ResponseEntity.ok(arqueoService.obtenerSaldoEsperado(idUsuario, idSucursal, idPunto));
    }

    @PostMapping("/registrar")
    public ResponseEntity<ArqueoCajaDTO> registrarArqueo(@RequestBody ArqueoCajaDTO dto, HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        Long idUsuario = null;
        Long idSucursal = null;
        Long idPunto = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            idUsuario = jwtService.extractUserId(token);
            idSucursal = jwtService.extractSucursalId(token);
            idPunto = jwtService.extractPuntoId(token);
        }
        
        dto.setIdUsuario(idUsuario);
        dto.setIdSucursal(idSucursal);
        dto.setIdPunto(idPunto);
        
        return ResponseEntity.ok(arqueoService.registrarArqueo(dto));
    }

    @GetMapping("/historial")
    public ResponseEntity<Page<ArqueoCajaDTO>> listarArqueos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request) {

        String authHeader = request.getHeader("Authorization");
        Long idUsuario = null;
        Long idSucursal = null;
        Long idPunto = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            idUsuario = jwtService.extractUserId(token);
            idSucursal = jwtService.extractSucursalId(token);
            idPunto = jwtService.extractPuntoId(token);
        }
        
        var startTime = (inicio != null) ? inicio.atStartOfDay() : null;
        var endTime = (fin != null) ? fin.atTime(LocalTime.MAX) : null;

        return ResponseEntity
                .ok(arqueoService.listarArqueos(idUsuario, idSucursal, idPunto, startTime, endTime, PageRequest.of(page, size)));
    }
}
