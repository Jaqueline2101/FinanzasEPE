package com.pe.finanzas.roles.service;

import com.pe.finanzas.roles.dto.DelegacionRequest;
import com.pe.finanzas.roles.dto.DelegacionResponse;

import java.util.List;

public interface DelegacionTemporalService {

    DelegacionResponse createDelegation(DelegacionRequest request);

    DelegacionResponse updateDelegation(Long id, DelegacionRequest request);

    void deleteDelegation(Long id);

    DelegacionResponse getDelegationById(Long id);

    List<DelegacionResponse> getDelegationsByOriginUser(Long usuarioOrigenId);

    List<DelegacionResponse> getDelegationsByDelegatedUser(Long usuarioDelegadoId);

    List<DelegacionResponse> getActiveDelegationsForUser(Long usuarioId);
}
