package com.pe.finanzas.users.service;

import java.util.List;
import org.springframework.data.domain.Pageable;

import com.pe.finanzas.shared.dto.PageResponse;
import com.pe.finanzas.users.dto.CreateUserRequest;
import com.pe.finanzas.users.dto.UpdateUserRequest;
import com.pe.finanzas.users.dto.UserResponse;
import com.pe.finanzas.sucursales.dto.SucursalDto;

public interface UserService {

    UserResponse createUser(CreateUserRequest request);

    UserResponse promoteToUser(Long id, CreateUserRequest request);

    UserResponse updateUser(Long id, UpdateUserRequest request);

    UserResponse getUserById(Long id);

    UserResponse getUserByLogin(String login);

    PageResponse<UserResponse> getAllUsers(Pageable pageable);

    PageResponse<UserResponse> searchByName(String searchTerm, Pageable pageable);

    PageResponse<UserResponse> buscarPersonas(String searchTerm, Pageable pageable);

    PageResponse<UserResponse> searchByFilter(String query, String type, Pageable pageable);

    PageResponse<UserResponse> getActiveUsers(Pageable pageable);

    PageResponse<UserResponse> getUsersByRole(String roleName, Pageable pageable);

    void deleteUser(Long id);

    void toggleUserStatus(Long id, boolean active);

    UserResponse assignRoles(Long userId, List<Long> roleIds);

    UserResponse removeRoles(Long userId, List<Long> roleIds);

    void changePassword(Long userId, String oldPassword, String newPassword);

    void resetPassword(Long userId, String newPassword, boolean mustChangePassword);

    UserResponse assignSucursales(Long userId, List<Long> sucursalIds);

    UserResponse removeSucursales(Long userId, List<Long> sucursalIds);

    List<SucursalDto> getSucursalesAsignadas(Long userId);

    UserResponse updateProfilePhoto(Long id, org.springframework.web.multipart.MultipartFile file);

    boolean existsByLogin(String login);

    boolean existsByEmail(String email);

    boolean existsByNumdoc(String numdoc);

    com.pe.finanzas.users.dto.UserStatsResponse getUserStats();
}
