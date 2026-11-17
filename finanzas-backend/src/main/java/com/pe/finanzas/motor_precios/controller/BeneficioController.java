package com.pe.finanzas.motor_precios.controller;

import com.pe.finanzas.motor_precios.dto.BeneficioDescuentoMasivoRequest;
import com.pe.finanzas.motor_precios.dto.BeneficioDto;
import com.pe.finanzas.motor_precios.dto.BeneficioRequest;
import com.pe.finanzas.motor_precios.service.BeneficioService;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/beneficios")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class BeneficioController {

    private final BeneficioService beneficioService;

    @PostMapping
    @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
    public ResponseEntity<ApiResponse<BeneficioDto>> crear(@Valid @RequestBody BeneficioRequest request) {
        try {
            BeneficioDto dto = beneficioService.crear(request);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ApiResponse.<BeneficioDto>builder()
                            .success(true)
                            .message("Beneficio especial creado exitosamente")
                            .data(dto)
                            .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<BeneficioDto>builder()
                            .success(false)
                            .message("Error al crear beneficio: " + e.getMessage())
                            .build());
        }
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CONVENIO_LEER')")
    public ResponseEntity<ApiResponse<List<BeneficioDto>>> listarPorTipo(
            @RequestParam(defaultValue = "GENERAL") String tipoPlan) {
        List<BeneficioDto> lista = beneficioService.listarPorTipo(tipoPlan);
        return ResponseEntity.ok(ApiResponse.<List<BeneficioDto>>builder()
                .success(true)
                .message("Beneficios obtenidos exitosamente")
                .data(lista)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CONVENIO_LEER')")
    public ResponseEntity<ApiResponse<BeneficioDto>> obtener(@PathVariable Long id) {
        BeneficioDto dto = beneficioService.obtener(id);
        return ResponseEntity.ok(ApiResponse.<BeneficioDto>builder()
                .success(true)
                .message("Beneficio obtenido exitosamente")
                .data(dto)
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CONVENIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<BeneficioDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody BeneficioRequest request) {
        try {
            BeneficioDto dto = beneficioService.actualizar(id, request);
            return ResponseEntity.ok(ApiResponse.<BeneficioDto>builder()
                    .success(true)
                    .message("Beneficio actualizado exitosamente")
                    .data(dto)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<BeneficioDto>builder()
                            .success(false)
                            .message("Error al actualizar: " + e.getMessage())
                            .build());
        }
    }

    @PostMapping("/{id}/descuentos/masivo")
    @PreAuthorize("hasAuthority('CONVENIO_CREAR')")
    public ResponseEntity<ApiResponse<Integer>> aplicarDescuentoMasivo(
            @PathVariable Long id,
            @Valid @RequestBody BeneficioDescuentoMasivoRequest request) {
        try {
            int aplicadas = beneficioService.aplicarDescuentoMasivo(id, request);
            return ResponseEntity.ok(ApiResponse.<Integer>builder()
                    .success(true)
                    .message("Descuento masivo aplicado a " + aplicadas + " nivel(es) exitosamente")
                    .data(aplicadas)
                    .build());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ApiResponse.<Integer>builder()
                            .success(false)
                            .message("Error al aplicar descuento masivo: " + e.getMessage())
                            .build());
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('CONVENIO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {
        beneficioService.eliminar(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Beneficio eliminado exitosamente")
                .build());
    }
}
