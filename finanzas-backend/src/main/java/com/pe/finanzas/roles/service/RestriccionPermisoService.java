package com.pe.finanzas.roles.service;

import com.pe.finanzas.roles.dto.RestriccionRequest;
import com.pe.finanzas.roles.dto.RestriccionResponse;
import com.pe.finanzas.roles.dto.ValidateActionRequest;

import java.util.List;

public interface RestriccionPermisoService {

    RestriccionResponse createRestriction(RestriccionRequest request);

    RestriccionResponse updateRestriction(Long id, RestriccionRequest request);

    void deleteRestriction(Long id);

    RestriccionResponse getRestrictionById(Long id);

    List<RestriccionResponse> getRestrictionsByRole(Long roleId);

    List<RestriccionResponse> getRestrictionsByRoleAndPermission(Long roleId, Long permissionId);

    boolean validateAction(ValidateActionRequest request);
}
