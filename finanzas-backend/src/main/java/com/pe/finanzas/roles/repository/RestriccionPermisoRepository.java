package com.pe.finanzas.roles.repository;

import com.pe.finanzas.roles.entity.RestriccionPermiso;
import com.pe.finanzas.roles.entity.TipoRestriccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestriccionPermisoRepository extends JpaRepository<RestriccionPermiso, Long> {

    List<RestriccionPermiso> findByRoleIdAndPermissionIdAndActiveTrue(Long roleId, Long permissionId);

    List<RestriccionPermiso> findByRoleIdAndActiveTrue(Long roleId);

    List<RestriccionPermiso> findByTipoRestriccionAndActiveTrue(TipoRestriccion tipoRestriccion);

    @Query("SELECT r FROM RestriccionPermiso r WHERE r.role.id = :roleId AND r.permission.id = :permissionId AND r.tipoRestriccion = :tipo AND r.active = true")
    List<RestriccionPermiso> findByRolePermissionAndTipo(@Param("roleId") Long roleId,
            @Param("permissionId") Long permissionId,
            @Param("tipo") TipoRestriccion tipo);
}
