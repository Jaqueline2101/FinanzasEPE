package com.pe.finanzas.roles.service;

import com.pe.finanzas.roles.dto.SegregacionRequest;
import com.pe.finanzas.roles.dto.SegregacionResponse;

import java.util.List;

public interface SegregacionFuncionesService {

    SegregacionResponse createSegregation(SegregacionRequest request);

    SegregacionResponse updateSegregation(Long id, SegregacionRequest request);

    void deleteSegregation(Long id);

    SegregacionResponse getSegregationById(Long id);

    List<SegregacionResponse> getSegregationsByRole(Long roleId);

    boolean hasConflict(Long roleId1, Long roleId2);

    void validateUserRoleAssignment(Long userId, Long newRoleId);
}
