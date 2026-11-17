package com.pe.finanzas.grupo_familiar.repository;

import com.pe.finanzas.grupo_familiar.entity.GrupoFamiliar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GrupoFamiliarRepository extends JpaRepository<GrupoFamiliar, Long> {

    /**
     * Obtener todos los familiares de un trabajador titular
     */
    List<GrupoFamiliar> findByIdTitularAndEstado(String idTitular, String estado);

    /**
     * Obtener el trabajador titular de un familiar
     */
    Optional<GrupoFamiliar> findByIdFamiliarAndEstado(String idFamiliar, String estado);

    /**
     * Verificar si existe una relación entre titular y familiar
     */
    boolean existsByIdTitularAndIdFamiliar(String idTitular, String idFamiliar);

    /**
     * Obtener todos los familiares de un trabajador (activos e inactivos)
     */
    List<GrupoFamiliar> findByIdTitular(String idTitular);

    /**
     * Obtener relación específica entre titular y familiar
     */
    Optional<GrupoFamiliar> findByIdTitularAndIdFamiliar(String idTitular, String idFamiliar);

    /**
     * Contar familiares activos por titular
     */
    long countByIdTitularAndEstado(String idTitular, String estado);
}
