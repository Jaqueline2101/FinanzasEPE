package com.pe.finanzas.roles.service;

import com.pe.finanzas.roles.dto.RoleRequest;
import com.pe.finanzas.roles.dto.RoleResponse;
import com.pe.finanzas.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RoleService {

    RoleResponse createRole(RoleRequest request);

    RoleResponse updateRole(Long id, RoleRequest request);

    void deleteRole(Long id);

    RoleResponse getRoleById(Long id);

    RoleResponse getRoleByName(String name);

    PageResponse<RoleResponse> getAllRoles(Pageable pageable, String modulo);

    PageResponse<RoleResponse> getActiveRoles(Pageable pageable, String modulo);

    PageResponse<RoleResponse> searchByFilter(String query, String type, Pageable pageable, String modulo);

    RoleResponse assignPermissions(Long roleId, List<Long> permissionIds);

    RoleResponse removePermissions(Long roleId, List<Long> permissionIds);
}