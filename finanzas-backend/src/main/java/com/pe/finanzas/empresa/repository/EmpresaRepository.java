package com.pe.finanzas.empresa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.empresa.entity.Empresa;

@Repository
public interface EmpresaRepository extends JpaRepository<Empresa, Long> {
    java.util.Optional<Empresa> findByCodigo(String codigo);

    java.util.Optional<Empresa> findByIdPersonalUser(String ruc);

    java.util.Optional<Empresa> findByNombre(String nombre);

    java.util.List<Empresa> findTop10ByAbrevContainingIgnoreCaseOrIdPersonalUserStartingWithOrCodigoContainingIgnoreCase(
            String abrev, String ruc, String codigo);

    java.util.List<Empresa> findTop10ByNombreContainingIgnoreCase(String nombre);
}
