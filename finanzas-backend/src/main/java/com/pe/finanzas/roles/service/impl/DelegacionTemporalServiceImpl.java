package com.pe.finanzas.roles.service.impl;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.roles.dto.DelegacionRequest;
import com.pe.finanzas.roles.dto.DelegacionResponse;
import com.pe.finanzas.roles.entity.DelegacionTemporal;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.repository.DelegacionTemporalRepository;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.roles.service.DelegacionTemporalService;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DelegacionTemporalServiceImpl implements DelegacionTemporalService {

    private final DelegacionTemporalRepository delegacionRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public DelegacionResponse createDelegation(DelegacionRequest request) {
        log.info("Creating delegation from user {} to user {}", request.getUsuarioOrigenId(),
                request.getUsuarioDelegadoId());

        // Validar usuarios
        DatosPersonales usuarioOrigen = datosPersonalesRepository.findById(request.getUsuarioOrigenId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("DatosPersonales", "id", request.getUsuarioOrigenId()));

        DatosPersonales usuarioDelegado = datosPersonalesRepository.findById(request.getUsuarioDelegadoId())
                .orElseThrow(
                        () -> new ResourceNotFoundException("DatosPersonales", "id", request.getUsuarioDelegadoId()));

        // Validar rol
        Role role = roleRepository.findById(request.getRoleId())
                .orElseThrow(() -> new ResourceNotFoundException("Role", "id", request.getRoleId()));

        // Validar fechas
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        if (request.getFechaInicio().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("La fecha de inicio no puede ser en el pasado");
        }

        // Validar que no sea el mismo usuario
        if (request.getUsuarioOrigenId().equals(request.getUsuarioDelegadoId())) {
            throw new ValidationException("No se puede delegar a sí mismo");
        }

        DelegacionTemporal delegacion = DelegacionTemporal.builder()
                .usuarioOrigen(usuarioOrigen)
                .usuarioDelegado(usuarioDelegado)
                .role(role)
                .fechaInicio(request.getFechaInicio())
                .fechaFin(request.getFechaFin())
                .motivo(request.getMotivo())
                .active(request.getActive())
                .build();

        DelegacionTemporal saved = delegacionRepository.save(delegacion);
        log.info("Delegation created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public DelegacionResponse updateDelegation(Long id, DelegacionRequest request) {
        log.info("Updating delegation with id: {}", id);

        DelegacionTemporal delegacion = delegacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DelegacionTemporal", "id", id));

        // Validar fechas
        if (request.getFechaFin().isBefore(request.getFechaInicio())) {
            throw new BadRequestException("La fecha de fin debe ser posterior a la fecha de inicio");
        }

        delegacion.setFechaInicio(request.getFechaInicio());
        delegacion.setFechaFin(request.getFechaFin());
        delegacion.setMotivo(request.getMotivo());
        delegacion.setActive(request.getActive());

        DelegacionTemporal updated = delegacionRepository.save(delegacion);
        log.info("Delegation updated successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deleteDelegation(Long id) {
        log.info("Deleting delegation with id: {}", id);

        DelegacionTemporal delegacion = delegacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DelegacionTemporal", "id", id));

        delegacion.setActive(false);
        delegacionRepository.save(delegacion);

        log.info("Delegation soft deleted successfully");
    }

    @Override
    @Transactional(readOnly = true)
    public DelegacionResponse getDelegationById(Long id) {
        DelegacionTemporal delegacion = delegacionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("DelegacionTemporal", "id", id));
        return mapToResponse(delegacion);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelegacionResponse> getDelegationsByOriginUser(Long usuarioOrigenId) {
        List<DelegacionTemporal> delegaciones = delegacionRepository
                .findByUsuarioOrigenIdAndActiveTrue(usuarioOrigenId);
        return delegaciones.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelegacionResponse> getDelegationsByDelegatedUser(Long usuarioDelegadoId) {
        List<DelegacionTemporal> delegaciones = delegacionRepository
                .findByUsuarioDelegadoIdAndActiveTrue(usuarioDelegadoId);
        return delegaciones.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<DelegacionResponse> getActiveDelegationsForUser(Long usuarioId) {
        List<DelegacionTemporal> delegaciones = delegacionRepository.findActiveDelegationsForUser(usuarioId,
                LocalDateTime.now());
        return delegaciones.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private DelegacionResponse mapToResponse(DelegacionTemporal delegacion) {
        return DelegacionResponse.builder()
                .id(delegacion.getId())
                .usuarioOrigenId(delegacion.getUsuarioOrigen().getId())
                .usuarioOrigenNombre(delegacion.getUsuarioOrigen().getNombre())
                .usuarioDelegadoId(delegacion.getUsuarioDelegado().getId())
                .usuarioDelegadoNombre(delegacion.getUsuarioDelegado().getNombre())
                .roleId(delegacion.getRole().getId())
                .roleName(delegacion.getRole().getName())
                .fechaInicio(delegacion.getFechaInicio())
                .fechaFin(delegacion.getFechaFin())
                .motivo(delegacion.getMotivo())
                .active(delegacion.getActive())
                .isCurrentlyActive(delegacion.isActive())
                .createdAt(delegacion.getCreatedAt())
                .updatedAt(delegacion.getUpdatedAt())
                .build();
    }
}
