package com.pe.finanzas.users.controller;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.permissions.dto.PermissionResponse;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.users.dto.AssignDirectPermissionsRequest;
import com.pe.finanzas.users.dto.GrantDirectPermissionRequest;
import com.pe.finanzas.users.dto.TogglePermissionModeRequest;
import com.pe.finanzas.users.dto.UserDirectPermissionResponse;
import com.pe.finanzas.users.dto.UserWithPermissionsResponse;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.service.UserPermissionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserPermissionController {

    private final UserPermissionService userPermissionService;

    // ========================================
    // ASIGNACIÓN DE PERMISOS DIRECTOS
    // ========================================

    /**
     * Asignar múltiples permisos directos a un usuario
     * POST /api/users/permissions/assign
     */
    @PostMapping("/permissions/assign")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> assignDirectPermissions(
            @Valid @RequestBody AssignDirectPermissionsRequest request,
            Authentication authentication) {

        Long currentUserId = getCurrentUserId(authentication);
        UserWithPermissionsResponse user = userPermissionService.assignDirectPermissions(
                request, currentUserId);

        return ResponseEntity.ok(ApiResponse.<UserWithPermissionsResponse>builder()
                .success(true)
                .message("Permisos directos asignados exitosamente")
                .data(user)
                .build());
    }

    /**
     * Agregar un permiso directo a un usuario
     * POST /api/users/permissions/grant
     */
    @PostMapping("/permissions/grant")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserDirectPermissionResponse>> grantDirectPermission(
            @Valid @RequestBody GrantDirectPermissionRequest request,
            Authentication authentication) {

        Long currentUserId = getCurrentUserId(authentication);
        UserDirectPermissionResponse permission = userPermissionService
                .grantDirectPermission(request, currentUserId);

        return ResponseEntity.ok(ApiResponse.<UserDirectPermissionResponse>builder()
                .success(true)
                .message("Permiso directo otorgado exitosamente")
                .data(permission)
                .build());
    }

    /**
     * Remover permisos directos específicos
     * DELETE /api/users/{id}/permissions
     */
    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> removeDirectPermissions(
            @PathVariable Long id,
            @RequestBody List<Long> permissionIds) {

        // Validaciones
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
        }

        UserWithPermissionsResponse user = userPermissionService.removeDirectPermissions(
                id, permissionIds);

        return ResponseEntity.ok(ApiResponse.<UserWithPermissionsResponse>builder()
                .success(true)
                .message("Permisos removidos exitosamente")
                .data(user)
                .build());
    }

    /**
     * Revocar un permiso directo específico
     * DELETE /api/users/{id}/permissions/{permissionId}
     */
    @DeleteMapping("/{id}/permissions/{permissionId}")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> revokeDirectPermission(
            @PathVariable Long id,
            @PathVariable Long permissionId) {

        // Validaciones
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (permissionId == null || permissionId <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        userPermissionService.revokeDirectPermission(id, permissionId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Permiso revocado exitosamente")
                .build());
    }

    /**
     * Limpiar todos los permisos directos de un usuario
     * DELETE /api/users/{id}/permissions/all
     */
    @DeleteMapping("/{id}/permissions/all")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Void>> clearAllDirectPermissions(
            @PathVariable Long id) {

        // Validación
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        userPermissionService.clearAllDirectPermissions(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Todos los permisos directos eliminados exitosamente")
                .build());
    }

    // ========================================
    // CONSULTAS
    // ========================================

    /**
     * Obtener permisos directos de un usuario
     * GET /api/users/{id}/permissions/direct
     */
    @GetMapping("/{id}/permissions/direct")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<UserDirectPermissionResponse>>> getDirectPermissions(
            @PathVariable Long id) {

        // Validación
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        List<UserDirectPermissionResponse> permissions = userPermissionService
                .getDirectPermissions(id);

        return ResponseEntity.ok(ApiResponse.<List<UserDirectPermissionResponse>>builder()
                .success(true)
                .message("Permisos directos obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    /**
     * Obtener permisos efectivos de un usuario
     * GET /api/users/{id}/permissions/effective
     */
    @GetMapping("/{id}/permissions/effective")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getEffectivePermissions(
            @PathVariable Long id) {

        // Validación
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        List<PermissionResponse> permissions = userPermissionService
                .getEffectivePermissions(id);

        return ResponseEntity.ok(ApiResponse.<List<PermissionResponse>>builder()
                .success(true)
                .message("Permisos efectivos obtenidos exitosamente")
                .data(permissions)
                .build());
    }

    /**
     * Obtener información completa del usuario con permisos
     * GET /api/users/{id}/with-permissions
     */
    @GetMapping("/{id}/with-permissions")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> getUserWithPermissions(
            @PathVariable Long id) {

        // Validación
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        UserWithPermissionsResponse user = userPermissionService.getUserWithPermissions(id);

        return ResponseEntity.ok(ApiResponse.<UserWithPermissionsResponse>builder()
                .success(true)
                .message("Usuario con permisos obtenido exitosamente")
                .data(user)
                .build());
    }

    // ========================================
    // MODO DE PERMISOS
    // ========================================

    /**
     * Cambiar entre usar rol o permisos directos
     * PUT /api/users/permissions/toggle-mode
     */
    @PutMapping("/permissions/toggle-mode")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> togglePermissionMode(
            @Valid @RequestBody TogglePermissionModeRequest request) {

        // Validación adicional
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        UserWithPermissionsResponse user = userPermissionService.togglePermissionMode(
                request.getUserId(),
                request.getUseDirectPermissions());

        String mode = request.getUseDirectPermissions() ? "DIRECTO" : "ROL";

        return ResponseEntity.ok(ApiResponse.<UserWithPermissionsResponse>builder()
                .success(true)
                .message("Modo de permisos actualizado a: " + mode)
                .data(user)
                .build());
    }

    /**
     * Verificar si un usuario tiene un permiso específico
     * GET /api/users/{id}/has-permission/{permissionName}
     */
    @GetMapping("/{id}/has-permission/{permissionName}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> hasPermission(
            @PathVariable Long id,
            @PathVariable String permissionName) {

        // Validaciones
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del permiso no puede estar vacío");
        }

        boolean hasPermission = userPermissionService.hasPermission(id, permissionName);

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Verificación realizada exitosamente")
                .data(Map.of(
                        "hasPermission", hasPermission,
                        "permissionName", permissionName,
                        "userId", id))
                .build());
    }

    // ========================================
    // MANTENIMIENTO
    // ========================================

    /**
     * Desactivar permisos expirados (tarea de mantenimiento)
     * POST /api/users/permissions/deactivate-expired
     */
    @PostMapping("/permissions/deactivate-expired")
    @PreAuthorize("hasAuthority('PERMISO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> deactivateExpiredPermissions() {

        int count = userPermissionService.deactivateExpiredPermissions();

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message(count + " permisos expirados desactivados")
                .data(Map.of(
                        "deactivatedCount", count,
                        "timestamp", java.time.LocalDateTime.now()))
                .build());
    }

    /**
     * Obtener permisos que expiran pronto
     * GET /api/users/permissions/expiring?days=7
     */
    @GetMapping("/permissions/expiring")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<UserDirectPermissionResponse>>> getExpiringPermissions(
            @RequestParam(defaultValue = "7") int days) {

        // Validación
        if (days <= 0) {
            throw new BadRequestException("Los días deben ser un número positivo");
        }
        if (days > 365) {
            throw new BadRequestException("El rango máximo es de 365 días");
        }

        List<UserDirectPermissionResponse> permissions = userPermissionService
                .getExpiringPermissions(days);

        return ResponseEntity.ok(ApiResponse.<List<UserDirectPermissionResponse>>builder()
                .success(true)
                .message("Permisos que expiran en los próximos " + days + " días")
                .data(permissions)
                .build());
    }

    // ========================================
    // ENDPOINTS ADICIONALES ÚTILES
    // ========================================

    /**
     * Obtener estadísticas de permisos de un usuario
     * GET /api/users/{id}/permissions/stats
     */
    @GetMapping("/{id}/permissions/stats")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getPermissionStats(
            @PathVariable Long id) {

        // Validación
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        UserWithPermissionsResponse user = userPermissionService.getUserWithPermissions(id);

        Map<String, Object> stats = Map.of(
                "id", user.getId(),
                "login", user.getLogin(),
                "fullName", user.getFullName(),
                "permissionMode", user.getPermissionMode(),
                "useDirectPermissions",
                user.getUseDirectPermissions() != null ? user.getUseDirectPermissions() : false,
                "rolesCount", user.getRoles().size(),
                "rolePermissionsCount", user.getRolePermissions().size(),
                "directPermissionsCount", user.getDirectPermissions().size(),
                "activeDirectPermissionsCount", user.getActiveDirectPermissionsCount(),
                "effectivePermissionsCount", user.getEffectivePermissionsCount());

        return ResponseEntity.ok(ApiResponse.<Map<String, Object>>builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(stats)
                .build());
    }

    /**
     * Copiar permisos directos de un usuario a otro
     * POST /api/users/{sourceId}/permissions/copy-to/{targetId}
     */
    @PostMapping("/{sourceId}/permissions/copy-to/{targetId}")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserWithPermissionsResponse>> copyPermissions(
            @PathVariable Long sourceId,
            @PathVariable Long targetId,
            @RequestParam(required = false) String reason,
            Authentication authentication) {

        // Validaciones
        if (sourceId == null || sourceId <= 0) {
            throw new BadRequestException("El ID del usuario origen debe ser un número positivo");
        }
        if (targetId == null || targetId <= 0) {
            throw new BadRequestException("El ID del usuario destino debe ser un número positivo");
        }
        if (sourceId.equals(targetId)) {
            throw new BadRequestException("El usuario origen y destino no pueden ser el mismo");
        }

        // Obtener permisos del usuario origen
        List<UserDirectPermissionResponse> sourcePermissions = userPermissionService
                .getDirectPermissions(sourceId);

        // Verificar que tenga permisos
        if (sourcePermissions.isEmpty()) {
            throw new BadRequestException("El usuario origen no tiene permisos directos para copiar");
        }

        // Extraer IDs de permisos
        List<Long> permissionIds = sourcePermissions.stream()
                .map(p -> p.getPermission().getId())
                .toList();

        // Crear request
        AssignDirectPermissionsRequest request = new AssignDirectPermissionsRequest();
        request.setUserId(targetId);
        request.setPermissionIds(permissionIds);
        request.setReason(reason != null ? reason : "Copiado de usuario " + sourceId);

        // Asignar
        Long currentUserId = getCurrentUserId(authentication);
        UserWithPermissionsResponse targetUser = userPermissionService
                .assignDirectPermissions(request, currentUserId);

        return ResponseEntity.ok(ApiResponse.<UserWithPermissionsResponse>builder()
                .success(true)
                .message("Permisos copiados exitosamente de usuario " + sourceId + " a " + targetId)
                .data(targetUser)
                .build());
    }

    // ========================================
    // UTILIDADES
    // ========================================

    /**
     * Extrae el ID del usuario autenticado
     */
    private Long getCurrentUserId(Authentication authentication) {
        if (authentication == null) {
            throw new BadRequestException("Usuario no autenticado");
        }

        if (!(authentication.getPrincipal() instanceof DatosPersonales)) {
            throw new BadRequestException("Tipo de autenticación inválido");
        }

        DatosPersonales user = (DatosPersonales) authentication.getPrincipal();

        if (user.getId() == null) {
            throw new BadRequestException("ID de usuario no disponible");
        }

        return user.getId();
    }

    /**
     * Obtener SOLO los nombres de permisos efectivos del usuario
     * GET /api/users/{id}/permissions
     */
    @GetMapping("/{id}/permissions")
    @PreAuthorize("hasAuthority('USUARIO_LEER') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<String>>> getUserPermissionNames(
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);

        List<String> permissionNames = permissions.stream()
                .map(PermissionResponse::getName)
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Permisos del usuario obtenidos exitosamente")
                .data(permissionNames)
                .build());
    }

    /**
     * Obtener módulos con acceso del usuario
     * GET /api/users/{id}/modules
     */
    @GetMapping("/{id}/modules")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<List<String>>> getUserModules(
            @PathVariable Long id) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);

        List<String> modules = permissions.stream()
                .map(PermissionResponse::getModule)
                .filter(module -> module != null && !module.isEmpty())
                .distinct()
                .sorted()
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<String>>builder()
                .success(true)
                .message("Módulos del usuario obtenidos exitosamente")
                .data(modules)
                .build());
    }

    /**
     * Obtener permisos CRUD de un módulo específico
     * GET /api/users/{id}/crud?modulo=USUARIO
     */
    @GetMapping("/{id}/crud")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<Map<String, Boolean>>> getModuleCRUDPermissions(
            @PathVariable Long id,
            @RequestParam String modulo) {

        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (modulo == null || modulo.trim().isEmpty()) {
            throw new BadRequestException("El nombre del módulo no puede estar vacío");
        }

        List<PermissionResponse> permissions = userPermissionService.getEffectivePermissions(id);

        java.util.Set<String> permissionNames = permissions.stream()
                .map(PermissionResponse::getName)
                .collect(java.util.stream.Collectors.toSet());

        Map<String, Boolean> crudPermissions = new java.util.HashMap<>();
        crudPermissions.put("canCreate", permissionNames.contains(modulo + "_CREAR"));
        crudPermissions.put("canRead", permissionNames.contains(modulo + "_LEER"));
        crudPermissions.put("canUpdate", permissionNames.contains(modulo + "_ACTUALIZAR"));
        crudPermissions.put("canDelete", permissionNames.contains(modulo + "_ELIMINAR"));

        return ResponseEntity.ok(ApiResponse.<Map<String, Boolean>>builder()
                .success(true)
                .message("Permisos CRUD del módulo obtenidos exitosamente")
                .data(crudPermissions)
                .build());
    }
}
