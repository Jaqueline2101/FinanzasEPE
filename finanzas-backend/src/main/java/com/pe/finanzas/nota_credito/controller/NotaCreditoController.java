package com.pe.finanzas.nota_credito.controller;

import com.pe.finanzas.nota_credito.dto.NotaCreditoDto;
import com.pe.finanzas.nota_credito.dto.NotaCreditoRequest;
import com.pe.finanzas.nota_credito.service.NotaCreditoService;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/notas-credito")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class NotaCreditoController {

    private final NotaCreditoService service;

    /**
     * POST /api/notas-credito
     * Emite una nueva Nota de Crédito para una Boleta o Factura existente.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<NotaCreditoDto>> emitir(
            @Valid @RequestBody NotaCreditoRequest request) {

        NotaCreditoDto dto = service.emitir(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<NotaCreditoDto>builder()
                        .success(true)
                        .message("Nota de Crédito emitida correctamente: " + dto.getNumeroNc())
                        .data(dto)
                        .build());
    }

    /**
     * GET /api/notas-credito?idSucursal=1&estado=EMITIDA&page=0&size=10
     * Lista NCs de la sucursal con filtro opcional por estado.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<NotaCreditoDto>>> listar(
            @RequestParam Long idSucursal,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        Page<NotaCreditoDto> page = service.listarPorSucursal(idSucursal, estado, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<NotaCreditoDto>>builder()
                .success(true)
                .message("Notas de Crédito listadas correctamente")
                .data(page)
                .build());
    }

    /**
     * GET /api/notas-credito/{id}
     * Detalle de una NC específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NotaCreditoDto>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<NotaCreditoDto>builder()
                .success(true)
                .message("OK")
                .data(service.obtenerPorId(id))
                .build());
    }

    /**
     * GET /api/notas-credito/por-venta/{idVenta}
     * NCs emitidas para una venta específica.
     */
    @GetMapping("/por-venta/{idVenta}")
    public ResponseEntity<ApiResponse<Page<NotaCreditoDto>>> listarPorVenta(
            @PathVariable Long idVenta,
            @PageableDefault(size = 10) Pageable pageable) {

        return ResponseEntity.ok(ApiResponse.<Page<NotaCreditoDto>>builder()
                .success(true)
                .message("OK")
                .data(service.listarPorVenta(idVenta, pageable))
                .build());
    }

    /**
     * PUT /api/notas-credito/{id}/anular?idUsuario=5
     * Anula una NC previamente emitida.
     */
    @PutMapping("/{id}/anular")
    public ResponseEntity<ApiResponse<NotaCreditoDto>> anular(
            @PathVariable Long id,
            @RequestParam Long idUsuario) {

        NotaCreditoDto dto = service.anular(id, idUsuario);
        return ResponseEntity.ok(ApiResponse.<NotaCreditoDto>builder()
                .success(true)
                .message("Nota de Crédito anulada correctamente.")
                .data(dto)
                .build());
    }

    /**
     * GET /api/notas-credito/motivos
     * Devuelve el catálogo de motivos SUNAT para poblar el dropdown del frontend.
     */
    @GetMapping("/motivos")
    public ResponseEntity<ApiResponse<Map<String, String>>> motivos() {
        Map<String, String> catalogo = Map.of(
                "01", "Anulación de la operación",
                "02", "Anulación por error en el RUC",
                "03", "Corrección por error en la descripción",
                "04", "Descuento global",
                "05", "Descuento por ítem",
                "06", "Devolución total",
                "07", "Devolución por ítem",
                "13", "Ajuste en el monto del comprobante");
        return ResponseEntity.ok(ApiResponse.<Map<String, String>>builder()
                .success(true)
                .message("OK")
                .data(catalogo)
                .build());
    }
}
