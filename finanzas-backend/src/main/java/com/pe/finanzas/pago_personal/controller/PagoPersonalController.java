package com.pe.finanzas.pago_personal.controller;

import com.pe.finanzas.pago_personal.dto.PagoPersonalRequest;
import com.pe.finanzas.pago_personal.service.PagoPersonalService;
import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos-personal")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PagoPersonalController {

    private final PagoPersonalService pagoService;

    @PostMapping
    @PreAuthorize("hasAuthority('PAGO_PERSONAL_CREAR')")
    public ResponseEntity<ApiResponse<PagoComprobanteDto>> registrarPago(
            @Valid @RequestBody PagoPersonalRequest request) {

        PagoComprobanteDto pago = pagoService.registrarPago(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PagoComprobanteDto>builder()
                        .success(true)
                        .message("Pago de personal registrado exitosamente")
                        .data(pago)
                        .build());
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasAuthority('PAGO_PERSONAL_CREAR')")
    public ResponseEntity<ApiResponse<List<PagoComprobanteDto>>> registrarPagosMasivos(
            @Valid @RequestBody List<PagoPersonalRequest> requests) {

        List<PagoComprobanteDto> pagos = pagoService.registrarPagosMasivos(requests);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<List<PagoComprobanteDto>>builder()
                        .success(true)
                        .message(pagos.size() + " pagos de personal registrados exitosamente")
                        .data(pagos)
                        .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PAGO_PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<List<PagoComprobanteDto>>> listarPagos() {

        List<PagoComprobanteDto> pagos = pagoService.listarPagos();

        return ResponseEntity.ok(ApiResponse.<List<PagoComprobanteDto>>builder()
                .success(true)
                .message("Pagos de personal recuperados exitosamente")
                .data(pagos)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PAGO_PERSONAL_LEER')")
    public ResponseEntity<ApiResponse<PagoComprobanteDto>> obtenerPorId(@PathVariable Long id) {

        PagoComprobanteDto pago = pagoService.obtenerPorId(id);

        if (pago == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.<PagoComprobanteDto>builder()
                            .success(false)
                            .message("Pago de personal no encontrado")
                            .build());
        }

        return ResponseEntity.ok(ApiResponse.<PagoComprobanteDto>builder()
                .success(true)
                .message("Pago de personal recuperado exitosamente")
                .data(pago)
                .build());
    }
}
