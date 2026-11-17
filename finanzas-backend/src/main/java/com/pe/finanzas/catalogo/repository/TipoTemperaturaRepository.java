package com.pe.finanzas.catalogo.repository;

import com.pe.finanzas.catalogo.entity.TipoTemperatura;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface TipoTemperaturaRepository extends JpaRepository<TipoTemperatura, Long> {
    Optional<TipoTemperatura> findByNombreIgnoreCase(String nombre);
}
