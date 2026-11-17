package com.pe.finanzas.permissions.service;

import com.pe.finanzas.permissions.dto.PermissionRequest;
import com.pe.finanzas.permissions.dto.PermissionResponse;
import com.pe.finanzas.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface PermissionService {

    // ========== CRUD BÁSICO ==========

    PermissionResponse createPermission(PermissionRequest request);

    PermissionResponse updatePermission(Long id, PermissionRequest request);

    void deletePermission(Long id);

    // ========== CONSULTAS INDIVIDUALES ==========

    PermissionResponse getPermissionById(Long id);

    PermissionResponse getPermissionByName(String name);

    // ========== LISTADOS CON PAGINACIÓN (Para Tablas/Grillas) ==========

    PageResponse<PermissionResponse> getAllPermissions(Pageable pageable, String modulo);

    PageResponse<PermissionResponse> getActivePermissions(Pageable pageable, String modulo);

    PageResponse<PermissionResponse> getPermissionsByModule(String module, Pageable pageable);

    PageResponse<PermissionResponse> getActivePermissionsByModule(String module, Pageable pageable);

    PageResponse<PermissionResponse> searchPermissions(String query, String by, Pageable pageable);

    PageResponse<PermissionResponse> searchByFilter(String query, String type, Pageable pageable, String modulo);

    // ========== LISTADOS SIN PAGINACIÓN (Para Selects/Comboboxes) ==========

    List<PermissionResponse> getAllPermissionsAsList();

    List<PermissionResponse> getActivePermissionsAsList();

    List<PermissionResponse> getPermissionsByModuleAsList(String module);

    List<PermissionResponse> getActivePermissionsByModuleAsList(String module);

    List<PermissionResponse> searchPermissionsAsList(String query);

    // ========== MÓDULOS Y UTILIDADES ==========

    List<String> getAllModules();

    boolean existsByName(String name);
}