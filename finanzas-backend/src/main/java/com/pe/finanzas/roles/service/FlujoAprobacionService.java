package com.pe.finanzas.roles.service;

import com.pe.finanzas.roles.dto.AprobacionRequest;
import com.pe.finanzas.roles.dto.AprobacionResponse;
import com.pe.finanzas.roles.entity.EstadoAprobacion;

import java.util.List;

public interface FlujoAprobacionService {

    AprobacionResponse createApprovalRequest(AprobacionRequest request);

    AprobacionResponse approveRequest(Long id, Long aprobadorId, String comentario);

    AprobacionResponse rejectRequest(Long id, Long aprobadorId, String comentario);

    AprobacionResponse getApprovalById(Long id);

    List<AprobacionResponse> getApprovalsByRequester(Long solicitanteId);

    List<AprobacionResponse> getApprovalsByApprover(Long aprobadorId);

    List<AprobacionResponse> getPendingApprovals(Long aprobadorId);

    List<AprobacionResponse> getApprovalsByStatus(EstadoAprobacion estado);
}
