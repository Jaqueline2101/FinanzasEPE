package com.pe.finanzas.users.repository;

import com.pe.finanzas.users.entity.UserPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

@Repository
public interface UserPermissionRepository extends JpaRepository<UserPermission, Long> {

    void deleteByUserId(Long userId);

    Optional<UserPermission> findByUserIdAndPermissionId(Long userId, Long permissionId);

    long countByUserIdAndActive(Long userId, boolean active);

    List<UserPermission> findByUserId(Long userId);

    void deleteByUserIdAndPermissionIdIn(Long userId, List<Long> permissionIds);

    @Query("SELECT up FROM UserPermission up WHERE up.active = true AND up.expiresAt < :now")
    List<UserPermission> findExpiredPermissions(@Param("now") LocalDateTime now);

    @Query("SELECT up FROM UserPermission up WHERE up.active = true AND up.expiresAt BETWEEN :now AND :future")
    List<UserPermission> findExpiringPermissions(@Param("now") LocalDateTime now,
            @Param("future") LocalDateTime future);
}
