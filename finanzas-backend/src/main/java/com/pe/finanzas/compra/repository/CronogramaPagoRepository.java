package com.pe.finanzas.compra.repository;

import com.pe.finanzas.compra.entity.CronogramaPago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CronogramaPagoRepository extends JpaRepository<CronogramaPago, Long> {
    List<CronogramaPago> findByCompraId(Long idCompra);

    List<CronogramaPago> findByFechaVencimientoBetweenAndEstado(LocalDate inicio, LocalDate fin, Integer estado);

    List<CronogramaPago> findByFechaVencimientoAndEstado(LocalDate fecha, Integer estado);
}
