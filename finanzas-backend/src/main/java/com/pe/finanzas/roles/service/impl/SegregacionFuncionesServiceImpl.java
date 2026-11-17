package com.pe.finanzas.roles.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.roles.dto.SegregacionRequest;
import com.pe.finanzas.roles.dto.SegregacionResponse;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.entity.SegregacionFunciones;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.roles.repository.SegregacionFuncionesRepository;
import com.pe.finanzas.roles.service.SegregacionFuncionesService;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SegregacionFuncionesServiceImpl implements SegregacionFuncionesService {

    private final SegregacionFuncionesRepository segregacionRepository;
    private final RoleRepository roleRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional
    public SegregacionResponse createSegregation(SegregacionRequest request) {
        log.info("Creating segregation between roles {} and {}", request.getRolOrigenId(), request.getRolConflictoId());

        // Validar que los roles existen
        Role rolOrigen = roleRepository.findById(request.getRolOrigenId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRolOrigenId()));

        Role rolConflicto = roleRepository.findById(request.getRolConflictoId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRolConflictoId()));

        // Validar que no sean el mismo rol
        if (request.getRolOrigenId().equals(request.getRolConflictoId())) {
            throw new BadRequestException("No se puede crear una segregación entre el mismo rol");
        }

        // Validar que no exista ya una segregación entre estos roles
        if (segregacionRepository.existsConflictBetweenRoles(request.getRolOrigenId(), request.getRolConflictoId())) {
            throw new ValidationException("Ya existe una segregación entre estos roles");
        }

        SegregacionFunciones segregacion = SegregacionFunciones.builder()
                .rolOrigen(rolOrigen)
                .rolConflicto(rolConflicto)
                .descripcion(request.getDescripcion())
                .razonNegocio(request.getRazonNegocio())
                .active(request.getActive())
                .build();

        SegregacionFunciones saved = segregacionRepository.save(segregacion);
        log.info("Segregation created successfully with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public SegregacionResponse updateSegregation(Long id, SegregacionRequest request) {
        log.info("Updating segregation with id: {}", id);

        SegregacionFunciones segregacion = segregacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SegregacionFunciones", "id", id));

        segregacion.setDescripcion(request.getDescripcion());
        segregacion.setRazonNegocio(request.getRazonNegocio());
        segregacion.setActive(request.getActive());

        SegregacionFunciones updated = segregacionRepository.save(segregacion);
        log.info("Segregation updated successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteSegregation(Long id) {
        log.info("Deleting segregation with id: {}", id);

        SegregacionFunciones segregacion = segregacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SegregacionFunciones", "id", id));

        segregacion.setActive(false);
        segregacionRepository.save(segregacion);

        log.info("Segregation soft deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public SegregacionResponse getSegregationById(Long id) {
        SegregacionFunciones segregacion = segregacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("SegregacionFunciones", "id", id));

        return mapToResponse(segregacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<SegregacionResponse> getSegregationsByRole(Long roleId) {
        List<SegregacionFunciones> segregaciones = segregacionRepository.findByRolOrigenIdAndActiveTrue(roleId);
        return segregaciones.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean hasConflict(Long roleId1, Long roleId2) {
        return segregacionRepository.existsConflictBetweenRoles(roleId1, roleId2);
    }

    @Override
    @Transactional(readOnly = true)
    public void validateUserRoleAssignment(Long userId, Long newRoleId) {
        log.info("Validating role assignment for user {} with new role {}", userId, newRoleId);

        DatosPersonales user = datosPersonalesRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("DatosPersonales", "id", userId));

        if (user.getRoles() == null || user.getRoles().isEmpty()) {
            log.info("User has no current roles, assignment allowed");
            return;
        }

        // Verificar conflictos con todos los roles actuales del usuario
        for (Role currentRole : user.getRoles()) {
            if (segregacionRepository.existsConflictBetweenRoles(currentRole.getId(), newRoleId)) {
                List<SegregacionFunciones> conflicts = segregacionRepository
                        .findConflictBetweenRoles(currentRole.getId(), newRoleId);
                String razon = conflicts.isEmpty() ? "Segregación de funciones" : conflicts.get(0).getRazonNegocio();
                throw new ValidationException(
                        "No se puede asignar este rol debido a segregación de funciones con rol " +
                                currentRole.getName() + ": " + razon);
            }
        }

        log.info("Role assignment validation passed");
    }

    private SegregacionResponse mapToResponse(SegregacionFunciones segregacion) {
        return SegregacionResponse.builder()
                .id(segregacion.getId())
                .rolOrigenId(segregacion.getRolOrigen().getId())
                .rolOrigenName(segregacion.getRolOrigen().getName())
                .rolConflictoId(segregacion.getRolConflicto().getId())
                .rolConflictoName(segregacion.getRolConflicto().getName())
                .descripcion(segregacion.getDescripcion())
                .razonNegocio(segregacion.getRazonNegocio())
                .active(segregacion.getActive())
                .createdAt(segregacion.getCreatedAt())
                .updatedAt(segregacion.getUpdatedAt())
                .build();
    }
}
