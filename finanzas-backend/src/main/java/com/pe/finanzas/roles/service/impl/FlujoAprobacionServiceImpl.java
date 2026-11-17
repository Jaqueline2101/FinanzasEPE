package com.pe.finanzas.roles.service.impl;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.ValidationException;
import com.pe.finanzas.roles.dto.AprobacionRequest;
import com.pe.finanzas.roles.dto.AprobacionResponse;
import com.pe.finanzas.roles.entity.EstadoAprobacion;
import com.pe.finanzas.roles.entity.FlujoAprobacion;
import com.pe.finanzas.roles.repository.FlujoAprobacionRepository;
import com.pe.finanzas.roles.service.FlujoAprobacionService;
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
public class FlujoAprobacionServiceImpl implements FlujoAprobacionService {

    private final FlujoAprobacionRepository flujoRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional
    public AprobacionResponse createApprovalRequest(AprobacionRequest request) {
        log.info("Creating approval request for user {}", request.getSolicitanteId());

        DatosPersonales solicitante = datosPersonalesRepository.findById(request.getSolicitanteId())
                .orElseThrow(() -> new ResourceNotFoundException("DatosPersonales", "id", request.getSolicitanteId()));

        DatosPersonales aprobador = null;
        if (request.getAprobadorId() != null) {
            aprobador = datosPersonalesRepository.findById(request.getAprobadorId())
                    .orElseThrow(
                            () -> new ResourceNotFoundException("DatosPersonales", "id", request.getAprobadorId()));
        }

        FlujoAprobacion flujo = FlujoAprobacion.builder()
                .solicitante(solicitante)
                .aprobador(aprobador)
                .tipoAccion(request.getTipoAccion())
                .descripcion(request.getDescripcion())
                .monto(request.getMonto())
                .datosContexto(request.getDatosContexto())
                .estado(EstadoAprobacion.PENDIENTE)
                .fechaSolicitud(LocalDateTime.now())
                .active(true)
                .build();

        FlujoAprobacion saved = flujoRepository.save(flujo);
        log.info("Approval request created with id: {}", saved.getId());

        return mapToResponse(saved);
    }

    @Override
    @Transactional
    public AprobacionResponse approveRequest(Long id, Long aprobadorId, String comentario) {
        log.info("Approving request {} by user {}", id, aprobadorId);

        FlujoAprobacion flujo = flujoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlujoAprobacion", "id", id));

        if (flujo.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new ValidationException("Solo se pueden aprobar solicitudes pendientes");
        }

        flujo.setEstado(EstadoAprobacion.APROBADO);
        flujo.setComentarioAprobador(comentario);
        flujo.setFechaRespuesta(LocalDateTime.now());

        if (flujo.getAprobador() == null) {
            DatosPersonales aprobador = datosPersonalesRepository.findById(aprobadorId)
                    .orElseThrow(() -> new ResourceNotFoundException("DatosPersonales", "id", aprobadorId));
            flujo.setAprobador(aprobador);
        }

        FlujoAprobacion updated = flujoRepository.save(flujo);
        log.info("Request approved successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public AprobacionResponse rejectRequest(Long id, Long aprobadorId, String comentario) {
        log.info("Rejecting request {} by user {}", id, aprobadorId);

        FlujoAprobacion flujo = flujoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlujoAprobacion", "id", id));

        if (flujo.getEstado() != EstadoAprobacion.PENDIENTE) {
            throw new ValidationException("Solo se pueden rechazar solicitudes pendientes");
        }

        flujo.setEstado(EstadoAprobacion.RECHAZADO);
        flujo.setComentarioAprobador(comentario);
        flujo.setFechaRespuesta(LocalDateTime.now());

        if (flujo.getAprobador() == null) {
            DatosPersonales aprobador = datosPersonalesRepository.findById(aprobadorId)
                    .orElseThrow(() -> new ResourceNotFoundException("DatosPersonales", "id", aprobadorId));
            flujo.setAprobador(aprobador);
        }

        FlujoAprobacion updated = flujoRepository.save(flujo);
        log.info("Request rejected successfully");

        return mapToResponse(updated);
    }

    @Override
    @Transactional(readOnly = true)
    public AprobacionResponse getApprovalById(Long id) {
        FlujoAprobacion flujo = flujoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FlujoAprobacion", "id", id));
        return mapToResponse(flujo);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprobacionResponse> getApprovalsByRequester(Long solicitanteId) {
        List<FlujoAprobacion> flujos = flujoRepository.findBySolicitanteIdAndActiveTrue(solicitanteId);
        return flujos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprobacionResponse> getApprovalsByApprover(Long aprobadorId) {
        List<FlujoAprobacion> flujos = flujoRepository.findByAprobadorIdAndActiveTrue(aprobadorId);
        return flujos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprobacionResponse> getPendingApprovals(Long aprobadorId) {
        List<FlujoAprobacion> flujos = flujoRepository.findByAprobadorIdAndEstadoAndActiveTrue(aprobadorId,
                EstadoAprobacion.PENDIENTE);
        return flujos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AprobacionResponse> getApprovalsByStatus(EstadoAprobacion estado) {
        List<FlujoAprobacion> flujos = flujoRepository.findByEstadoAndActiveTrue(estado);
        return flujos.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    private AprobacionResponse mapToResponse(FlujoAprobacion flujo) {
        return AprobacionResponse.builder()
                .id(flujo.getId())
                .solicitanteId(flujo.getSolicitante().getId())
                .solicitanteNombre(flujo.getSolicitante().getNombre())
                .aprobadorId(flujo.getAprobador() != null ? flujo.getAprobador().getId() : null)
                .aprobadorNombre(flujo.getAprobador() != null ? flujo.getAprobador().getNombre() : null)
                .tipoAccion(flujo.getTipoAccion())
                .descripcion(flujo.getDescripcion())
                .monto(flujo.getMonto())
                .datosContexto(flujo.getDatosContexto())
                .estado(flujo.getEstado())
                .comentarioAprobador(flujo.getComentarioAprobador())
                .fechaSolicitud(flujo.getFechaSolicitud())
                .fechaRespuesta(flujo.getFechaRespuesta())
                .active(flujo.getActive())
                .build();
    }
}
