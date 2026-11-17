package com.pe.finanzas.caja_general.repository;

import com.pe.finanzas.caja_general.entity.CajaGeneral;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CajaGeneralRepository extends JpaRepository<CajaGeneral, Long> {
    Optional<CajaGeneral> findByIdSucursal(Long idSucursal);
}
