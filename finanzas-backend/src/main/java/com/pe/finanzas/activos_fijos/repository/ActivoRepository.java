package com.pe.finanzas.activos_fijos.repository;

import com.pe.finanzas.activos_fijos.entity.Activo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ActivoRepository extends JpaRepository<Activo, Long> {

    Optional<Activo> findByCodigoPatrimonial(String codigoPatrimonial);

    List<Activo> findBySituacion(String situacion);

    List<Activo> findByEstadoFisico(String estadoFisico);

    List<Activo> findByIdResponsable(Long idResponsable);
}
