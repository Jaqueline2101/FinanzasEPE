package com.pe.finanzas.users.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ConflictException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.permissions.dto.PermissionResponse;
import com.pe.finanzas.permissions.entity.Permission;
import com.pe.finanzas.permissions.repository.PermissionRepository;
import com.pe.finanzas.users.dto.AssignDirectPermissionsRequest;
import com.pe.finanzas.users.dto.GrantDirectPermissionRequest;
import com.pe.finanzas.users.dto.UserDirectPermissionResponse;
import com.pe.finanzas.users.dto.UserWithPermissionsResponse;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.entity.UserPermission;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.users.repository.UserPermissionRepository;
import com.pe.finanzas.users.service.UserPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPermissionServiceImpl implements UserPermissionService {

    private final DatosPersonalesRepository datosPersonalesRepository;
    private final UserPermissionRepository userPermissionRepository;
    private final PermissionRepository permissionRepository;

    // Constantes
    private static final int MAX_PERMISSIONS_PER_USER = 100;
    private static final int MAX_EXPIRATION_DAYS = 365;

    @Override
    @Transactional
    public UserWithPermissionsResponse assignDirectPermissions(
            AssignDirectPermissionsRequest request, Long currentUserId) {

        log.info("Assigning {} direct permissions to user ID: {}",
                request.getPermissionIds().size(), request.getUserId());

        // ========================================
        // VALIDACIONES
        // ========================================

        // Validar ID de usuario
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        // Validar ID de usuario que otorga
        if (currentUserId == null || currentUserId <= 0) {
            throw new BadRequestException("El ID del usuario que otorga el permiso es inválido");
        }

        // Validar lista de permisos
        if (request.getPermissionIds() == null || request.getPermissionIds().isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso");
        }

        // Validar cantidad máxima de permisos
        if (request.getPermissionIds().size() > MAX_PERMISSIONS_PER_USER) {
            throw new BadRequestException(
                    "No se pueden asignar más de " + MAX_PERMISSIONS_PER_USER + " permisos");
        }

        // Validar fecha de expiración si existe
        if (request.getExpiresAt() != null) {
            validateExpirationDate(request.getExpiresAt());
        }

        // Validar razón si se proporciona
        if (request.getReason() != null && request.getReason().length() > 500) {
            throw new BadRequestException("La razón no puede tener más de 500 caracteres");
        }

        // Buscar usuario
        DatosPersonales user = datosPersonalesRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUserId()));

        // Verificar que el usuario esté activo
        if (!user.getActive()) {
            throw new ValidationException("No se pueden asignar permisos a un usuario inactivo");
        }

        // Validar y obtener permisos
        Set<Permission> permissions = validateAndGetPermissions(request.getPermissionIds());

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        // Limpiar permisos directos existentes
        userPermissionRepository.deleteByUserId(user.getId());

        // Crear nuevos permisos directos
        Set<UserPermission> userPermissions = new HashSet<>();
        for (Permission permission : permissions) {
            UserPermission userPermission = new UserPermission();
            userPermission.setUser(user);
            userPermission.setPermission(permission);
            userPermission.setGrantedBy(currentUserId);
            userPermission.setReason(request.getReason());
            userPermission.setExpiresAt(request.getExpiresAt());
            userPermission.setActive(true);

            userPermissions.add(userPermission);
        }

        user.setDirectPermissions(userPermissions);
        user.setUseDirectPermissions(true);

        DatosPersonales saved = datosPersonalesRepository.save(user);

        log.info("Direct permissions assigned successfully to user: {} (Total: {})",
                saved.getLogin(), userPermissions.size());

        return mapToUserWithPermissionsResponse(saved);
    }

    @Override
    @Transactional
    public UserDirectPermissionResponse grantDirectPermission(
            GrantDirectPermissionRequest request, Long currentUserId) {

        log.info("Granting permission {} to user {}", request.getPermissionId(), request.getUserId());

        // ========================================
        // VALIDACIONES
        // ========================================

        // Validar IDs
        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (request.getPermissionId() == null || request.getPermissionId() <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }
        if (currentUserId == null || currentUserId <= 0) {
            throw new BadRequestException("El ID del usuario que otorga el permiso es inválido");
        }

        // Validar fecha de expiración
        if (request.getExpiresAt() != null) {
            validateExpirationDate(request.getExpiresAt());
        }

        // Validar razón
        if (request.getReason() != null && request.getReason().length() > 500) {
            throw new BadRequestException("La razón no puede tener más de 500 caracteres");
        }

        // Buscar usuario
        DatosPersonales user = datosPersonalesRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", request.getUserId()));

        // Verificar que el usuario esté activo
        if (!user.getActive()) {
            throw new ValidationException("No se pueden otorgar permisos a un usuario inactivo");
        }

        // Buscar permiso
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permiso", "id",
                        request.getPermissionId()));

        // Verificar que el permiso esté activo
        if (permission.getActive() != null && !permission.getActive()) {
            throw new ValidationException("El permiso está inactivo y no puede ser asignado");
        }

        // Verificar si ya existe
        Optional<UserPermission> existing = userPermissionRepository
                .findByUserIdAndPermissionId(user.getId(), permission.getId());

        if (existing.isPresent()) {
            throw new ConflictException("El usuario ya tiene este permiso directo asignado");
        }

        // Verificar límite de permisos
        long currentPermissionsCount = userPermissionRepository
                .countByUserIdAndActive(user.getId(), true);

        if (currentPermissionsCount >= MAX_PERMISSIONS_PER_USER) {
            throw new ValidationException("El usuario ha alcanzado el límite máximo de " +
                    MAX_PERMISSIONS_PER_USER + " permisos directos");
        }

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        UserPermission userPermission = new UserPermission();
        userPermission.setUser(user);
        userPermission.setPermission(permission);
        userPermission.setGrantedBy(currentUserId);
        userPermission.setReason(request.getReason());
        userPermission.setExpiresAt(request.getExpiresAt());
        userPermission.setActive(true);

        UserPermission saved = userPermissionRepository.save(userPermission);

        // Activar modo de permisos directos si no está activo
        if (user.getUseDirectPermissions() == null || !user.getUseDirectPermissions()) {
            user.setUseDirectPermissions(true);
            datosPersonalesRepository.save(user);
        }

        log.info("Direct permission granted successfully: {} to user: {}",
                permission.getName(), user.getLogin());

        return mapToDirectPermissionResponse(saved);
    }

    @Override
    @Transactional
    public UserWithPermissionsResponse removeDirectPermissions(Long userId, List<Long> permissionIds) {
        log.info("Removing {} direct permissions from user {}", permissionIds.size(), userId);

        // ========================================
        // VALIDACIONES
        // ========================================

        // Validar ID de usuario
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        // Validar lista de permisos
        if (permissionIds == null || permissionIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de permiso para remover");
        }

        // Validar que los IDs sean válidos
        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Los IDs de permisos deben ser números positivos");
            }
        }

        // Buscar usuario
        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar que el usuario tenga permisos directos
        List<UserPermission> currentPermissions = userPermissionRepository.findByUserId(userId);
        if (currentPermissions.isEmpty()) {
            throw new ValidationException("El usuario no tiene permisos directos asignados");
        }

        // Verificar que existan los permisos a remover
        Set<Long> currentPermissionIds = currentPermissions.stream()
                .map(up -> up.getPermission().getId())
                .collect(Collectors.toSet());

        for (Long permissionId : permissionIds) {
            if (!currentPermissionIds.contains(permissionId)) {
                throw new ResourceNotFoundException("Permiso directo", "id", permissionId);
            }
        }

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        userPermissionRepository.deleteByUserIdAndPermissionIdIn(userId, permissionIds);

        // Recargar usuario
        user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        log.info("Direct permissions removed successfully from user: {} (Removed: {})",
                user.getLogin(), permissionIds.size());

        return mapToUserWithPermissionsResponse(user);
    }

    @Override
    @Transactional
    public void revokeDirectPermission(Long userId, Long permissionId) {
        log.info("Revoking permission {} from user {}", permissionId, userId);

        // ========================================
        // VALIDACIONES
        // ========================================

        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (permissionId == null || permissionId <= 0) {
            throw new BadRequestException("El ID del permiso debe ser un número positivo");
        }

        // Verificar que el usuario existe
        if (!datosPersonalesRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", "id", userId);
        }

        // Buscar el permiso directo
        UserPermission userPermission = userPermissionRepository
                .findByUserIdAndPermissionId(userId, permissionId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Permiso directo no encontrado para usuario " + userId + " y permiso "
                                + permissionId));

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        userPermissionRepository.delete(userPermission);

        log.info("Direct permission revoked successfully: {} from user ID: {}",
                userPermission.getPermission().getName(), userId);
    }

    @Override
    @Transactional
    public void clearAllDirectPermissions(Long userId) {
        log.info("Clearing all direct permissions for user {}", userId);

        // ========================================
        // VALIDACIONES
        // ========================================

        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        // Buscar usuario
        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar que tenga permisos directos
        List<UserPermission> permissions = userPermissionRepository.findByUserId(userId);
        if (permissions.isEmpty()) {
            throw new ValidationException("El usuario no tiene permisos directos para limpiar");
        }

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        userPermissionRepository.deleteByUserId(userId);

        user.setUseDirectPermissions(false);
        datosPersonalesRepository.save(user);

        log.info("All direct permissions cleared for user: {} (Cleared: {})",
                user.getLogin(), permissions.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDirectPermissionResponse> getDirectPermissions(Long userId) {
        log.info("Getting direct permissions for user {}", userId);

        // Validar ID
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        // Verificar que el usuario existe
        if (!datosPersonalesRepository.existsById(userId)) {
            throw new ResourceNotFoundException("Usuario", "id", userId);
        }

        List<UserPermission> permissions = userPermissionRepository.findByUserId(userId);

        return permissions.stream()
                .map(this::mapToDirectPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getEffectivePermissions(Long userId) {
        log.info("Getting effective permissions for user {}", userId);

        // Validar ID
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        Set<Permission> effectivePermissions = user.getEffectivePermissions();

        return effectivePermissions.stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserWithPermissionsResponse getUserWithPermissions(Long userId) {
        log.info("Getting user with permissions: {}", userId);

        // Validar ID
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        return mapToUserWithPermissionsResponse(user);
    }

    @Override
    @Transactional
    public UserWithPermissionsResponse togglePermissionMode(Long userId, Boolean useDirectPermissions) {
        log.info("Toggling permission mode for user {} to: {}",
                userId, useDirectPermissions ? "DIRECT" : "ROLE");

        // ========================================
        // VALIDACIONES
        // ========================================

        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (useDirectPermissions == null) {
            throw new BadRequestException("El modo de permisos es requerido");
        }

        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar que el usuario esté activo
        if (!user.getActive()) {
            throw new ValidationException("No se puede cambiar el modo de permisos de un usuario inactivo");
        }

        // Validar cambio a modo ROL
        if (!useDirectPermissions) {
            if (user.getRoles() == null || user.getRoles().isEmpty()) {
                throw new ValidationException(
                        "El usuario no tiene roles asignados. Debe asignar al menos un rol antes de cambiar al modo ROL");
            }
        }

        // Validar cambio a modo DIRECTO
        if (useDirectPermissions) {
            List<UserPermission> directPermissions = userPermissionRepository.findByUserId(userId);
            if (directPermissions.isEmpty()) {
                log.warn("Cambiando a modo DIRECTO sin permisos directos asignados para usuario: {}",
                        userId);
            }
        }

        // Verificar que el modo sea diferente
        if (user.getUseDirectPermissions() != null
                && user.getUseDirectPermissions().equals(useDirectPermissions)) {
            String mode = useDirectPermissions ? "DIRECTO" : "ROL";
            throw new ValidationException("El usuario ya está en modo " + mode);
        }

        // ========================================
        // LÓGICA DE NEGOCIO
        // ========================================

        user.setUseDirectPermissions(useDirectPermissions);
        DatosPersonales saved = datosPersonalesRepository.save(user);

        log.info("Permission mode changed for user: {} to {}",
                saved.getLogin(), useDirectPermissions ? "DIRECT" : "ROLE");

        return mapToUserWithPermissionsResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasPermission(Long userId, String permissionName) {
        // Validaciones
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (permissionName == null || permissionName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del permiso no puede estar vacío");
        }

        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        return user.hasPermission(permissionName);
    }

    @Override
    @Transactional
    public int deactivateExpiredPermissions() {
        log.info("Deactivating expired permissions");

        LocalDateTime now = LocalDateTime.now();
        List<UserPermission> expired = userPermissionRepository.findExpiredPermissions(now);

        int count = 0;
        for (UserPermission up : expired) {
            up.setActive(false);
            userPermissionRepository.save(up);
            count++;
        }

        log.info("{} expired permissions deactivated", count);
        return count;
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserDirectPermissionResponse> getExpiringPermissions(int days) {
        // Validaciones
        if (days <= 0) {
            throw new BadRequestException("Los días deben ser un número positivo");
        }
        if (days > 365) {
            throw new BadRequestException("El rango máximo es de 365 días");
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime future = now.plusDays(days);

        List<UserPermission> expiring = userPermissionRepository
                .findExpiringPermissions(now, future);

        return expiring.stream()
                .map(this::mapToDirectPermissionResponse)
                .collect(Collectors.toList());
    }

    // ========================================
    // MÉTODOS DE VALIDACIÓN PRIVADOS
    // ========================================

    private Set<Permission> validateAndGetPermissions(List<Long> permissionIds) {
        Set<Permission> permissions = new HashSet<>();

        for (Long permissionId : permissionIds) {
            if (permissionId == null || permissionId <= 0) {
                throw new BadRequestException("Los IDs de permisos deben ser números positivos");
            }

            Permission permission = permissionRepository.findById(permissionId)
                    .orElseThrow(() -> new ResourceNotFoundException("Permiso", "id",
                            permissionId));

            // Verificar que el permiso esté activo
            if (permission.getActive() != null && !permission.getActive()) {
                throw new ValidationException(
                        "El permiso '" + permission.getName() + "' está inactivo");
            }

            permissions.add(permission);
        }

        return permissions;
    }

    private void validateExpirationDate(LocalDateTime expiresAt) {
        if (expiresAt == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        // No puede estar en el pasado
        if (expiresAt.isBefore(now)) {
            throw new BadRequestException("La fecha de expiración no puede estar en el pasado");
        }

        // No puede ser más de 1 año en el futuro
        LocalDateTime maxDate = now.plusDays(MAX_EXPIRATION_DAYS);
        if (expiresAt.isAfter(maxDate)) {
            throw new BadRequestException("La fecha de expiración no puede ser mayor a " +
                    MAX_EXPIRATION_DAYS + " días en el futuro");
        }

        // Debe ser al menos 1 día en el futuro
        LocalDateTime minDate = now.plusDays(1);
        if (expiresAt.isBefore(minDate)) {
            throw new BadRequestException("La fecha de expiración debe ser al menos 1 día en el futuro");
        }
    }

    // ========================================
    // MÉTODOS DE MAPEO
    // ========================================

    private UserWithPermissionsResponse mapToUserWithPermissionsResponse(DatosPersonales user) {
        // Permisos de roles
        Set<Permission> allRolePermissions = new HashSet<>();
        if (user.getRoles() != null) {
            user.getRoles().forEach(role -> allRolePermissions.addAll(role.getPermissions()));
        }
        if (user.getUserBranchRoles() != null) {
            user.getUserBranchRoles().stream()
                .filter(ubr -> ubr.getActive() != null && ubr.getActive())
                .forEach(ubr -> allRolePermissions.addAll(ubr.getRole().getPermissions()));
        }
        
        List<PermissionResponse> rolePermissions = allRolePermissions.stream()
                .distinct()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        // Permisos directos
        List<UserDirectPermissionResponse> directPermissions = user.getDirectPermissions().stream()
                .map(this::mapToDirectPermissionResponse)
                .collect(Collectors.toList());

        // Permisos efectivos
        List<PermissionResponse> effectivePermissions = user.getEffectivePermissions().stream()
                .map(this::mapToPermissionResponse)
                .collect(Collectors.toList());

        return UserWithPermissionsResponse.builder()
                .id(user.getId())
                .login(user.getLogin())
                .nombre(user.getNombre())
                .apepat(user.getApepat())
                .apemat(user.getApemat())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .active(user.getActive())
                .roles(user.getRoleNames())
                .rolePermissions(rolePermissions)
                .useDirectPermissions(user.getUseDirectPermissions())
                .permissionMode(user.getPermissionMode())
                .directPermissions(directPermissions)
                .activeDirectPermissionsCount(user.countActiveDirectPermissions())
                .effectivePermissions(effectivePermissions)
                .effectivePermissionsCount((long) effectivePermissions.size())
                .createdAt(user.getCreatedAt())
                .lastLogin(user.getLastLogin())
                .build();
    }

    private UserDirectPermissionResponse mapToDirectPermissionResponse(UserPermission up) {
        boolean isExpired = up.getExpiresAt() != null &&
                up.getExpiresAt().isBefore(LocalDateTime.now());

        Long daysUntilExpiration = null;
        if (up.getExpiresAt() != null && !isExpired) {
            daysUntilExpiration = ChronoUnit.DAYS.between(LocalDateTime.now(), up.getExpiresAt());
        }

        String grantedByLogin = null;
        String grantedByFullName = null;
        if (up.getGrantedBy() != null) {
            Optional<DatosPersonales> grantedByUser = datosPersonalesRepository
                    .findById(up.getGrantedBy());
            if (grantedByUser.isPresent()) {
                grantedByLogin = grantedByUser.get().getLogin();
                grantedByFullName = grantedByUser.get().getFullName();
            }
        }

        return UserDirectPermissionResponse.builder()
                .id(up.getId())
                .permission(mapToPermissionResponse(up.getPermission()))
                .grantedBy(up.getGrantedBy())
                .grantedByLogin(grantedByLogin)
                .grantedByFullName(grantedByFullName)
                .grantedAt(up.getGrantedAt())
                .reason(up.getReason())
                .expiresAt(up.getExpiresAt())
                .active(up.getActive())
                .isExpired(isExpired)
                .daysUntilExpiration(daysUntilExpiration)
                .build();
    }

    private PermissionResponse mapToPermissionResponse(Permission permission) {
        return PermissionResponse.builder()
                .id(permission.getId())
                .name(permission.getName())
                .description(permission.getDescription())
                .module(permission.getModule())
                .active(permission.getActive())
                .createdAt(permission.getCreatedAt())
                .build();
    }
}
