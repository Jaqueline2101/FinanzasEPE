package com.pe.finanzas.autorizar_descuento.controller;

import com.pe.finanzas.autorizar_descuento.dto.AutorizacionRequestDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResolucionDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResponseDto;
import com.pe.finanzas.autorizar_descuento.service.AutorizacionDescuentoService;
import com.pe.finanzas.shared.dto.ApiResponse;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/autorizaciones-descuento")
@RequiredArgsConstructor
public class AutorizacionDescuentoController {

    private final AutorizacionDescuentoService service;

    @PostMapping("/solicitar")
    public ResponseEntity<ApiResponse<AutorizacionResponseDto>> solicitarDescuento(
            @RequestBody AutorizacionRequestDto request) {
        AutorizacionResponseDto response = service.solicitarDescuento(request);
        return ResponseEntity.ok(ApiResponse.success("Solicitud de descuento creada exitosamente", response));
    }

    @PutMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<AutorizacionResponseDto>> aprobarDescuento(
            @PathVariable Long id,
            @RequestBody AutorizacionResolucionDto request) {
        AutorizacionResponseDto response = service.aprobarDescuento(id, request);
        return ResponseEntity.ok(ApiResponse.success("Descuento aprobado exitosamente", response));
    }

    @PutMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<AutorizacionResponseDto>> rechazarDescuento(
            @PathVariable Long id,
            @RequestBody AutorizacionResolucionDto request) {
        AutorizacionResponseDto response = service.rechazarDescuento(id, request);
        return ResponseEntity.ok(ApiResponse.success("Descuento rechazado exitosamente", response));
    }

    @GetMapping("/estado/pendientes")
    public ResponseEntity<ApiResponse<List<AutorizacionResponseDto>>> listarPendientes() {
        List<AutorizacionResponseDto> response = service.listarPendientes();
        return ResponseEntity.ok(ApiResponse.success("Listado de autorizaciones pendientes", response));
    }

    @GetMapping("/estado/resueltos")
    public ResponseEntity<ApiResponse<List<AutorizacionResponseDto>>> listarResueltos() {
        List<AutorizacionResponseDto> response = service.listarResueltos();
        return ResponseEntity.ok(ApiResponse.success("Listado de autorizaciones resueltas", response));
    }

    @GetMapping("/cajero/{idCajero}")
    public ResponseEntity<ApiResponse<List<AutorizacionResponseDto>>> listarPorCajero(@PathVariable Long idCajero) {
        List<AutorizacionResponseDto> response = service.listarPorCajero(idCajero);
        return ResponseEntity.ok(ApiResponse.success("Listado de autorizaciones por cajero", response));
    }
}
