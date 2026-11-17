package com.pe.finanzas.users.controller;

import java.util.List;

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

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.shared.dto.ApiResponse;
import com.pe.finanzas.shared.dto.PageResponse;
import com.pe.finanzas.users.dto.ChangePasswordRequest;
import com.pe.finanzas.users.dto.CreateUserRequest;
import com.pe.finanzas.users.dto.UpdateUserRequest;
import com.pe.finanzas.users.dto.UserResponse;
import com.pe.finanzas.users.dto.UserStatsResponse;
import com.pe.finanzas.users.service.UserService;
import com.pe.finanzas.sucursales.dto.SucursalDto;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserController {

    private final UserService userService;

    /**
     * Crear un nuevo usuario
     * POST /api/users
     */
    @PostMapping
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse user = userService.createUser(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Usuario creado exitosamente")
                        .data(user)
                        .build());
    }

    /**
     * Promover un empleado a usuario (asignar credenciales)
     * POST /api/users/{id}/promote
     */
    @PostMapping("/{id}/promote")
    @PreAuthorize("hasAuthority('USUARIO_CREAR')")
    public ResponseEntity<ApiResponse<UserResponse>> promoteToUser(
            @PathVariable Long id,
            @Valid @RequestBody CreateUserRequest request) {

        UserResponse user = userService.promoteToUser(id, request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.<UserResponse>builder()
                        .success(true)
                        .message("Empleado promovido a Usuario exitosamente")
                        .data(user)
                        .build());
    }

    /**
     * Actualizar un usuario existente
     * PUT /api/users/{id}
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UpdateUserRequest request) {

        UserResponse user = userService.updateUser(id, request);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Usuario actualizado exitosamente")
                .data(user)
                .build());
    }

    /**
     * Obtener un usuario por ID
     * GET /api/users/{id}
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {

        UserResponse user = userService.getUserById(id);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Usuario obtenido exitosamente")
                .data(user)
                .build());
    }

    /**
     * Listar todos los usuarios con paginación
     * GET /api/users
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {

        PageResponse<UserResponse> response = userService.getAllUsers(pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Usuarios obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Listar solo usuarios activos
     * GET /api/users/active
     */
    @GetMapping("/active")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getActiveUsers(
            @PageableDefault(page = 0, size = 10, sort = "nombre") Pageable pageable) {

        PageResponse<UserResponse> response = userService.getActiveUsers(pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Usuarios activos obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Listar usuarios por rol
     * GET /api/users/role/{roleName}
     */
    @GetMapping("/role/{roleName}")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> getUsersByRole(
            @PathVariable String roleName,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // Validar que el nombre del rol no esté vacío
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }

        PageResponse<UserResponse> response = userService.getUsersByRole(roleName, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Usuarios con rol " + roleName + " obtenidos exitosamente")
                .data(response)
                .build());
    }

    /**
     * Buscar personas para ventas (incluye pacientes, busca por nombre, DNI, empresa, compañía)
     * GET /api/users/buscar-personas?q=Juan
     */
    @GetMapping("/buscar-personas")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> buscarPersonas(
            @RequestParam String q,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }
        if (q.trim().length() < 2) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres");
        }

        PageResponse<UserResponse> response = userService.buscarPersonas(q, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    /**
     * Buscar usuarios por nombre o DNI
     * GET /api/users/search?q=Juan
     */
    @GetMapping("/search")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchUsers(
            @RequestParam String q,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        // Validar que el término de búsqueda no esté vacío
        if (q == null || q.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }

        // Validar longitud mínima
        if (q.trim().length() < 2) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres");
        }

        PageResponse<UserResponse> response = userService.searchByName(q, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    /**
     * Filtro avanzado de usuarios por tipo
     * GET /api/users/filter?query=juan&type=NOMBRE
     */
    @GetMapping("/filter")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<PageResponse<UserResponse>>> searchByFilter(
            @RequestParam(required = false, defaultValue = "") String query,
            @RequestParam(required = false, defaultValue = "ALL") String type,
            @PageableDefault(page = 0, size = 10) Pageable pageable) {

        PageResponse<UserResponse> response = userService.searchByFilter(query, type, pageable);

        return ResponseEntity.ok(ApiResponse.<PageResponse<UserResponse>>builder()
                .success(true)
                .message("Búsqueda realizada exitosamente")
                .data(response)
                .build());
    }

    /**
     * Eliminar un usuario (soft delete)
     * DELETE /api/users/{id}
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USUARIO_ELIMINAR')")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Usuario eliminado exitosamente")
                .build());
    }

    /**
     * Activar/Desactivar un usuario
     * PATCH /api/users/{id}/status?active=true
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('USUARIO_BLOQUEAR')")
    public ResponseEntity<ApiResponse<Void>> toggleUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {

        userService.toggleUserStatus(id, active);

        String message = active ? "Usuario activado exitosamente" : "Usuario desactivado exitosamente";

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message(message)
                .build());
    }

    /**
     * Asignar roles a un usuario
     * POST /api/users/{id}/roles
     * Body: [1, 2, 3] (IDs de roles)
     */
    @PostMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {

        // Validar que la lista de roles no esté vacía
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol");
        }

        // Validar que los IDs sean válidos
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
        }

        UserResponse user = userService.assignRoles(id, roleIds);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Roles asignados exitosamente")
                .data(user)
                .build());
    }

    /**
     * Remover roles de un usuario
     * DELETE /api/users/{id}/roles
     * Body: [1, 2] (IDs de roles a remover)
     */
    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removeRoles(
            @PathVariable Long id,
            @RequestBody List<Long> roleIds) {

        // Validar que la lista de roles no esté vacía
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol para remover");
        }

        // Validar que los IDs sean válidos
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
        }

        UserResponse user = userService.removeRoles(id, roleIds);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Roles removidos exitosamente")
                .data(user)
                .build());
    }

    /**
     * Cambiar contraseña de un usuario
     * POST /api/users/{id}/change-password
     */
    @PostMapping("/{id}/change-password")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody ChangePasswordRequest request) {

        // Validar que las contraseñas nuevas coincidan
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Las contraseñas nuevas no coinciden");
        }

        // Validar que la nueva contraseña sea diferente a la antigua
        if (request.getOldPassword().equals(request.getNewPassword())) {
            throw new BadRequestException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        userService.changePassword(id, request.getOldPassword(), request.getNewPassword());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Contraseña cambiada exitosamente")
                .build());
    }

    /**
     * Reseteo administrativo de contraseña
     * POST /api/users/{id}/reset-password
     */
    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasAuthority('USUARIO_PASSWORD_RESET')")
    public ResponseEntity<ApiResponse<Void>> resetPassword(
            @PathVariable Long id,
            @Valid @RequestBody com.pe.finanzas.users.dto.ResetPasswordRequest request) {

        userService.resetPassword(id, request.getNewPassword(), request.getMustChangePassword());

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(true)
                .message("Contraseña reseteada exitosamente por el administrador")
                .build());
    }

    // --- GESTIÓN DE SUCURSALES ASIGNADAS (igual que roles) ---

    @PostMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> assignSucursales(
            @PathVariable Long id,
            @RequestBody List<Long> sucursalIds) {

        UserResponse user = userService.assignSucursales(id, sucursalIds);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message(sucursalIds.size() + " sucursales asignadas exitosamente")
                .data(user)
                .build());
    }

    @DeleteMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR')")
    public ResponseEntity<ApiResponse<UserResponse>> removeSucursales(
            @PathVariable Long id,
            @RequestBody List<Long> sucursalIds) {

        UserResponse user = userService.removeSucursales(id, sucursalIds);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Sucursales removidas exitosamente")
                .data(user)
                .build());
    }

    @GetMapping("/{id}/sucursales")
    @PreAuthorize("hasAuthority('USUARIO_LEER') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<List<SucursalDto>>> getSucursalesAsignadas(
            @PathVariable Long id) {

        List<SucursalDto> sucursales = userService.getSucursalesAsignadas(id);

        return ResponseEntity.ok(ApiResponse.<List<SucursalDto>>builder()
                .success(true)
                .data(sucursales)
                .build());
    }

    /**
     * Subir foto de perfil
     * POST /api/users/{id}/photo
     */
    @PostMapping("/{id}/photo")
    @PreAuthorize("hasAuthority('USUARIO_ACTUALIZAR') or #id == authentication.principal.id")
    public ResponseEntity<ApiResponse<UserResponse>> uploadPhoto(
            @PathVariable Long id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {

        UserResponse user = userService.updateProfilePhoto(id, file);

        return ResponseEntity.ok(ApiResponse.<UserResponse>builder()
                .success(true)
                .message("Foto de perfil actualizada exitosamente")
                .data(user)
                .build());
    }

    /**
     * Verificar si un DNI ya existe
     * GET /api/users/check-numdoc?numdoc=12345678
     */
    @GetMapping("/check-numdoc")
    public ResponseEntity<ApiResponse<Boolean>> checkNumdocExists(@RequestParam String numdoc) {
        boolean exists = userService.existsByNumdoc(numdoc);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "El DNI ya está registrado" : "DNI disponible")
                .data(exists)
                .build());
    }

    /**
     * Verificar si un Email ya existe
     * GET /api/users/check-email?email=test@test.com
     */
    @GetMapping("/check-email")
    public ResponseEntity<ApiResponse<Boolean>> checkEmailExists(@RequestParam String email) {
        boolean exists = userService.existsByEmail(email);
        return ResponseEntity.ok(ApiResponse.<Boolean>builder()
                .success(true)
                .message(exists ? "El email ya está registrado" : "Email disponible")
                .data(exists)
                .build());
    }

    /**
     * Obtener estadísticas de usuarios
     * GET /api/users/stats
     */
    @GetMapping("/stats")
    @PreAuthorize("hasAuthority('USUARIO_LEER')")
    public ResponseEntity<ApiResponse<UserStatsResponse>> getUserStats() {
        UserStatsResponse stats = userService.getUserStats();
        return ResponseEntity.ok(ApiResponse.<UserStatsResponse>builder()
                .success(true)
                .message("Estadísticas obtenidas exitosamente")
                .data(stats)
                .build());
    }
}
