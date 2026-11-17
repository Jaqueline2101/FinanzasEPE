package com.pe.finanzas.caja_chica.repository;

import com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CajaChicaMovimientoRepository extends JpaRepository<CajaChicaMovimiento, Long> {
    Page<CajaChicaMovimiento> findByCajaChicaIdOrderByFechaDesc(Long cajaChicaId, Pageable pageable);

    List<CajaChicaMovimiento> findByCajaChicaId(Long cajaChicaId);

    @Query("SELECT m.fecha, SUM(m.monto) " +
            "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'INGRESO' " +
            "GROUP BY m.fecha ORDER BY m.fecha")
    List<Object[]> ingresosDiariosRaw(@Param("idSucursal") Long idSucursal);

    @Query("SELECT m.fecha, SUM(m.monto) " +
            "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'EGRESO' " +
            "GROUP BY m.fecha ORDER BY m.fecha")
    List<Object[]> egresosDiariosRaw(@Param("idSucursal") Long idSucursal);

    @Query("SELECT m.metodoPago.descripcion, SUM(m.monto) " +
            "FROM CajaChicaMovimiento m WHERE m.cajaChica.idSucursal = :idSucursal " +
            "GROUP BY m.metodoPago.descripcion")
    List<Object[]> movimientosPorMetodoPagoRaw(@Param("idSucursal") Long idSucursal);

    @Query("SELECT SUM(m.monto) FROM CajaChicaMovimiento m " +
           "WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'INGRESO' " +
           "AND m.fecha BETWEEN :start AND :end")
    java.math.BigDecimal sumIngresosBySucursalAndRange(@Param("idSucursal") Long idSucursal, 
            @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);

    @Query("SELECT SUM(m.monto) FROM CajaChicaMovimiento m " +
           "WHERE m.cajaChica.idSucursal = :idSucursal AND m.tipo = 'EGRESO' " +
           "AND m.fecha BETWEEN :start AND :end")
    java.math.BigDecimal sumEgresosBySucursalAndRange(@Param("idSucursal") Long idSucursal, 
            @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
            
    // New queries for caja-specific calculations!
    @Query("SELECT SUM(m.monto) FROM CajaChicaMovimiento m " +
           "WHERE m.cajaChica.id = :cajaId AND m.tipo = 'INGRESO' " +
           "AND m.fecha BETWEEN :start AND :end")
    java.math.BigDecimal sumIngresosByCajaAndRange(@Param("cajaId") Long cajaId, 
            @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
            
    @Query("SELECT SUM(m.monto) FROM CajaChicaMovimiento m " +
           "WHERE m.cajaChica.id = :cajaId AND m.tipo = 'EGRESO' " +
           "AND m.fecha BETWEEN :start AND :end")
    java.math.BigDecimal sumEgresosByCajaAndRange(@Param("cajaId") Long cajaId, 
            @Param("start") java.time.LocalDateTime start, @Param("end") java.time.LocalDateTime end);
}
