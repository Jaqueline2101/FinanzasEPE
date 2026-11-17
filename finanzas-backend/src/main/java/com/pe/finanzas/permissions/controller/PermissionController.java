package com.pe.finanzas.permissions.controller;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.permissions.dto.PermissionRequest;
import com.pe.finanzas.permissions.dto.PermissionResponse;
import com.pe.finanzas.permissions.service.PermissionService;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.shared.dto.PageResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/permissions")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    // ========================================
    // CRUD BÁSICO
    // ========================================

    /**
     * Crear un nuevo permiso
     * POST /api/permissions
     */
    @PostMapping
    @PreAuthorize("hasAuthority('PERMISO_CREAR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody PermissionRequest request) {

        PermissionResponse permission = permissionService.createPermission(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<PermissionResponse>builder()
                        .success(true)
                        .message("Permiso creado exitosamente")
                        .data(permission)
                        .build());
    }

    /**
     * Actualizar un permiso existente
     * PUT /api/permissions/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<PermissionResponse>> updatePermission(
            @PathVariable Long id,
            @Valid @RequestBody PermissionRequest request) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        PermissionResponse permission = permissionService.updatePermission(id, request);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso actualizado exitosamente")
                .data(permission)
                .build());
    }

    /**
     * Eliminar un permiso (soft delete)
     * DELETE /api/permissions/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> deletePermission(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        permissionService.deletePermission(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Permiso eliminado exitosamente")
                .build());
    }

    // ========================================
    // CONSULTAS INDIVIDUALES
    // ========================================

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionById(@PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        PermissionResponse permission = permissionService.getPermissionById(id);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso obtenido exitosamente")
                .data(permission)
                .build());
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PermissionResponse>> getPermissionByName(@PathVariable String name) {

        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del permiso no puede estar vacío");
        }

        PermissionResponse permission = permissionService.getPermissionByName(name);

        return ResponseEntity.ok(ApiResponse.<PermissionResponse>builder()
                .success(true)
                .message("Permiso obtenido exitosamente")
                .data(permission)
                .build());
    }

    // ========================================
    // LISTADOS CON PAGINACIÓN
    // ========================================

    @GetMapping
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getAllPermissions(
            @RequestParam(required = false) String modulo,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.getAllPermissions(pageable, modulo);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> getActivePermissions(
            @RequestParam(required = false) String modulo,
            @PageableDefault(page = 0, size = 20, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.getActivePermissions(pageable, modulo);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Permisos activos obtenidos exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> searchPermissions(
            @RequestParam String q,
            @RequestParam(required = false, defaultValue = "all") String by,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.searchPermissions(q, by, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<PermissionResponse>>> searchByFilter(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @RequestParam(required = false) String modulo,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        PageResponse<PermissionResponse> response = permissionService.searchByFilter(query, type, pageable, modulo);

        return ResponseEntity.ok(ApiResponse.<PageResponse<PermissionResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    // ========================================
    // LISTADOS SIN PAGINACIÓN
    // ========================================

    @GetMapping("/list")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissionsList() {
        List<PermissionResponse> permissions = permissionService.getAllPermissionsAsList();
        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    @GetMapping("/modules")
    @PreAuthorize("hasAuthority('PERMISO_LEER')")
    public ResponseEntity<ApiResponse<List<String>>> getAllModules() {
        List<String> modules = permissionService.getAllModules();
        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Módulos obtenidos exitosamente")
                .data(modules)
                .build());
    }
}