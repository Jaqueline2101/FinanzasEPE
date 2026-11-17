package com.pe.finanzas.puntos.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pe.finanzas.puntos.dto.TipoDocumentoDto;
import com.pe.finanzas.puntos.service.TipoDocumentoService;
import com.pe.finanzas.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/tipos-documento")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TipoDocumentoController {

    private final TipoDocumentoService tipoDocumentoService;

    /**
     * Crear un nuevo tipo de documento
     * POST /api/tipos-documento
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<TipoDocumentoDto>> crear(@Valid @RequestBody TipoDocumentoDto dto) {

        TipoDocumentoDto tipoDocumento = tipoDocumentoService.crear(dto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<TipoDocumentoDto>builder()
                        .success(true)
                        .message("Tipo de documento creado exitosamente")
                        .data(tipoDocumento)
                        .build());
    }

    /**
     * Actualizar un tipo de documento
     * PUT /api/tipos-documento/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<TipoDocumentoDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody TipoDocumentoDto dto) {

        TipoDocumentoDto tipoDocumento = tipoDocumentoService.actualizar(id, dto);

        return ResponseEntity.ok(ApiResponse.<TipoDocumentoDto>builder()
                .success(true)
                .message("Tipo de documento actualizado exitosamente")
                .data(tipoDocumento)
                .build());
    }

    /**
     * Obtener un tipo de documento por ID
     * GET /api/tipos-documento/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<TipoDocumentoDto>> obtenerPorId(@PathVariable Long id) {

        TipoDocumentoDto tipoDocumento = tipoDocumentoService.obtenerPorId(id);

        return ResponseEntity.ok(ApiResponse.<TipoDocumentoDto>builder()
                .success(true)
                .message("Tipo de documento obtenido exitosamente")
                .data(tipoDocumento)
                .build());
    }

    /**
     * Listar todos los tipos de documento
     * GET /api/tipos-documento
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<TipoDocumentoDto>>> listarTodos() {

        List<TipoDocumentoDto> tiposDocumento = tipoDocumentoService.listarTodos();

        return ResponseEntity.ok(ApiResponse.<List<TipoDocumentoDto>>builder()
                .success(true)
                .message("Tipos de documento obtenidos exitosamente")
                .data(tiposDocumento)
                .build());
    }

    /**
     * Listar tipos de documento activos
     * GET /api/tipos-documento/activos
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<TipoDocumentoDto>>> listarActivos() {

        List<TipoDocumentoDto> tiposDocumento = tipoDocumentoService.listarActivos();

        return ResponseEntity.ok(ApiResponse.<List<TipoDocumentoDto>>builder()
                .success(true)
                .message("Tipos de documento activos obtenidos exitosamente")
                .data(tiposDocumento)
                .build());
    }

    /**
     * Buscar tipo de documento por código
     * GET /api/tipos-documento/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<TipoDocumentoDto>> buscarPorCodigo(@PathVariable String codigo) {

        TipoDocumentoDto tipoDocumento = tipoDocumentoService.buscarPorCodigo(codigo);

        return ResponseEntity.ok(ApiResponse.<TipoDocumentoDto>builder()
                .success(true)
                .message("Tipo de documento encontrado")
                .data(tipoDocumento)
                .build());
    }

    /**
     * Eliminar un tipo de documento (soft delete)
     * DELETE /api/tipos-documento/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {

        tipoDocumentoService.eliminar(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Tipo de documento eliminado exitosamente")
                .build());
    }
}
