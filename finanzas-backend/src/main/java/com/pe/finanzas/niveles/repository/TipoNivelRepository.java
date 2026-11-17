package com.pe.finanzas.niveles.repository;

import com.pe.finanzas.niveles.entity.TipoNivel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoNivelRepository extends JpaRepository<TipoNivel, Long> {

    @Query("SELECT t FROM TipoNivel t WHERE t.estado = 1 ORDER BY t.orden ASC")
    List<TipoNivel> findAllActivos();
}
