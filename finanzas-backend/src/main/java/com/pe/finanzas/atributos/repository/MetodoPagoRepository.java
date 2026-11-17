package com.pe.finanzas.atributos.repository;

import com.pe.finanzas.atributos.entity.MetodoPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MetodoPagoRepository extends JpaRepository<MetodoPago, Long> {
    Optional<MetodoPago> findByDescripcionIgnoreCase(String descripcion);
}
