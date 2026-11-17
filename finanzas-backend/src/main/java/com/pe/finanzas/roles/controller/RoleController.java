package com.pe.finanzas.roles.controller;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.roles.dto.RoleRequest;
import com.pe.finanzas.roles.dto.RoleResponse;
import com.pe.finanzas.roles.service.RoleService;
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
@RequestMapping("/api/roles")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class RoleController {

    private final RoleService roleService;

    /**
     * Crear un nuevo rol con validación de permisos ROL_CREAR
     */
    @PostMapping
    @PreAuthorize("hasAuthority('ROL_CREAR')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody RoleRequest request) {
        RoleResponse role = roleService.createRole(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<RoleResponse>builder()
                        .success(true)
                        .message("Rol creado exitosamente")
                        .data(role)
                        .build());
    }

    /**
     * Actualizar rol con validación de ID positivo
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable Long id,
            @Valid @RequestBody RoleRequest request) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }

        RoleResponse role = roleService.updateRole(id, request);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Rol actualizado exitosamente")
                .data(role)
                .build());
    }

    /**
     * Búsqueda por Nombre con validación de longitud mínima
     */
    @GetMapping("/name/{name}")
    @PreAuthorize("hasAuthority('ROL_LEER')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleByName(@PathVariable String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }
        if (name.trim().length() < 3) {
            throw new BadRequestException("El nombre del rol debe tener al menos 3 caracteres");
        }

        RoleResponse role = roleService.getRoleByName(name);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Rol obtenido exitosamente")
                .data(role)
                .build());
    }

    /**
     * Listado paginado (Indispensable para proyectos grandes)
     */
    @GetMapping
    @PreAuthorize("hasAuthority('ROL_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<RoleResponse>>> getAllRoles(
            @RequestParam(required = false) String modulo,
            @PageableDefault(page = 0, size = 10, sort = "name") Pageable pageable) {
        PageResponse<RoleResponse> response = roleService.getAllRoles(pageable, modulo);
        return ResponseEntity.ok(ApiResponse.<PageResponse<RoleResponse>>builder()
                .success(true)
                .message("Roles obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Asignación Masiva de Permisos con Límite de Seguridad
     */
    @PostMapping("/{roleId}/permissions")
    @PreAuthorize("hasAuthority('ROL_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<RoleResponse>> assignPermissions(
            @PathVariable Long roleId,
            @RequestBody List<Long> permissionIds) {

        if (roleId == null || roleId <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
        }
        // Límite de seguridad: evita sobrecargar la BD con peticiones gigantes
        if (permissionIds.size() > 100) {
            throw new BadRequestException("No se pueden asignar más de 100 permisos a la vez");
        }

        RoleResponse role = roleService.assignPermissions(roleId, permissionIds);
        return ResponseEntity.ok(ApiResponse.<RoleResponse>builder()
                .success(true)
                .message("Permisos asignados exitosamente al rol")
                .data(role)
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROL_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable Long id) {
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del rol debe ser un número positivo");
        }
        roleService.deleteRole(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Rol eliminado exitosamente")
                .build());
    }
}