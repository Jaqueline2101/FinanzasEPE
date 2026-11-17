package com.pe.finanzas.pagorec_honorarios.controller;

import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;
import com.pe.finanzas.pagorec_honorarios.dto.PagoHonorariosRequest;
import com.pe.finanzas.pagorec_honorarios.service.PagoHonorariosService;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos-honorarios")
@RequiredArgsConstructor
public class PagoHonorariosController {

    private final PagoHonorariosService pagoService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAGO_HONORARIO_CREAR')")
    public ResponseEntity<ApiResponse<PagoComprobanteDto>> registrarPago(
            @Valid @RequestBody PagoHonorariosRequest request) {

        PagoComprobanteDto pago = pagoService.registrarPago(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PagoComprobanteDto>builder()
                        .success(true)
                        .message("Pago registrado exitosamente")
                        .data(pago)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAGO_HONORARIO_LEER')")
    public ResponseEntity<ApiResponse<List<PagoComprobanteDto>>> listarPagos() {

        List<PagoComprobanteDto> pagos = pagoService.listarPagos();

        return ResponseEntity.ok(ApiResponse.<List<PagoComprobanteDto>>builder()
                .success(true)
                .message("Pagos recuperados exitosamente")
                .data(pagos)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAGO_HONORARIO_LEER')")
    public ResponseEntity<ApiResponse<PagoComprobanteDto>> obtenerPorId(@PathVariable Long id) {

        PagoComprobanteDto pago = pagoService.obtenerPorId(id);

        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PagoComprobanteDto>builder()
                            .success(false)
                            .message("Pago no encontrado")
                            .build());
        }

        return ResponseEntity.ok(ApiResponse.<PagoComprobanteDto>builder()
                .success(true)
                .message("Pago recuperado exitosamente")
                .data(pago)
                .build());
    }
}
