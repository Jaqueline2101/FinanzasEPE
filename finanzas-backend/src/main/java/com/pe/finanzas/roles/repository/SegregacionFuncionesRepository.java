package com.pe.finanzas.roles.repository;

import com.pe.finanzas.roles.entity.SegregacionFunciones;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SegregacionFuncionesRepository extends JpaRepository<SegregacionFunciones, Long> {

    List<SegregacionFunciones> findByRolOrigenIdAndActiveTrue(Long rolOrigenId);

    @Query("SELECT s FROM SegregacionFunciones s WHERE (s.rolOrigen.id = :rolId1 AND s.rolConflicto.id = :rolId2) OR (s.rolOrigen.id = :rolId2 AND s.rolConflicto.id = :rolId1) AND s.active = true")
    List<SegregacionFunciones> findConflictBetweenRoles(@Param("rolId1") Long rolId1, @Param("rolId2") Long rolId2);

    @Query("SELECT CASE WHEN COUNT(s) > 0 THEN true ELSE false END FROM SegregacionFunciones s WHERE ((s.rolOrigen.id = :rolId1 AND s.rolConflicto.id = :rolId2) OR (s.rolOrigen.id = :rolId2 AND s.rolConflicto.id = :rolId1)) AND s.active = true")
    boolean existsConflictBetweenRoles(@Param("rolId1") Long rolId1, @Param("rolId2") Long rolId2);
}
