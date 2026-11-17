package com.pe.finanzas.compra.repository;

import com.pe.finanzas.compra.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface CompraRepository extends JpaRepository<Compra, Long>, JpaSpecificationExecutor<Compra> {

    Page<Compra> findByIdSucursal(Long idSucursal, Pageable pageable);

    @Query("SELECT c FROM Compra c WHERE " +
            "(cast(:fechaInicio as LocalDate) IS NULL OR c.fechaEmision >= :fechaInicio) AND " +
            "(cast(:fechaFin as LocalDate) IS NULL OR c.fechaEmision <= :fechaFin) AND " +
            "(cast(:idSucursal as Long) IS NULL OR c.idSucursal = :idSucursal) AND " +
            "(cast(:idProveedor as Long) IS NULL OR c.proveedor.id = :idProveedor) AND " +
            "(:estado IS NULL OR c.estado = :estado)")
    Page<Compra> findReporteCompras(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idSucursal") Long idSucursal,
            @Param("idProveedor") Long idProveedor,
            @Param("estado") Integer estado,
            Pageable pageable);

    @Query("SELECT c FROM Compra c WHERE " +
            "(cast(:fechaInicio as LocalDate) IS NULL OR c.fechaEmision >= :fechaInicio) AND " +
            "(cast(:fechaFin as LocalDate) IS NULL OR c.fechaEmision <= :fechaFin) AND " +
            "(cast(:idSucursal as Long) IS NULL OR c.idSucursal = :idSucursal) AND " +
            "(cast(:idProveedor as Long) IS NULL OR c.proveedor.id = :idProveedor) AND " +
            "(:estado IS NULL OR c.estado = :estado)")
    List<Compra> findResumenReporteCompras(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idSucursal") Long idSucursal,
            @Param("idProveedor") Long idProveedor,
            @Param("estado") Integer estado);
}
