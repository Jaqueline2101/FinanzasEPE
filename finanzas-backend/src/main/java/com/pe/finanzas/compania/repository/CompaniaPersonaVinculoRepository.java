package com.pe.finanzas.compania.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.compania.entity.CompaniaPersonaVinculo;

@Repository
public interface CompaniaPersonaVinculoRepository extends JpaRepository<CompaniaPersonaVinculo, Long> {
    List<CompaniaPersonaVinculo> findByCompaniaId(Long idCompania);

    List<CompaniaPersonaVinculo> findByPersonaId(Long idPersona);
}
