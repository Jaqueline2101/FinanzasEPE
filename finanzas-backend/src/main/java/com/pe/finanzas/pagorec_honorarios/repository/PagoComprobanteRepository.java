package com.pe.finanzas.pagorec_honorarios.repository;

import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface PagoComprobanteRepository extends JpaRepository<PagoComprobante, Long> {
    List<PagoComprobante> findByFechaEmisionBetween(LocalDate fechaInicio, LocalDate fechaFin);

    List<PagoComprobante> findByNumeroDocumento(String numeroDocumento);

    @Query("SELECT p FROM PagoComprobante p " +
            "WHERE (p.fechaEmision BETWEEN :fechaInicio AND :fechaFin OR p.fechaPago BETWEEN :fechaInicio AND :fechaFin) " +
            "AND (cast(:tipoPago as string) IS NULL OR p.tipoPago = cast(:tipoPago as string)) " +
            "AND (cast(:tipoBeneficiario as string) IS NULL OR p.tipoBeneficiario = cast(:tipoBeneficiario as string)) " +
            "AND (cast(:estadoPago as string) IS NULL OR p.estadoPago = cast(:estadoPago as string)) " +
            "AND (cast(:query as string) IS NULL OR (" +
            " LOWER(p.nombreRazonSocial) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
            " LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
            " LOWER(p.conceptoPago) LIKE LOWER(CONCAT('%', cast(:query as string), '%'))))")
    org.springframework.data.domain.Page<PagoComprobante> findReporteHonorarios(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin,
            @Param("tipoPago") String tipoPago,
            @Param("tipoBeneficiario") String tipoBeneficiario,
            @Param("estadoPago") String estadoPago,
            @Param("query") String query,
            org.springframework.data.domain.Pageable pageable);

    @Query("SELECT p FROM PagoComprobante p " +
            "WHERE (p.fechaEmision BETWEEN :fechaInicio AND :fechaFin OR p.fechaPago BETWEEN :fechaInicio AND :fechaFin) " +
            "AND (cast(:tipoPago as string) IS NULL OR p.tipoPago = cast(:tipoPago as string)) " +
            "AND (cast(:tipoBeneficiario as string) IS NULL OR p.tipoBeneficiario = cast(:tipoBeneficiario as string)) " +
            "AND (cast(:estadoPago as string) IS NULL OR p.estadoPago = cast(:estadoPago as string)) " +
            "AND (cast(:query as string) IS NULL OR (" +
            " LOWER(p.nombreRazonSocial) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
            " LOWER(p.numeroDocumento) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR " +
            " LOWER(p.conceptoPago) LIKE LOWER(CONCAT('%', cast(:query as string), '%'))))")
    List<PagoComprobante> findResumenReporteHonorarios(
            @Param("fechaInicio") java.time.LocalDate fechaInicio,
            @Param("fechaFin") java.time.LocalDate fechaFin,
            @Param("tipoPago") String tipoPago,
            @Param("tipoBeneficiario") String tipoBeneficiario,
            @Param("estadoPago") String estadoPago,
            @Param("query") String query);
}
