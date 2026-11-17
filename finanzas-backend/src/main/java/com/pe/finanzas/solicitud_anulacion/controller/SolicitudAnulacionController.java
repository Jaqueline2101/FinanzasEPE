package com.pe.finanzas.solicitud_anulacion.controller;

import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.solicitud_anulacion.dto.ResolucionRequest;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionDto;
import com.pe.finanzas.solicitud_anulacion.dto.SolicitudAnulacionRequest;
import com.pe.finanzas.solicitud_anulacion.service.SolicitudAnulacionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solicitudes-anulacion")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SolicitudAnulacionController {

    private final SolicitudAnulacionService service;

    /**
     * POST /api/solicitudes-anulacion
     * El cajero crea una solicitud de anulación para una venta de fecha anterior.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<SolicitudAnulacionDto>> crear(
            @Valid @RequestBody SolicitudAnulacionRequest request) {

        SolicitudAnulacionDto dto = service.crearSolicitud(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<SolicitudAnulacionDto>builder()
                        .success(true)
                        .message("Solicitud de anulación creada correctamente. En espera de autorización.")
                        .data(dto)
                        .build());
    }

    /**
     * GET /api/solicitudes-anulacion?idSucursal=1&estado=PENDIENTE&page=0&size=10
     * El supervisor lista sus solicitudes (bandeja de trabajo).
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<SolicitudAnulacionDto>>> listar(
            @RequestParam Long idSucursal,
            @RequestParam(required = false) String estado,
            @PageableDefault(size = 10, sort = "fechaCreacion") Pageable pageable) {

        Page<SolicitudAnulacionDto> page = service.listarPorSucursal(idSucursal, estado, pageable);
        return ResponseEntity.ok(ApiResponse.<Page<SolicitudAnulacionDto>>builder()
                .success(true)
                .message("Solicitudes listadas correctamente")
                .data(page)
                .build());
    }

    /**
     * GET /api/solicitudes-anulacion/{id}
     * Detalle de una solicitud específica.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SolicitudAnulacionDto>> obtener(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.<SolicitudAnulacionDto>builder()
                .success(true)
                .message("OK")
                .data(service.obtenerPorId(id))
                .build());
    }

    /**
     * PUT /api/solicitudes-anulacion/{id}/aprobar
     * El supervisor aprueba → se ejecuta la anulación real de la venta.
     */
    @PutMapping("/{id}/aprobar")
    public ResponseEntity<ApiResponse<SolicitudAnulacionDto>> aprobar(
            @PathVariable Long id,
            @Valid @RequestBody ResolucionRequest request) {

        SolicitudAnulacionDto dto = service.aprobar(id, request);
        return ResponseEntity.ok(ApiResponse.<SolicitudAnulacionDto>builder()
                .success(true)
                .message("Solicitud aprobada. La venta ha sido anulada correctamente.")
                .data(dto)
                .build());
    }

    /**
     * PUT /api/solicitudes-anulacion/{id}/rechazar
     * El supervisor rechaza la solicitud con motivo obligatorio.
     */
    @PutMapping("/{id}/rechazar")
    public ResponseEntity<ApiResponse<SolicitudAnulacionDto>> rechazar(
            @PathVariable Long id,
            @Valid @RequestBody ResolucionRequest request) {

        SolicitudAnulacionDto dto = service.rechazar(id, request);
        return ResponseEntity.ok(ApiResponse.<SolicitudAnulacionDto>builder()
                .success(true)
                .message("Solicitud rechazada.")
                .data(dto)
                .build());
    }

    /**
     * GET /api/solicitudes-anulacion/pendientes/count?idSucursal=1
     * Contador de solicitudes PENDIENTES para el badge del sidebar.
     */
    @GetMapping("/pendientes/count")
    public ResponseEntity<ApiResponse<Long>> contarPendientes(@RequestParam Long idSucursal) {
        long count = service.contarPendientes(idSucursal);
        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("OK")
                .data(count)
                .build());
    }
}
