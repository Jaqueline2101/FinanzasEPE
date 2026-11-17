package com.pe.finanzas.venta_registro.repository;

import com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface VentaRegistroRepository extends JpaRepository<VentaRegistro, Long> {

        Optional<VentaRegistro> findByIdVenta(Long idVenta);

        Optional<VentaRegistro> findBySerieAndNumdoc(String serie, String numdoc);

        List<VentaRegistro> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);

        Page<VentaRegistro> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable);

        List<VentaRegistro> findByIdSucursal(Long idSucursal);

        @Query("SELECT v FROM VentaRegistro v WHERE v.idSucursal = :idSucursal AND v.fecha BETWEEN :fechaInicio AND :fechaFin")
        Page<VentaRegistro> findByIdSucursalAndFechaBetween(
                        @Param("idSucursal") Long idSucursal,
                        @Param("fechaInicio") LocalDate fechaInicio,
                        @Param("fechaFin") LocalDate fechaFin,
                        Pageable pageable);

        List<VentaRegistro> findByIdCtacte(Long idCtacte);

        List<VentaRegistro> findByEstado(Integer estado);

        long countByFecha(LocalDate fecha);

        boolean existsBySerieAndNumdoc(String serie, String numdoc);

        boolean existsByIdOrden(Long idOrden);

        @Query("SELECT MAX(v.numdoc) FROM VentaRegistro v WHERE v.serie = :serie")
        String findMaxNumdocBySerie(@Param("serie") String serie);

    @Query("SELECT v FROM VentaRegistro v " +
            "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "AND (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
            "AND (:idPersonal IS NULL OR v.idPersonalUser = :idPersonal) " +
            "AND (:estado IS NULL OR v.estado = :estado) " +
            "AND (:tipoAtencion IS NULL OR v.tipoAtencion = :tipoAtencion) " +
            "AND (:query IS NULL OR v.nombrePac LIKE %:query% OR v.numdoc LIKE %:query% OR v.serie LIKE %:query%)")
    Page<VentaRegistro> findReporteVentas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idSucursal") Long idSucursal,
            @Param("idPersonal") Long idPersonal,
            @Param("estado") Integer estado,
            @Param("tipoAtencion") String tipoAtencion,
            @Param("query") String query,
            Pageable pageable);

    @Query("SELECT v FROM VentaRegistro v " +
            "WHERE v.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "AND (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
            "AND (:idPersonal IS NULL OR v.idPersonalUser = :idPersonal) " +
            "AND (:estado IS NULL OR v.estado = :estado) " +
            "AND (:tipoAtencion IS NULL OR v.tipoAtencion = :tipoAtencion) " +
            "AND (:query IS NULL OR v.nombrePac LIKE %:query% OR v.numdoc LIKE %:query% OR v.serie LIKE %:query%)")
    List<VentaRegistro> findResumenReporteVentas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin,
            @Param("idSucursal") Long idSucursal,
            @Param("idPersonal") Long idPersonal,
            @Param("estado") Integer estado,
            @Param("tipoAtencion") String tipoAtencion,
            @Param("query") String query);

        @Query("SELECT MONTH(v.fecha), v.tipoDoc, v.serie, COUNT(v), MIN(v.numdoc), MAX(v.numdoc), SUM(v.baseImp), SUM(v.igv), SUM(v.total) "
                        +
                        "FROM VentaRegistro v " +
                        "WHERE (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
                        "AND YEAR(v.fecha) = :anio " +
                        "AND (:mes IS NULL OR MONTH(v.fecha) = :mes) " +
                        "AND v.estado != 10 " +
                        "GROUP BY MONTH(v.fecha), v.tipoDoc, v.serie " +
                        "ORDER BY MONTH(v.fecha) ASC, v.tipoDoc, v.serie")
        List<Object[]> findCorrelatividadResumen(
                        @Param("idSucursal") Long idSucursal,
                        @Param("anio") Integer anio,
                        @Param("mes") Integer mes);

    @Query("SELECT v FROM VentaRegistro v " +
            "WHERE (:idSucursal IS NULL OR v.idSucursal = :idSucursal) " +
            "AND YEAR(v.fecha) = :anio " +
            "AND (:mes IS NULL OR MONTH(v.fecha) = :mes) " +
            "AND v.tipoDoc = :tipoDoc " +
            "AND v.serie = :serie " +
            "AND v.estado != 10 " +
            "ORDER BY v.numdoc ASC")
    List<VentaRegistro> findCorrelatividadDetalle(
            @Param("idSucursal") Long idSucursal,
            @Param("anio") Integer anio,
            @Param("mes") Integer mes,
            @Param("tipoDoc") String tipoDoc,
            @Param("serie") String serie);

    @Query("SELECT new com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO(" +
            "c.id, " +
            "c.nombre, " +
            "COUNT(v), " +
            "COALESCE(SUM(v.total), 0) " +
            ") " +
            "FROM VentaRegistro v " +
            "INNER JOIN DatosPersonales dp ON v.idCtacte = dp.id " +
            "LEFT JOIN CredPlan p ON v.idPlan = p.idPlan " +
            "INNER JOIN Compania c ON c.id = COALESCE(p.idCompania, dp.idCompania) " +
            "WHERE v.estado != 10 " +
            "AND v.fecha BETWEEN :fechaInicio AND :fechaFin " +
            "GROUP BY c.id, c.nombre " +
            "ORDER BY COUNT(v) DESC")
    List<EmpresaAtendidaDTO> findEmpresasMasAtendidas(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin
    );
}
