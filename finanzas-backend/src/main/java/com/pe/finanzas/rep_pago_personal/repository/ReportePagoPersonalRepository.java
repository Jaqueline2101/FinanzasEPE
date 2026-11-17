package com.pe.finanzas.rep_pago_personal.repository;

import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportePagoPersonalRepository extends JpaRepository<PagoComprobante, Long> {

    @Query("SELECT SUM(p.montoBruto), SUM(p.montoNeto), SUM(p.otrosDescuentos), COUNT(p.idPago) " +
           "FROM PagoComprobante p WHERE p.tipoBeneficiario = 'TRABAJADOR' " +
           "AND p.periodoAnio = :anio AND p.estadoPago != 'ANULADO'")
    Object[] getResumenPersonal(@Param("anio") Integer anio);

    @Query("SELECT p.periodoMes, SUM(p.montoNeto), COUNT(p.idPago) " +
           "FROM PagoComprobante p WHERE p.tipoBeneficiario = 'TRABAJADOR' " +
           "AND p.periodoAnio = :anio AND p.estadoPago != 'ANULADO' " +
           "GROUP BY p.periodoMes")
    List<Object[]> getReporteMensualPersonal(@Param("anio") Integer anio);

    @Query("SELECT p.areaTrabajador, SUM(p.montoNeto), COUNT(p.idPago) " +
           "FROM PagoComprobante p WHERE p.tipoBeneficiario = 'TRABAJADOR' " +
           "AND p.periodoAnio = :anio AND p.estadoPago != 'ANULADO' " +
           "GROUP BY p.areaTrabajador")
    List<Object[]> getReporteAreaPersonal(@Param("anio") Integer anio);
}
