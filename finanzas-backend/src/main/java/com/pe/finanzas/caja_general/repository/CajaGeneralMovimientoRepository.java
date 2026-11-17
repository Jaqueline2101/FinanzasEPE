package com.pe.finanzas.caja_general.repository;

import com.pe.finanzas.caja_general.entity.CajaGeneralMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CajaGeneralMovimientoRepository extends JpaRepository<CajaGeneralMovimiento, Long> {
    Page<CajaGeneralMovimiento> findByCajaGeneralIdOrderByFechaDesc(Long cajaGeneralId, Pageable pageable);

    Page<CajaGeneralMovimiento> findByCajaGeneralIdAndMetodoPagoDescripcionOrderByFechaDesc(Long cajaGeneralId,
            String metodoPago, Pageable pageable);
}
