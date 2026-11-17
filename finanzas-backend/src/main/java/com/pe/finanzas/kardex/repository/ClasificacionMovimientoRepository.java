package com.pe.finanzas.kardex.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pe.finanzas.kardex.entity.ClasificacionMovimiento;
import java.util.Optional;

@Repository
public interface ClasificacionMovimientoRepository extends JpaRepository<ClasificacionMovimiento, Long> {
    Optional<ClasificacionMovimiento> findByNombre(String nombre);
}
