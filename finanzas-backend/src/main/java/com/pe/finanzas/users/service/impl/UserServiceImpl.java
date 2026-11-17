package com.pe.finanzas.users.service.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ConflictException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.users.dto.CreateUserRequest;
import com.pe.finanzas.users.dto.UpdateUserRequest;
import com.pe.finanzas.users.dto.UserResponse;
import com.pe.finanzas.users.dto.UserStatsResponse;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.users.service.UserService;
import org.springframework.data.domain.Page;
import com.pe.finanzas.security.util.HttpUtils;
import com.pe.finanzas.reports.audit.service.AuditService;
import com.pe.finanzas.shared.dto.PageResponse;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserServiceImpl implements UserService {

    private final DatosPersonalesRepository datosPersonalesRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;
    private final HttpServletRequest httpRequest;
    private final com.pe.finanzas.sucursales.repository.SucursalRepository sucursalRepository;
    private final com.pe.finanzas.grupo_familiar.repository.GrupoFamiliarRepository grupoFamiliarRepository;
    private final com.pe.finanzas.empresa.repository.EmpresaRepository empresaRepository;
    private final com.pe.finanzas.empresa.repository.EmpresaPersonaVinculoRepository empresaPersonaVinculoRepository;
    private final com.pe.finanzas.compania.repository.CompaniaRepository companiaRepository;
    private final com.pe.finanzas.compania.repository.CompaniaPersonaVinculoRepository companiaPersonaVinculoRepository;
    private final com.pe.finanzas.paciente.repository.DatosPacienteRepository datosPacienteRepository;

    // Constantes para validaciones
    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final int MIN_USERNAME_LENGTH = 3;
    private static final int MAX_USERNAME_LENGTH = 30;

    @Override
    public UserResponse createUser(CreateUserRequest request) {
        log.info("Creating user: {}", request.getUsername());

        // ========================================
        // VALIDACIONES DE NEGOCIO
        // ========================================

        // Validar username
        validateUsername(request.getUsername());

        // Validar email
        validateEmail(request.getEmail());

        // Validar password
        validatePassword(request.getPassword());

        // Validar que el login (username) no exista
        if (datosPersonalesRepository.existsByLogin(request.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
        }

        // Validar que el email no exista
        if (datosPersonalesRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
        }

        // Validar nombre y apellido
        if (request.getFirstName() == null || request.getFirstName().trim().isEmpty()) {
            throw new BadRequestException("El nombre es obligatorio");
        }
        if (request.getLastName() == null || request.getLastName().trim().isEmpty()) {
            throw new BadRequestException("El apellido paterno es obligatorio");
        }

        // Validar roles si se proporcionan
        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = validateAndGetRoles(request.getRoleIds());
        } else {
            // Si no se especifican roles, asignar rol por defecto (PACIENTE)
            // Esto asegura que registros desde el frontend de ventas sean pacientes
            Role defaultRole = roleRepository.findFirstByName("PACIENTE")
                    .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto 'PACIENTE' no encontrado"));
            roles.add(defaultRole);
        }

        // ========================================
        // CREACIÓN DEL USUARIO
        // ========================================

        DatosPersonales datosPersonales = DatosPersonales.builder()
                .login(request.getUsername().trim())
                .email(request.getEmail().trim().toLowerCase())
                .passwd(passwordEncoder.encode(request.getPassword()))
                .nombre(capitalizeFirstLetter(request.getFirstName().trim()))
                .apepat(request.getPaternalSurname() != null ? capitalizeFirstLetter(request.getPaternalSurname().trim())
                        : capitalizeFirstLetter(request.getLastName() != null ? request.getLastName().trim() : ""))
                .apemat(request.getMaternalSurname() != null ? capitalizeFirstLetter(request.getMaternalSurname().trim()) : null)
                .tipodoc(request.getTipodoc())
                .numdoc(request.getNumdoc())
                .nacfec(request.getNacfec())
                .sexo(request.getSexo())
                .direcc(request.getDirecc())
                .fonLocal(request.getPhone() != null ? request.getPhone().trim() : null)
                .active(true)
                .roles(roles)
                .build();

        // NUEVO: Asignar Sucursal Principal por defecto si el usuario no es admin
        boolean isAdmin = roles.stream().anyMatch(r -> "ADMINISTRADOR".equals(r.getName()));

        if (!isAdmin) {
            sucursalRepository.findByNombreSucursal("Sede Principal").ifPresent(sucursal -> {
                datosPersonales.setSucursalesAsignadas(new HashSet<>());
                datosPersonales.getSucursalesAsignadas().add(sucursal);
                datosPersonales.setSucursalActual(sucursal);
            });
        }

        DatosPersonales savedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("User created successfully: {} (ID: {})", savedUser.getLogin(), savedUser.getId());

        // Si el usuario tiene rol PACIENTE, crear registro en datos_paciente
        if (roles.stream().anyMatch(r -> "PACIENTE".equals(r.getName()))) {
            try {
                var datosPaciente = com.pe.finanzas.paciente.entity.DatosPaciente.builder()
                        .datosPersonales(savedUser)
                        .esAsegurado(false)
                        .build();
                datosPacienteRepository.save(datosPaciente);
                log.info("DatosPaciente creado para usuario: {}", savedUser.getLogin());
            } catch (Exception e) {
                log.warn("No se pudo crear DatosPaciente: {}", e.getMessage());
            }
        }

        // ========================================
        // AUDITORÍA
        // ========================================
        logAuditAction(
                "USER_CREATE",
                "Usuario creado: " + savedUser.getLogin() + " (Email: " + savedUser.getEmail() + ") - Roles: "
                        + roles.size(),
                savedUser.getId(),
                "/api/users",
                "POST");

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse promoteToUser(Long id, CreateUserRequest request) {
        log.info("Promoting employee to user: {} (ID: {})", request.getUsername(), id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del empleado debe ser un número positivo");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Empleado", "id", id));

        // Validar username
        validateUsername(request.getUsername());

        // Validar email
        validateEmail(request.getEmail());

        // Validar password
        validatePassword(request.getPassword());

        // Validar que el login (username) no exista (excluyendo el actual si por alguna
        // razón ya tenía)
        if (datosPersonales.getLogin() != null && !datosPersonales.getLogin().equals(request.getUsername()) &&
                datosPersonalesRepository.existsByLogin(request.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
        } else if (datosPersonales.getLogin() == null
                && datosPersonalesRepository.existsByLogin(request.getUsername())) {
            throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
        }

        // Validar que el email no exista
        if (datosPersonales.getEmail() != null && !datosPersonales.getEmail().equals(request.getEmail()) &&
                datosPersonalesRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
        } else if (datosPersonales.getEmail() == null && datosPersonalesRepository.existsByEmail(request.getEmail())) {
            throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
        }

        // Actualizar datos
        datosPersonales.setLogin(request.getUsername().trim());
        datosPersonales.setEmail(request.getEmail().trim().toLowerCase());
        datosPersonales.setPasswd(passwordEncoder.encode(request.getPassword()));

        // Actualizar nombre/apellido si se envían (opcional, pero el formulario los
        // manda)
        if (request.getFirstName() != null)
            datosPersonales.setNombre(capitalizeFirstLetter(request.getFirstName().trim()));
        if (request.getPaternalSurname() != null)
            datosPersonales.setApepat(capitalizeFirstLetter(request.getPaternalSurname().trim()));
        if (request.getMaternalSurname() != null)
            datosPersonales.setApemat(capitalizeFirstLetter(request.getMaternalSurname().trim()));
        if (request.getPhone() != null)
            datosPersonales.setFonLocal(request.getPhone().trim());

        // Asignar roles
        Set<Role> roles = new HashSet<>();
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            roles = validateAndGetRoles(request.getRoleIds());
        } else {
            Role defaultRole = roleRepository.findFirstByName("USUARIO")
                    .orElseThrow(() -> new ResourceNotFoundException("Rol por defecto 'USUARIO' no encontrado"));
            roles.add(defaultRole);
        }
        datosPersonales.setRoles(roles);

        // Activar usuario
        datosPersonales.setActive(true);

        // Asignar Sucursal Principal si aplica
        boolean isAdmin = roles.stream().anyMatch(r -> "ADMINISTRADOR".equals(r.getName()));
        if (!isAdmin && (datosPersonales.getSucursalesAsignadas() == null
                || datosPersonales.getSucursalesAsignadas().isEmpty())) {
            sucursalRepository.findByNombreSucursal("Sede Principal").ifPresent(sucursal -> {
                if (datosPersonales.getSucursalesAsignadas() == null)
                    datosPersonales.setSucursalesAsignadas(new HashSet<>());
                datosPersonales.getSucursalesAsignadas().add(sucursal);
                datosPersonales.setSucursalActual(sucursal);
            });
        }

        DatosPersonales savedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("User promoted successfully: {} (ID: {})", savedUser.getLogin(), savedUser.getId());

        // Auditoría
        logAuditAction(
                "USER_PROMOTE",
                "Empleado promovido a Usuario: " + savedUser.getLogin() + " (ID: " + id + ")",
                id,
                "/api/users/" + id + "/promote",
                "POST");

        return mapToResponse(savedUser);
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        log.info("Updating user with id: {}", id);

        // Validar que el ID sea válido
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // ========================================
        // VALIDACIONES
        // ========================================

        // Validar username si cambió
        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            validateUsername(request.getUsername());
            if (!datosPersonales.getLogin().equals(request.getUsername()) &&
                    datosPersonalesRepository.existsByLogin(request.getUsername())) {
                throw new ConflictException("El nombre de usuario '" + request.getUsername() + "' ya existe");
            }
        }

        // Validar email si cambió
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            validateEmail(request.getEmail());
            if (!datosPersonales.getEmail().equals(request.getEmail()) &&
                    datosPersonalesRepository.existsByEmail(request.getEmail())) {
                throw new ConflictException("El email '" + request.getEmail() + "' ya está registrado");
            }
        }

        // SE ELIMINÓ: La validación de password que estaba aquí

        // ========================================
        // ACTUALIZACIÓN DE CAMPOS
        // ========================================

        if (request.getUsername() != null && !request.getUsername().trim().isEmpty()) {
            datosPersonales.setLogin(request.getUsername().trim());
        }
        if (request.getEmail() != null && !request.getEmail().trim().isEmpty()) {
            datosPersonales.setEmail(request.getEmail().trim().toLowerCase());
        }
        if (request.getFirstName() != null && !request.getFirstName().trim().isEmpty()) {
            datosPersonales.setNombre(capitalizeFirstLetter(request.getFirstName().trim()));
        }
        if (request.getPaternalSurname() != null && !request.getPaternalSurname().trim().isEmpty()) {
            datosPersonales.setApepat(capitalizeFirstLetter(request.getPaternalSurname().trim()));
        }
        if (request.getMaternalSurname() != null) {
            datosPersonales.setApemat(capitalizeFirstLetter(request.getMaternalSurname().trim()));
        }
        if (request.getPhone() != null) {
            datosPersonales.setFonLocal(request.getPhone().trim());
        }

        // SE ELIMINÓ: La actualización de la contraseña (passwordEncoder.encode)

        // Actualizar roles si se proporcionan
        if (request.getRoleIds() != null && !request.getRoleIds().isEmpty()) {
            Set<Role> roles = validateAndGetRoles(request.getRoleIds());
            datosPersonales.setRoles(roles);
        }

        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);
        log.info("User updated successfully: {} (ID: {})", updatedUser.getLogin(), updatedUser.getId());

        // Auditoría
        logAuditAction(
                "USER_UPDATE",
                "Usuario actualizado: " + updatedUser.getLogin() + " (ID: " + id + ")",
                id,
                "/api/users/" + id,
                "PUT");

        return mapToResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        log.info("Getting user by id: {}", id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        return mapToResponse(datosPersonales);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserByLogin(String login) {
        log.info("Getting user by login: {}", login);

        // Validar login
        if (login == null || login.trim().isEmpty()) {
            throw new BadRequestException("El login no puede estar vacío");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findByLogin(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "login", login));

        return mapToResponse(datosPersonales);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getAllUsers(Pageable pageable) {
        log.info("Getting all users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        validatePageable(pageable);

        Long sucursalId = getScopeSucursalId();
        Page<DatosPersonales> userPage;

        if (sucursalId == null) {
            userPage = datosPersonalesRepository.findAllWithRoles(pageable);
        } else {
            userPage = datosPersonalesRepository.findAllBySucursalActualId(sucursalId, pageable);
        }

        return mapToPageResponse(userPage);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getActiveUsers(Pageable pageable) {
        log.info("Getting active users with pagination: page={}, size={}",
                pageable.getPageNumber(), pageable.getPageSize());

        validatePageable(pageable);

        Long sucursalId = getScopeSucursalId();
        Page<DatosPersonales> userPage;

        if (sucursalId == null) {
            userPage = datosPersonalesRepository.findAllActive(pageable);
        } else {
            userPage = datosPersonalesRepository.findAllActiveBySucursalActualId(sucursalId, pageable);
        }

        return mapToPageResponse(userPage);
    }

    @Override
    public void deleteUser(Long id) {
        log.info("Deleting user with id: {}", id);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Verificar que el usuario esté activo
        if (!datosPersonales.getActive()) {
            throw new ValidationException("El usuario ya está inactivo");
        }

        // Verificar que no sea administrador
        if (datosPersonales.hasRole("ADMINISTRADOR")) {
            throw new ValidationException("No se puede eliminar un usuario con rol ADMINISTRADOR");
        }

        // Verificar que no sea el usuario actual
        String currentUsername = HttpUtils.getCurrentUsername();
        if (datosPersonales.getLogin().equals(currentUsername)) {
            throw new ValidationException("No puedes eliminar tu propia cuenta");
        }

        // Soft delete
        datosPersonales.setActive(false);
        datosPersonalesRepository.save(datosPersonales);

        log.info("User deleted (soft delete): {} (ID: {})", datosPersonales.getLogin(), id);

        // Auditoría
        logAuditAction(
                "USER_DELETE",
                "Usuario eliminado (soft delete): " + datosPersonales.getLogin() + " (ID: " + id + ")",
                id,
                "/api/users/" + id,
                "DELETE");
    }

    @Override
    public void toggleUserStatus(Long id, boolean active) {
        log.info("Toggling user status - id: {}, active: {}", id, active);

        // Validar ID
        if (id == null || id <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", id));

        // Verificar que el estado sea diferente
        if (datosPersonales.getActive() == active) {
            String status = active ? "activo" : "inactivo";
            throw new ValidationException("El usuario ya se encuentra " + status);
        }

        // Verificar que no sea administrador al desactivar
        if (datosPersonales.hasRole("ADMINISTRADOR") && !active) {
            throw new ValidationException("No se puede desactivar la cuenta de un ADMINISTRADOR");
        }

        // Verificar que no sea el usuario actual
        String currentUsername = HttpUtils.getCurrentUsername();
        if (datosPersonales.getLogin().equals(currentUsername) && !active) {
            throw new ValidationException("No puedes desactivar tu propia cuenta");
        }

        datosPersonales.setActive(active);
        datosPersonalesRepository.save(datosPersonales);

        log.info("User status changed: {} - active: {}", datosPersonales.getLogin(), active);

        // Auditoría
        logAuditAction(
                "USER_STATUS_CHANGE",
                "Cambio de estado usuario: " + datosPersonales.getLogin() + " a " + (active ? "ACTIVO" : "INACTIVO"),
                id,
                "/api/users/" + id + "/status",
                "PATCH");
    }

    @Override
    public UserResponse assignRoles(Long userId, List<Long> roleIds) {
        log.info("Assigning {} roles to user: {}", roleIds.size(), userId);

        // Validar parámetros
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Validar y obtener roles
        Set<Role> rolesToAdd = validateAndGetRoles(roleIds);

        // Verificar si ya tiene todos los roles
        Set<Long> existingRoleIds = datosPersonales.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<Long> newRoleIds = rolesToAdd.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        if (existingRoleIds.containsAll(newRoleIds)) {
            throw new ValidationException("El usuario ya tiene todos los roles especificados");
        }

        datosPersonales.getRoles().addAll(rolesToAdd);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);

        log.info("Roles assigned successfully to user: {} (Total roles: {})",
                datosPersonales.getLogin(), updatedUser.getRoles().size());

        // Auditoría
        logAuditAction(
                "USER_ROLE_ASSIGN",
                "Roles asignados a usuario " + datosPersonales.getLogin() + ": " + roleIds,
                userId,
                "/api/users/" + userId + "/roles",
                "POST");

        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse removeRoles(Long userId, List<Long> roleIds) {
        log.info("Removing {} roles from user: {}", roleIds.size(), userId);

        // Validar parámetros
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (roleIds == null || roleIds.isEmpty()) {
            throw new BadRequestException("Debe proporcionar al menos un ID de rol para remover");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Validar que el usuario tenga roles
        if (datosPersonales.getRoles().isEmpty()) {
            throw new ValidationException("El usuario no tiene roles asignados");
        }

        // Validar y obtener roles a remover
        Set<Role> rolesToRemove = validateAndGetRoles(roleIds);

        // Verificar que el usuario tenga los roles a remover
        Set<Long> userRoleIds = datosPersonales.getRoles().stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        Set<Long> rolesToRemoveIds = rolesToRemove.stream()
                .map(Role::getId)
                .collect(Collectors.toSet());

        if (!userRoleIds.containsAll(rolesToRemoveIds)) {
            throw new ValidationException("El usuario no tiene todos los roles especificados");
        }

        datosPersonales.getRoles().removeAll(rolesToRemove);

        // Validar que no se quede sin roles
        if (datosPersonales.getRoles().isEmpty()) {
            throw new ValidationException("Un usuario debe tener al menos un rol asignado");
        }

        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);

        log.info("Roles removed successfully from user: {} (Remaining roles: {})",
                datosPersonales.getLogin(), updatedUser.getRoles().size());

        // Auditoría
        logAuditAction(
                "USER_ROLE_REMOVE",
                "Roles removidos de usuario " + datosPersonales.getLogin() + ": " + roleIds,
                userId,
                "/api/users/" + userId + "/roles",
                "DELETE");

        return mapToResponse(updatedUser);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        log.info("Changing password for user: {}", userId);

        // Validar parámetros
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (oldPassword == null || oldPassword.trim().isEmpty()) {
            throw new BadRequestException("La contraseña actual es obligatoria");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }

        // Validar nueva contraseña
        validatePassword(newPassword);

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Verificar contraseña actual
        if (!passwordEncoder.matches(oldPassword, datosPersonales.getPasswd())) {
            throw new ValidationException("La contraseña actual es incorrecta");
        }

        // Validar que la nueva contraseña sea diferente
        if (passwordEncoder.matches(newPassword, datosPersonales.getPasswd())) {
            throw new ValidationException("La nueva contraseña debe ser diferente a la contraseña actual");
        }

        // Cambiar contraseña y resetear flag si existía
        datosPersonales.setPasswd(passwordEncoder.encode(newPassword));
        datosPersonales.setMustChangePassword(false);
        datosPersonalesRepository.save(datosPersonales);

        log.info("Password changed successfully for user: {} (ID: {})", datosPersonales.getLogin(), userId);

        // Auditoría
        logAuditAction(
                "USER_PASSWORD_CHANGE",
                "Cambio de contraseña para usuario: " + datosPersonales.getLogin(),
                userId,
                "/api/users/" + userId + "/change-password",
                "POST");
    }

    @Override
    public void resetPassword(Long userId, String newPassword, boolean mustChangePassword) {
        log.info("Resetting password for user: {} by admin", userId);

        // Validar parámetros
        if (userId == null || userId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BadRequestException("La nueva contraseña es obligatoria");
        }

        // Validar nueva contraseña (longitud mínima para reset puede ser menor o igual)
        if (newPassword.length() < 6) {
            throw new BadRequestException("La contraseña de reseteo debe tener al menos 6 caracteres");
        }

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", "id", userId));

        // Cambiar contraseña y marcar flag de cambio obligatorio
        datosPersonales.setPasswd(passwordEncoder.encode(newPassword));
        datosPersonales.setMustChangePassword(mustChangePassword);
        datosPersonalesRepository.save(datosPersonales);

        log.info("Password reset successfully for user: {} (ID: {})", datosPersonales.getLogin(), userId);

        // Auditoría
        logAuditAction(
                "USER_PASSWORD_RESET",
                "Reseteo de contraseña administrativa para usuario: " + datosPersonales.getLogin() + " (Forzar cambio: " + mustChangePassword + ")",
                userId,
                "/api/users/" + userId + "/reset-password",
                "POST");
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable) {
        log.info("Getting users by role: {} with pagination", roleName);

        // Validar parámetros
        if (roleName == null || roleName.trim().isEmpty()) {
            throw new BadRequestException("El nombre del rol no puede estar vacío");
        }
        validatePageable(pageable);

        // Verificar que el rol exista
        if (!roleRepository.existsByName(roleName)) {
            throw new ResourceNotFoundException("Rol", "nombre", roleName);
        }

        Long sucursalId = getScopeSucursalId();
        Page<DatosPersonales> results;

        if (sucursalId == null) {
            results = datosPersonalesRepository.findByRoleName(roleName, pageable);
        } else {
            // Se requiere nuevo método en repositorio
            results = datosPersonalesRepository.findByRoleNameAndSucursal(roleName, sucursalId, pageable);
        }

        return mapToPageResponse(results);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> buscarPersonas(String searchTerm, Pageable pageable) {
        String cleanSearchTerm = searchTerm.trim();
        log.info("buscarPersonas called with term: {}, pageable: {}", cleanSearchTerm, pageable);

        Set<Long> idsAgregados = new HashSet<>();
        List<UserResponse> resultados = new ArrayList<>();

        // 1. Buscar por DNI exacto si es numérico
        if (cleanSearchTerm.matches("\\d+")) {
            var byDni = datosPersonalesRepository.findByNumdoc(cleanSearchTerm);
            if (byDni.isPresent()) {
                log.info("Found by exact DNI: {}", byDni.get().getNombre());
                resultados.add(mapToResponse(byDni.get()));
                idsAgregados.add(byDni.get().getId());
                return PageResponse.<UserResponse>builder()
                        .content(resultados)
                        .number(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();
            }
        }

        // 2. Buscar en datos_personales (nombre, apellido, DNI, login) - sin filtro de rol, con scope de sucursal
        Long sucursalId = getScopeSucursalId();
        log.info("getScopeSucursalId returned: {}", sucursalId);
        Page<DatosPersonales> pagePersonas;
        if (sucursalId == null) {
            pagePersonas = datosPersonalesRepository.searchPersonasSinFiltroRol(cleanSearchTerm, pageable);
        } else if (sucursalId == -1L) {
            // If user has no sucursal, return empty page for consistency with other functions
            pagePersonas = Page.empty(pageable);
        } else {
            pagePersonas = datosPersonalesRepository.searchPersonasSinFiltroRolAndSucursal(cleanSearchTerm, sucursalId, pageable);
        }
        log.info("searchPersonasSinFiltroRol returned {} results", pagePersonas.getTotalElements());
        for (DatosPersonales p : pagePersonas.getContent()) {
            log.info("Found person: id={}, nombre={}, apepat={}, apemat={}, active={}, sucursalActual={}", 
                p.getId(), p.getNombre(), p.getApepat(), p.getApemat(), p.getActive(), 
                p.getSucursalActual() != null ? p.getSucursalActual().getIdSucursal() : "null");
            if (idsAgregados.add(p.getId())) {
                resultados.add(mapToResponse(p));
            }
        }

        // 3. Buscar en datos_empresa por nombre y agregar personas vinculadas
        List<com.pe.finanzas.empresa.entity.Empresa> empresas = empresaRepository.findTop10ByNombreContainingIgnoreCase(cleanSearchTerm);
        for (com.pe.finanzas.empresa.entity.Empresa emp : empresas) {
            List<com.pe.finanzas.empresa.entity.EmpresaPersonaVinculo> vinculos = empresaPersonaVinculoRepository.findByEmpresaIdEmpresa(emp.getIdEmpresa());
            for (com.pe.finanzas.empresa.entity.EmpresaPersonaVinculo v : vinculos) {
                if (v.getPersonal() != null && idsAgregados.add(v.getPersonal().getId())) {
                    resultados.add(mapToResponse(v.getPersonal()));
                }
            }
        }

        // 4. Buscar en datos_companias por nombre y agregar personas vinculadas
        List<com.pe.finanzas.compania.entity.Compania> companias = companiaRepository
                .findTop10ByRazSolContainingIgnoreCaseOrPersonaBaseRucStartingWithOrNombreContainingIgnoreCase(
                        cleanSearchTerm, cleanSearchTerm, cleanSearchTerm);
        for (com.pe.finanzas.compania.entity.Compania cia : companias) {
            // La compañía misma es una persona (MapsId)
            if (cia.getPersonaBase() != null && idsAgregados.add(cia.getPersonaBase().getId())) {
                resultados.add(mapToResponse(cia.getPersonaBase()));
            }
            // Personas vinculadas a esta compañía
            List<com.pe.finanzas.compania.entity.CompaniaPersonaVinculo> vinculos = companiaPersonaVinculoRepository.findByCompaniaId(cia.getId());
            for (com.pe.finanzas.compania.entity.CompaniaPersonaVinculo v : vinculos) {
                if (v.getPersona() != null && idsAgregados.add(v.getPersona().getId())) {
                    resultados.add(mapToResponse(v.getPersona()));
                }
            }
        }

        log.info("buscarPersonas returning {} results: {}", resultados.size(), resultados);
        return PageResponse.<UserResponse>builder()
                .content(resultados)
                .number(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .totalElements(resultados.size())
                .totalPages(1)
                .last(true)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchByName(String searchTerm, Pageable pageable) {
        log.info("Searching users by term: {} with pagination", searchTerm);

        // Validar parámetros
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            throw new BadRequestException("El término de búsqueda no puede estar vacío");
        }
        if (searchTerm.trim().length() < 2) {
            throw new BadRequestException("El término de búsqueda debe tener al menos 2 caracteres");
        }
        validatePageable(pageable);

        String cleanSearchTerm = searchTerm.trim();

        // Si el término es puramente numérico (DNI), intentamos búsqueda exacta primero
        if (cleanSearchTerm.matches("\\d+")) {
            var byDni = datosPersonalesRepository.findByNumdoc(cleanSearchTerm);
            if (byDni.isPresent()) {
                List<UserResponse> content = Collections.singletonList(mapToResponse(byDni.get()));
                return PageResponse.<UserResponse>builder()
                        .content(content)
                        .number(pageable.getPageNumber())
                        .pageSize(pageable.getPageSize())
                        .totalElements(1)
                        .totalPages(1)
                        .last(true)
                        .build();
            }
        }

        Long sucursalId = getScopeSucursalId();
        Page<DatosPersonales> results;

        if (sucursalId == null) {
            results = datosPersonalesRepository.searchByName(cleanSearchTerm, pageable);
        } else {
            results = datosPersonalesRepository.searchByNameAndSucursal(cleanSearchTerm, sucursalId, pageable);
        }

        return mapToPageResponse(results);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserResponse> searchByFilter(String query, String type, Pageable pageable) {
        Long sucursalId = getScopeSucursalId();
        Page<DatosPersonales> page;

        if (sucursalId == null) {
            // BÚSQUEDA GLOBAL (ADMIN)
            switch (type.toUpperCase()) {
                case "PACIENTE":
                    page = datosPersonalesRepository.searchPacienteAll(query, pageable);
                    break;
                case "NOMBRE":
                    page = datosPersonalesRepository.searchByName(query, pageable);
                    break;
                case "EMAIL":
                    page = datosPersonalesRepository.searchByEmail(query, pageable);
                    break;
                case "ROL":
                    page = datosPersonalesRepository.findActiveByRoleName(query, pageable);
                    break;
                case "LOGIN":
                    page = datosPersonalesRepository.searchByLogin(query, pageable);
                    break;
                default: // "ALL"
                    page = datosPersonalesRepository.searchByAnyTerm(query, pageable);
                    break;
            }
        } else {
            // BÚSQUEDA SCOPED (POR SUCURSAL)
            switch (type.toUpperCase()) {
                case "PACIENTE":
                    page = datosPersonalesRepository.searchPacienteBySucursal(query, sucursalId, pageable);
                    break;
                case "NOMBRE":
                    page = datosPersonalesRepository.searchByNameAndSucursal(query, sucursalId, pageable);
                    break;
                case "EMAIL":
                    page = datosPersonalesRepository.searchByEmailAndSucursal(query, sucursalId, pageable);
                    break;
                case "ROL":
                    page = datosPersonalesRepository.findActiveByRoleNameAndSucursal(query, sucursalId, pageable);
                    break;
                case "LOGIN":
                    page = datosPersonalesRepository.searchByLoginAndSucursal(query, sucursalId, pageable);
                    break;
                default: // "ALL"
                    page = datosPersonalesRepository.searchByAnyTermAndSucursal(query, sucursalId, pageable);
                    break;
            }
        }

        return mapToPageResponse(page);
    }

    private void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BadRequestException("El nombre de usuario es obligatorio");
        }
        if (username.trim().length() < MIN_USERNAME_LENGTH) {
            throw new BadRequestException(
                    "El nombre de usuario debe tener al menos " + MIN_USERNAME_LENGTH + " caracteres");
        }
        if (username.trim().length() > MAX_USERNAME_LENGTH) {
            throw new BadRequestException(
                    "El nombre de usuario no puede tener más de " + MAX_USERNAME_LENGTH + " caracteres");
        }
        if (!username.matches("^[a-zA-Z0-9._-]+$")) {
            throw new BadRequestException(
                    "El nombre de usuario solo puede contener letras, números, puntos, guiones y guiones bajos");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BadRequestException("El email es obligatorio");
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new BadRequestException("El formato del email no es válido");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isEmpty()) {
            throw new BadRequestException("La contraseña es obligatoria");
        }
        if (password.length() < MIN_PASSWORD_LENGTH) {
            throw new BadRequestException("La contraseña debe tener al menos " + MIN_PASSWORD_LENGTH + " caracteres");
        }
        // Validar que tenga al menos una letra y un número
        if (!password.matches(".*[A-Za-z].*") || !password.matches(".*[0-9].*")) {
            throw new BadRequestException("La contraseña debe contener al menos una letra y un número");
        }
    }

    private void validatePageable(Pageable pageable) {
        if (pageable == null) {
            throw new BadRequestException("Los parámetros de paginación son requeridos");
        }
        if (pageable.getPageSize() > 100) {
            throw new BadRequestException("El tamaño de página no puede ser mayor a 100");
        }
    }

    private Set<Role> validateAndGetRoles(Collection<Long> roleIds) {
        Set<Role> roles = new HashSet<>();
        for (Long roleId : roleIds) {
            if (roleId == null || roleId <= 0) {
                throw new BadRequestException("Los IDs de roles deben ser números positivos");
            }
            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol", "id", roleId));
            roles.add(role);
        }
        return roles;
    }

    /**
     * Determina el alcance de la búsqueda basado en el rol del usuario actual.
     * 
     * @return ID de la sucursal actual si el usuario tiene alcance limitado, o null
     *         si tiene alcance global.
     */
    private Long getScopeSucursalId() {
        try {
            String currentUsername = HttpUtils.getCurrentUsername();
            // Para evitar recursión o auditoría interna, buscamos directo
            DatosPersonales currentUser = datosPersonalesRepository.findByLoginWithRoles(currentUsername)
                    .orElse(null);

            if (currentUser == null) {
                return null; // Fallback seguro (o lanzar error)
            }

            // Roles con acceso GLOBAL (Cross-Branch)
            Set<String> globalRoles = Set.of("AREA_INFORMATICA", "DIRECTOR", "CONTADOR_GENERAL", "ADMINISTRADOR", "SUPER_ADMIN", "DTI");

            boolean isGlobal = currentUser.getRoles().stream()
                    .anyMatch(role -> globalRoles.contains(role.getName()));

            if (isGlobal) {
                return null; // Access ALL data
            }

            // Usuarios "Locales" solo ven su sucursal actual
            if (currentUser.getSucursalActual() != null) {
                return currentUser.getSucursalActual().getIdSucursal();
            }

            // Si es local pero no tiene sucursal, no debería ver nada (Safe default)
            return -1L;
        } catch (Exception e) {
            log.error("Error determining user scope", e);
            return -1L; // Safe default on error
        }
    }

    // ========================================
    // MÉTODOS DE UTILIDAD PRIVADOS
    // ========================================

    private String capitalizeFirstLetter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        return text.substring(0, 1).toUpperCase() + text.substring(1).toLowerCase();
    }

    private void logAuditAction(String action, String description, Long resourceId,
            String endpoint, String method) {
        try {
            auditService.logAction(
                    "USUARIOS",
                    action,
                    description,
                    HttpUtils.getCurrentUsername(),
                    resourceId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    endpoint,
                    method);
        } catch (Exception e) {
            log.error("Error logging audit for action: {}", action, e);
        }
    }

    // ========================================
    // MÉTODOS DE MAPEO
    // ========================================

    private PageResponse<UserResponse> mapToPageResponse(Page<DatosPersonales> page) {
        List<UserResponse> content = page.getContent().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());

        return PageResponse.<UserResponse>builder()
                .content(content)
                .number(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }

    private UserResponse mapToResponse(DatosPersonales datosPersonales) {
        Set<String> roleNames = datosPersonales.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = datosPersonales.getPermissionNames();

        // Lógica para determinar si es familiar
        boolean esFamiliar = false;
        String nombreTitular = null;

        if (datosPersonales.getNumdoc() != null) {
            var familiarOpt = grupoFamiliarRepository.findByIdFamiliarAndEstado(datosPersonales.getNumdoc(), "1");
            if (familiarOpt.isPresent()) {
                esFamiliar = true;
                // Buscar nombre del titular
                var titularOpt = datosPersonalesRepository.findByNumdoc(familiarOpt.get().getIdTitular());
                if (titularOpt.isPresent()) {
                    DatosPersonales titular = titularOpt.get();
                    nombreTitular = titular.getNombre() + " " + titular.getApepat();
                }
            }
        }

        return UserResponse.builder()
                .id(datosPersonales.getId())
                .username(datosPersonales.getLogin())
                .email(datosPersonales.getEmail())
                .firstName(datosPersonales.getNombre() != null ? datosPersonales.getNombre() : "Usuario")
                .paternalSurname(datosPersonales.getApepat() != null ? datosPersonales.getApepat() : "")
                .maternalSurname(datosPersonales.getApemat() != null ? datosPersonales.getApemat() : "")
                .lastName((datosPersonales.getApepat() != null ? datosPersonales.getApepat() : "Sin Apellido") +
                        (datosPersonales.getApemat() != null && !datosPersonales.getApemat().trim().isEmpty()
                                ? " " + datosPersonales.getApemat().trim()
                                : ""))
                .phone(datosPersonales.getFonLocal())
                .active(datosPersonales.getActive())
                .roles(roleNames)
                .permissions(permissions)
                .createdAt(datosPersonales.getCreatedAt())
                .updatedAt(datosPersonales.getUpdatedAt())
                .lastLogin(datosPersonales.getLastLogin())
                .idCompania(datosPersonales.getIdCompania())
                .esFamiliar(esFamiliar)
                .nombreTitular(nombreTitular)
                // Nuevos campos
                .numdoc(datosPersonales.getNumdoc())
                .nacfec(datosPersonales.getNacfec())
                .direcc(datosPersonales.getDirecc())
                .nomOcupacion(datosPersonales.getNomOcupacion())
                .fechaIng(datosPersonales.getFechaIng())
                .sucursal(datosPersonales.getSucursalActual() != null
                        ? datosPersonales.getSucursalActual().getNombreSucursal()
                        : null)
                .fotoPerfil(datosPersonales.getFotoPerfil())
                .build();
    }

    @Override
    @Transactional
    public UserResponse updateProfilePhoto(Long id, org.springframework.web.multipart.MultipartFile file) {
        DatosPersonales usuario = datosPersonalesRepository.findById(id)
                .orElseThrow(() -> new com.pe.finanzas.exception.ResourceNotFoundException("Usuario no encontrado"));

        try {
            String base64Image = "data:" + file.getContentType() + ";base64," +
                    java.util.Base64.getEncoder().encodeToString(file.getBytes());

            usuario.setFotoPerfil(base64Image);
            DatosPersonales updatedUser = datosPersonalesRepository.save(usuario);
            return mapToResponse(updatedUser);
        } catch (java.io.IOException e) {
            throw new RuntimeException("Error al procesar la imagen", e);
        }
    }

    // --- GESTIÓN DE SUCURSALES ASIGNADAS (igual que roles) ---

    @Override
    public UserResponse assignSucursales(Long userId, List<Long> sucursalIds) {
        log.info("Assigning {} sucursales to user: {}", sucursalIds.size(), userId);

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Set<com.pe.finanzas.sucursales.entity.Sucursal> sucursalesToAdd = sucursalIds.stream()
                .map(sucursalId -> sucursalRepository.findById(sucursalId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Sucursal no encontrada con id: " + sucursalId)))
                .collect(Collectors.toSet());

        datosPersonales.getSucursalesAsignadas().addAll(sucursalesToAdd);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);

        log.info("Sucursales assigned successfully to user: {}", datosPersonales.getLogin());

        // Auditoría
        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_SUCURSAL_ASSIGN",
                    "Sucursales asignadas a usuario " + datosPersonales.getLogin() + ": " + sucursalIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/v1/users/" + userId + "/sucursales",
                    "POST");
        } catch (Exception e) {
            log.error("Error logging audit for assign sucursales", e);
        }

        return mapToResponse(updatedUser);
    }

    @Override
    public UserResponse removeSucursales(Long userId, List<Long> sucursalIds) {
        log.info("Removing {} sucursales from user: {}", sucursalIds.size(), userId);

        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        Set<com.pe.finanzas.sucursales.entity.Sucursal> sucursalesToRemove = sucursalIds.stream()
                .map(sucursalId -> sucursalRepository.findById(sucursalId)
                        .orElseThrow(
                                () -> new ResourceNotFoundException("Sucursal no encontrada con id: " + sucursalId)))
                .collect(Collectors.toSet());

        datosPersonales.getSucursalesAsignadas().removeAll(sucursalesToRemove);
        DatosPersonales updatedUser = datosPersonalesRepository.save(datosPersonales);

        log.info("Sucursales removed successfully from user: {}", datosPersonales.getLogin());

        // Auditoría
        try {
            auditService.logAction(
                    "USUARIOS",
                    "USER_SUCURSAL_REMOVE",
                    "Sucursales removidas de usuario " + datosPersonales.getLogin() + ": " + sucursalIds,
                    HttpUtils.getCurrentUsername(),
                    userId,
                    HttpUtils.getClientIpAddress(httpRequest),
                    "Éxito",
                    HttpUtils.getUserAgent(httpRequest),
                    "/api/v1/users/" + userId + "/sucursales",
                    "DELETE");
        } catch (Exception e) {
            log.error("Error logging audit for remove sucursales", e);
        }

        return mapToResponse(updatedUser);
    }

    @Override
    public List<com.pe.finanzas.sucursales.dto.SucursalDto> getSucursalesAsignadas(Long userId) {
        DatosPersonales datosPersonales = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + userId));

        return datosPersonales.getSucursalesAsignadas().stream()
                .map(sucursal -> {
                    com.pe.finanzas.sucursales.dto.SucursalDto dto = new com.pe.finanzas.sucursales.dto.SucursalDto();
                    dto.setIdSucursal(sucursal.getIdSucursal());
                    dto.setNombreSucursal(sucursal.getNombreSucursal());
                    dto.setEstado(sucursal.getEstado());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByLogin(String login) {
        return datosPersonalesRepository.existsByLogin(login);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return datosPersonalesRepository.existsByEmail(email);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existsByNumdoc(String numdoc) {
        return datosPersonalesRepository.findByNumdoc(numdoc).isPresent();
    }

    @Override
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        long total = datosPersonalesRepository.countTotalUsersWithRoles();
        long active = datosPersonalesRepository.countByActiveWithRoles(true);
        long inactive = total - active;

        return UserStatsResponse.builder()
                .total(total)
                .active(active)
                .inactive(inactive)
                .build();
    }
}
