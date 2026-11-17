package com.pe.finanzas.roles.repository;

import com.pe.finanzas.roles.entity.DelegacionTemporal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface DelegacionTemporalRepository extends JpaRepository<DelegacionTemporal, Long> {

    List<DelegacionTemporal> findByUsuarioOrigenIdAndActiveTrue(Long usuarioOrigenId);

    List<DelegacionTemporal> findByUsuarioDelegadoIdAndActiveTrue(Long usuarioDelegadoId);

    @Query("SELECT d FROM DelegacionTemporal d WHERE d.usuarioDelegado.id = :usuarioId AND d.active = true AND :now >= d.fechaInicio AND :now <= d.fechaFin")
    List<DelegacionTemporal> findActiveDelegationsForUser(@Param("usuarioId") Long usuarioId,
            @Param("now") LocalDateTime now);

    @Query("SELECT d FROM DelegacionTemporal d WHERE d.usuarioOrigen.id = :usuarioOrigenId AND d.active = true AND :now >= d.fechaInicio AND :now <= d.fechaFin")
    List<DelegacionTemporal> findActiveDelegationsByOrigin(@Param("usuarioOrigenId") Long usuarioOrigenId,
            @Param("now") LocalDateTime now);
}
