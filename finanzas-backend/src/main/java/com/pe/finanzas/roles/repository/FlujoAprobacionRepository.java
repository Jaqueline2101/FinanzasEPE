package com.pe.finanzas.roles.repository;

import com.pe.finanzas.roles.entity.EstadoAprobacion;
import com.pe.finanzas.roles.entity.FlujoAprobacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FlujoAprobacionRepository extends JpaRepository<FlujoAprobacion, Long> {

    List<FlujoAprobacion> findBySolicitanteIdAndActiveTrue(Long solicitanteId);

    List<FlujoAprobacion> findByAprobadorIdAndActiveTrue(Long aprobadorId);

    List<FlujoAprobacion> findByEstadoAndActiveTrue(EstadoAprobacion estado);

    List<FlujoAprobacion> findByAprobadorIdAndEstadoAndActiveTrue(Long aprobadorId, EstadoAprobacion estado);
}
