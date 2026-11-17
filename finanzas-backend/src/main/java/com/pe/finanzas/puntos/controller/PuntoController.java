package com.pe.finanzas.puntos.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.pe.finanzas.puntos.dto.PuntoDto;
import com.pe.finanzas.puntos.service.PuntoService;
import com.pe.finanzas.shared.dto.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/puntos")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class PuntoController {

    private final PuntoService puntoService;

    /**
     * Crear un nuevo punto de venta
     * POST /api/puntos
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PUNTO_CREAR')")
    public ResponseEntity<ApiResponse<PuntoDto>> crear(@Valid @RequestBody PuntoDto puntoDto) {

        PuntoDto punto = puntoService.crear(puntoDto);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PuntoDto>builder()
                        .success(true)
                        .message("Punto de venta creado exitosamente")
                        .data(punto)
                        .build());
    }

    /**
     * Actualizar un punto de venta existente
     * PUT /api/puntos/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<PuntoDto>> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody PuntoDto puntoDto) {

        PuntoDto punto = puntoService.actualizar(id, puntoDto);

        return ResponseEntity.ok(ApiResponse.<PuntoDto>builder()
                .success(true)
                .message("Punto de venta actualizado exitosamente")
                .data(punto)
                .build());
    }

    /**
     * Obtener un punto de venta por ID
     * GET /api/puntos/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<PuntoDto>> obtenerPorId(@PathVariable Long id) {

        PuntoDto punto = puntoService.obtenerPorId(id);

        return ResponseEntity.ok(ApiResponse.<PuntoDto>builder()
                .success(true)
                .message("Punto de venta obtenido exitosamente")
                .data(punto)
                .build());
    }

    /**
     * Listar todos los puntos de venta con paginación
     * GET /api/puntos
     */
    @GetMapping
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<Page<PuntoDto>>> listarTodos(
            @PageableDefault(page = 0, size = 10, sort = "nombrePunto") Pageable pageable) {

        Page<PuntoDto> puntos = puntoService.listarTodos(pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PuntoDto>>builder()
                .success(true)
                .message("Puntos de venta obtenidos exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Listar puntos de venta activos
     * GET /api/puntos/activos
     */
    @GetMapping("/activos")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<PuntoDto>>> listarActivos() {

        List<PuntoDto> puntos = puntoService.listarActivos();

        return ResponseEntity.ok(ApiResponse.<List<PuntoDto>>builder()
                .success(true)
                .message("Puntos activos obtenidos exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Listar puntos de venta por sucursal
     * GET /api/puntos/sucursal/{idSucursal}
     */
    @GetMapping("/sucursal/{idSucursal}")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<PuntoDto>>> listarPorSucursal(@PathVariable Long idSucursal) {

        List<PuntoDto> puntos = puntoService.listarPorSucursal(idSucursal);

        return ResponseEntity.ok(ApiResponse.<List<PuntoDto>>builder()
                .success(true)
                .message("Puntos de la sucursal obtenidos exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Listar puntos activos por sucursal
     * GET /api/puntos/sucursal/{idSucursal}/activos
     */
    @GetMapping("/sucursal/{idSucursal}/activos")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<List<PuntoDto>>> listarActivosPorSucursal(@PathVariable Long idSucursal) {

        List<PuntoDto> puntos = puntoService.listarActivosPorSucursal(idSucursal);

        return ResponseEntity.ok(ApiResponse.<List<PuntoDto>>builder()
                .success(true)
                .message("Puntos activos de la sucursal obtenidos exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Buscar puntos por nombre
     * GET /api/puntos/buscar?nombre=xxx
     */
    @GetMapping("/buscar")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<Page<PuntoDto>>> buscarPorNombre(
            @RequestParam String nombre,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        Page<PuntoDto> puntos = puntoService.buscarPorNombre(nombre, pageable);

        return ResponseEntity.ok(ApiResponse.<Page<PuntoDto>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(puntos)
                .build());
    }

    /**
     * Eliminar un punto de venta (soft delete)
     * DELETE /api/puntos/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PUNTO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> eliminar(@PathVariable Long id) {

        puntoService.eliminar(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Punto de venta eliminado exitosamente")
                .build());
    }

    /**
     * Activar/Desactivar un punto de venta
     * PATCH /api/puntos/{id}/estado?activo=true
     */
    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasAuthority('PUNTO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> cambiarEstado(
            @PathVariable Long id,
            @RequestParam Boolean activo) {

        puntoService.cambiarEstado(id, activo);

        String message = activo ? "Punto activado exitosamente" : "Punto desactivado exitosamente";

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build());
    }

    /**
     * Contar puntos por sucursal
     * GET /api/puntos/sucursal/{idSucursal}/contar
     */
    @GetMapping("/sucursal/{idSucursal}/contar")
    @PreAuthorize("hasAuthority('PUNTO_LEER')")
    public ResponseEntity<ApiResponse<Long>> contarPorSucursal(@PathVariable Long idSucursal) {

        long count = puntoService.contarPorSucursal(idSucursal);

        return ResponseEntity.ok(ApiResponse.<Long>builder()
                .success(true)
                .message("Conteo realizado exitosamente")
                .data(count)
                .build());
    }
}
