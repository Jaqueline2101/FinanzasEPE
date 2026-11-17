package com.pe.finanzas.users.service;

import java.util.List;

import com.pe.finanzas.permissions.dto.PermissionResponse;
import com.pe.finanzas.users.dto.AssignDirectPermissionsRequest;
import com.pe.finanzas.users.dto.GrantDirectPermissionRequest;
import com.pe.finanzas.users.dto.UserDirectPermissionResponse;
import com.pe.finanzas.users.dto.UserWithPermissionsResponse;

public interface UserPermissionService {

    UserWithPermissionsResponse assignDirectPermissions(AssignDirectPermissionsRequest request, Long currentUserId);

    UserDirectPermissionResponse grantDirectPermission(GrantDirectPermissionRequest request, Long currentUserId);

    UserWithPermissionsResponse removeDirectPermissions(Long userId, List<Long> permissionIds);

    void revokeDirectPermission(Long userId, Long permissionId);

    void clearAllDirectPermissions(Long userId);

    List<UserDirectPermissionResponse> getDirectPermissions(Long userId);

    List<PermissionResponse> getEffectivePermissions(Long userId);

    UserWithPermissionsResponse getUserWithPermissions(Long userId);

    UserWithPermissionsResponse togglePermissionMode(Long userId, Boolean useDirectPermissions);

    boolean hasPermission(Long userId, String permissionName);

    int deactivateExpiredPermissions();

    List<UserDirectPermissionResponse> getExpiringPermissions(int days);
}
