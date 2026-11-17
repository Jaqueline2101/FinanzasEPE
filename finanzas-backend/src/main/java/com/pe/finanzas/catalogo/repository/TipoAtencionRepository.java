package com.pe.finanzas.catalogo.repository;

import com.pe.finanzas.catalogo.entity.TipoAtencion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TipoAtencionRepository extends JpaRepository<TipoAtencion, Long> {

    @Query("SELECT t FROM TipoAtencion t WHERE t.estado = 1 ORDER BY t.orden ASC")
    List<TipoAtencion> findAllActivos();

    boolean existsByCodigo(String codigo);

    java.util.Optional<TipoAtencion> findByNombre(String nombre);
}
