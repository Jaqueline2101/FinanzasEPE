package com.pe.finanzas.accesos.repository;

import com.pe.finanzas.accesos.entity.AccesoMain;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AccesoMainRepository extends JpaRepository<AccesoMain, Long> {
    java.util.List<AccesoMain> findByNombreIgnoreCase(String nombre);
    java.util.Optional<AccesoMain> findFirstByNombreIgnoreCase(String nombre);
}
