package com.pe.finanzas.nota_credito.repository;

import com.pe.finanzas.nota_credito.entity.NotaCredito;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotaCreditoRepository extends JpaRepository<NotaCredito, Long> {

    /** Listado paginado por sucursal */
    Page<NotaCredito> findByIdSucursalOrderByFechaCreacionDesc(Long idSucursal, Pageable pageable);

    /** Listado paginado por sucursal y estado */
    Page<NotaCredito> findByIdSucursalAndEstadoOrderByFechaCreacionDesc(
            Long idSucursal, String estado, Pageable pageable);

    /** Verificar si ya existe una NC activa para un comprobante original */
    Optional<NotaCredito> findByIdVentaRefAndEstado(Long idVentaRef, String estado);

    /**
     * Obtener el último número de la serie para generar el correlativo.
     * Ejemplo: SELECT MAX(CAST(numdoc_nc AS UNSIGNED)) FROM nota_credito WHERE
     * serie_nc = 'NC-B'
     */
    @Query("SELECT MAX(CAST(n.numdocNc AS integer)) FROM NotaCredito n WHERE n.serieNc = :serieNc")
    Optional<Integer> findMaxNumdocBySerie(@Param("serieNc") String serieNc);

    /** NC emitidas por una venta específica (historial) */
    Page<NotaCredito> findByIdVentaRefOrderByFechaCreacionDesc(Long idVentaRef, Pageable pageable);
}
