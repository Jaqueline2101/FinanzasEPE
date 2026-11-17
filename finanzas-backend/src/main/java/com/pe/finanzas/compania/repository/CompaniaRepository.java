package com.pe.finanzas.compania.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.compania.entity.Compania;

@Repository
public interface CompaniaRepository extends JpaRepository<Compania, Long> {
    Optional<Compania> findByCodigo(String codigo);

    Optional<Compania> findByRazSol(String razSol);

    Optional<Compania> findByPersonaBaseRuc(String ruc);

    java.util.List<Compania> findTop10ByRazSolContainingIgnoreCaseOrPersonaBaseRucStartingWithOrNombreContainingIgnoreCase(
            String razSol, String ruc, String nombre);
}
