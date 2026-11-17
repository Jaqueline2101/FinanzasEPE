package com.pe.finanzas.puntos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.puntos.entity.TipoDocumento;

@Repository
public interface TipoDocumentoRepository extends JpaRepository<TipoDocumento, Long> {

    // Buscar por código
    Optional<TipoDocumento> findByCodigo(String codigo);

    // Buscar activos
    List<TipoDocumento> findByActivoTrue();

    // Verificar si existe por código
    boolean existsByCodigo(String codigo);

    // Buscar por nombre (búsqueda parcial)
    List<TipoDocumento> findByNombreContainingIgnoreCase(String nombre);
}
