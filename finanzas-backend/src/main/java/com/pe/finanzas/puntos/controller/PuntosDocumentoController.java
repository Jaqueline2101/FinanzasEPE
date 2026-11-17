package com.pe.finanzas.puntos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pe.finanzas.puntos.dto.PuntosDocumentoDto;
import com.pe.finanzas.puntos.dto.SerieCorrelativoDto;
import com.pe.finanzas.puntos.service.PuntosDocumentoService;
import com.pe.finanzas.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/puntos-documento")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PuntosDocumentoController {

    private final PuntosDocumentoService puntosDocumentoService;

    /**
     * Asignar un tipo de documento a un punto
     * POST /api/puntos/{idPunto}/documentos
     */
    @PostMapping("/punto/{idPunto}")
    @PreAuthorize("hasAuthority('PUNTO_DOCUMENTO_GESTIONAR')")
    public ResponseEntity<ApiResponse<PuntosDocumentoDto>> asignarDocumento(
            @PathVariable Long idPunto,
            @Valid @RequestBody PuntosDocumentoDto dto) {

        PuntosDocumentoDto documento = puntosDocumentoService.asignarDocumentoAPunto(idPunto, dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PuntosDocumentoDto>builder()
                        .success(true)
                        .message("Documento asignado al punto exitosamente")
                        .data(documento)
                        .build());
    }

    /**
     * Actualizar un documento de punto
     * PUT /api/puntos-documento/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_DOCUMENTO_GESTIONAR')")
    public ResponseEntity<ApiResponse<PuntosDocumentoDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PuntosDocumentoDto dto) {

        PuntosDocumentoDto documento = puntosDocumentoService.actualizar(id, dto);

        return ResponseEntity.ok(ApiResponse.<PuntosDocumentoDto>builder()
                .success(true)
                .message("Documento actualizado exitosamente")
                .data(documento)
                .build());
    }

    /**
     * Obtener un documento por ID
     * GET /api/puntos-documento/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<PuntosDocumentoDto>> obtenerPorId(@PathVariable Long id) {

        PuntosDocumentoDto documento = puntosDocumentoService.obtenerPorId(id);

        return ResponseEntity.ok(ApiResponse.<PuntosDocumentoDto>builder()
                .success(true)
                .message("Documento obtenido exitosamente")
                .data(documento)
                .build());
    }

    /**
     * Listar documentos de un punto
     * GET /api/puntos-documento/punto/{idPunto}
     */
    @GetMapping("/punto/{idPunto}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<PuntosDocumentoDto>>> listarPorPunto(@PathVariable Long idPunto) {

        List<PuntosDocumentoDto> documentos = puntosDocumentoService.listarPorPunto(idPunto);

        return ResponseEntity.ok(ApiResponse.<List<PuntosDocumentoDto>>builder()
                .success(true)
                .message("Documentos del punto obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Listar documentos activos de un punto
     * GET /api/puntos-documento/punto/{idPunto}/activos
     */
    @GetMapping("/punto/{idPunto}/activos")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<PuntosDocumentoDto>>> listarActivosPorPunto(@PathVariable Long idPunto) {

        List<PuntosDocumentoDto> documentos = puntosDocumentoService.listarActivosPorPunto(idPunto);

        return ResponseEntity.ok(ApiResponse.<List<PuntosDocumentoDto>>builder()
                .success(true)
                .message("Documentos activos obtenidos exitosamente")
                .data(documentos)
                .build());
    }

    /**
     * Obtener siguiente número de un documento
     * GET /api/puntos-documento/{id}/siguiente
     */
    @GetMapping("/{id}/siguiente")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<SerieCorrelativoDto>> obtenerSiguienteNumero(@PathVariable Long id) {

        SerieCorrelativoDto correlativo = puntosDocumentoService.obtenerSiguienteNumero(id);

        return ResponseEntity.ok(ApiResponse.<SerieCorrelativoDto>builder()
                .success(true)
                .message("Siguiente número obtenido exitosamente")
                .data(correlativo)
                .build());
    }

    /**
     * Obtener siguiente número por punto y tipo de documento
     * GET /api/puntos-documento/punto/{idPunto}/tipo/{idTipoDocumento}/siguiente
     */
    @GetMapping("/punto/{idPunto}/tipo/{idTipoDocumento}/siguiente")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<SerieCorrelativoDto>> obtenerSiguienteNumeroPorPuntoYTipo(
            @PathVariable Long idPunto,
            @PathVariable Long idTipoDocumento) {

        SerieCorrelativoDto correlativo = puntosDocumentoService
                .obtenerSiguienteNumeroPorPuntoYTipo(idPunto, idTipoDocumento);

        return ResponseEntity.ok(ApiResponse.<SerieCorrelativoDto>builder()
                .success(true)
                .message("Siguiente número obtenido exitosamente")
                .data(correlativo)
                .build());
    }

    /**
     * Incrementar correlativo de un documento
     * POST /api/puntos-documento/{id}/incrementar
     */
    @PostMapping("/{id}/incrementar")
    @PreAuthorize("hasAuthority('PUNTO_DOCUMENTO_GESTIONAR')")
    public ResponseEntity<ApiResponse<Void>> incrementarCorrelativo(@PathVariable Long id) {

        puntosDocumentoService.incrementarCorrelativo(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Correlativo incrementado exitosamente")
                .build());
    }

    /**
     * Eliminar un documento (soft delete)
     * DELETE /api/puntos-documento/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_DOCUMENTO_GESTIONAR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {

        puntosDocumentoService.eliminar(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Documento eliminado exitosamente")
                .build());
    }

    /**
     * Activar/Desactivar un documento
     * PATCH /api/puntos-documento/{id}/estado?activo=true
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PUNTO_DOCUMENTO_GESTIONAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {

        puntosDocumentoService.cambiarEstado(id, activo);

        String message = activo ? "Documento activado exitosamente" : "Documento desactivado exitosamente";

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build());
    }
}
