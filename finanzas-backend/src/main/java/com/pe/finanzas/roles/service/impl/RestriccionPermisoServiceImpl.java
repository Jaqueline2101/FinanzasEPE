package com.pe.finanzas.roles.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.permissions.entity.Permission;
import com.pe.finanzas.permissions.repository.PermissionRepository;
import com.pe.finanzas.roles.dto.RestriccionRequest;
import com.pe.finanzas.roles.dto.RestriccionResponse;
import com.pe.finanzas.roles.dto.ValidateActionRequest;
import com.pe.finanzas.roles.entity.RestriccionPermiso;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.entity.TipoRestriccion;
import com.pe.finanzas.roles.repository.RestriccionPermisoRepository;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.roles.service.RestriccionPermisoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RestriccionPermisoServiceImpl implements RestriccionPermisoService {

    private final RestriccionPermisoRepository restriccionRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public RestriccionResponse createRestriction(RestriccionRequest request) {
        log.info("Creating restriction for role {} and permission {}", request.getRoleId(), request.getPermissionId());

        // Validar que el rol existe
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        // Validar que el permiso existe
        Permission permission = permissionRepository.findById(request.getPermissionId())
                .orElseThrow(() -> new ResourceNotFoundException("Permission", "id", request.getPermissionId()));

        // Validar que el rol tiene el permiso asignado
        if (!role.getPermissions().contains(permission)) {
            throw new ValidationException("El rol no tiene asignado el permiso especificado");
        }

        // Validar valores según tipo de restricción
        validateRestrictionValues(request);

        RestriccionPermiso restriccion = RestriccionPermiso.builder()
                .role(role)
                .permission(permission)
                .tipoRestriccion(request.getTipoRestriccion())
                .valorNumerico(request.getValorNumerico())
                .valorTexto(request.getValorTexto())
                .descripcion(request.getDescripcion())
                .active(request.getActive())
                .build();

        RestriccionPermiso saved = restriccionRepository.save(restriccion);
        log.info("Restriction created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public RestriccionResponse updateRestriction(Long id, RestriccionRequest request) {
        log.info("Updating restriction with id: {}", id);

        RestriccionPermiso restriccion = restriccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestriccionPermiso", "id", id));

        // Validar valores según tipo de restricción
        validateRestrictionValues(request);

        restriccion.setTipoRestriccion(request.getTipoRestriccion());
        restriccion.setValorNumerico(request.getValorNumerico());
        restriccion.setValorTexto(request.getValorTexto());
        restriccion.setDescripcion(request.getDescripcion());
        restriccion.setActive(request.getActive());

        RestriccionPermiso updated = restriccionRepository.save(restriccion);
        log.info("Restriction updated successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteRestriction(Long id) {
        log.info("Deleting restriction with id: {}", id);

        RestriccionPermiso restriccion = restriccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestriccionPermiso", "id", id));

        restriccion.setActive(false);
        restriccionRepository.save(restriccion);

        log.info("Restriction soft deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public RestriccionResponse getRestrictionById(Long id) {
        RestriccionPermiso restriccion = restriccionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("RestriccionPermiso", "id", id));

        return mapToResponse(restriccion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestriccionResponse> getRestrictionsByRole(Long roleId) {
        List<RestriccionPermiso> restricciones = restriccionRepository.findByRoleIdAndActiveTrue(roleId);
        return restricciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestriccionResponse> getRestrictionsByRoleAndPermission(Long roleId, Long permissionId) {
        List<RestriccionPermiso> restricciones = restriccionRepository
                .findByRoleIdAndPermissionIdAndActiveTrue(roleId, permissionId);
        return restricciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean validateAction(ValidateActionRequest request) {
        log.info("Validating action for role {} and permission {}", request.getRoleId(), request.getPermissionId());

        List<RestriccionPermiso> restricciones = restriccionRepository
                .findByRoleIdAndPermissionIdAndActiveTrue(request.getRoleId(), request.getPermissionId());

        if (restricciones.isEmpty()) {
            log.info("No restrictions found, action allowed");
            return true;
        }

        for (RestriccionPermiso restriccion : restricciones) {
            if (!validateSingleRestriction(restriccion, request)) {
                log.warn("Action blocked by restriction: {}", restriccion.getTipoRestriccion());
                return false;
            }
        }

        log.info("All restrictions passed, action allowed");
        return true;
    }

    private boolean validateSingleRestriction(RestriccionPermiso restriccion, ValidateActionRequest request) {
        switch (restriccion.getTipoRestriccion()) {
            case MONTO_MAXIMO:
                if (request.getMonto() != null && restriccion.getValorNumerico() != null) {
                    return request.getMonto().compareTo(restriccion.getValorNumerico()) <= 0;
                }
                return true;

            case DIAS_RETROACTIVOS:
                if (request.getFechaAccion() != null && restriccion.getValorNumerico() != null) {
                    long diasDiferencia = ChronoUnit.DAYS.between(request.getFechaAccion(), LocalDateTime.now());
                    return diasDiferencia <= restriccion.getValorNumerico().longValue();
                }
                return true;

            case HORA_INICIO:
                if (restriccion.getValorTexto() != null) {
                    LocalTime horaInicio = LocalTime.parse(restriccion.getValorTexto());
                    LocalTime horaActual = LocalTime.now();
                    return horaActual.isAfter(horaInicio) || horaActual.equals(horaInicio);
                }
                return true;

            case HORA_FIN:
                if (restriccion.getValorTexto() != null) {
                    LocalTime horaFin = LocalTime.parse(restriccion.getValorTexto());
                    LocalTime horaActual = LocalTime.now();
                    return horaActual.isBefore(horaFin) || horaActual.equals(horaFin);
                }
                return true;

            default:
                return true;
        }
    }

    private void validateRestrictionValues(RestriccionRequest request) {
        switch (request.getTipoRestriccion()) {
            case MONTO_MAXIMO:
            case DIAS_RETROACTIVOS:
                if (request.getValorNumerico() == null || request.getValorNumerico().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new BadRequestException(
                            "El valor numérico debe ser mayor a 0 para " + request.getTipoRestriccion());
                }
                break;

            case HORA_INICIO:
            case HORA_FIN:
                if (request.getValorTexto() == null || request.getValorTexto().trim().isEmpty()) {
                    throw new BadRequestException(
                            "El valor de texto (hora) es obligatorio para " + request.getTipoRestriccion());
                }
                try {
                    LocalTime.parse(request.getValorTexto());
                } catch (Exception e) {
                    throw new BadRequestException("El formato de hora debe ser HH:mm:ss");
                }
                break;
        }
    }

    private RestriccionResponse mapToResponse(RestriccionPermiso restriccion) {
        return RestriccionResponse.builder()
                .id(restriccion.getId())
                .roleId(restriccion.getRole().getId())
                .roleName(restriccion.getRole().getName())
                .permissionId(restriccion.getPermission().getId())
                .permissionName(restriccion.getPermission().getName())
                .tipoRestriccion(restriccion.getTipoRestriccion())
                .valorNumerico(restriccion.getValorNumerico())
                .valorTexto(restriccion.getValorTexto())
                .descripcion(restriccion.getDescripcion())
                .active(restriccion.getActive())
                .createdAt(restriccion.getCreatedAt())
                .updatedAt(restriccion.getUpdatedAt())
                .build();
    }
}
