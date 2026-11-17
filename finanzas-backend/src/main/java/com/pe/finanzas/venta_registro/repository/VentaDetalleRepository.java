package com.pe.finanzas.venta_registro.repository;

import com.pe.finanzas.venta_registro.entity.VentaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para operaciones de detalle de venta
 */
@Repository
public interface VentaDetalleRepository extends JpaRepository<VentaDetalle, Long> {

    /**
     * Buscar detalles por ID de venta
     */
    @Query("SELECT vd FROM VentaDetalle vd WHERE vd.ventaRegistro.idVenta = :idVenta")
    List<VentaDetalle> findByIdVenta(@Param("idVenta") Long idVenta);

    /**
     * Buscar detalles por artículo/servicio
     */
    List<VentaDetalle> findByIdArticulo(Long idArticulo);

    /**
     * Contar items de una venta
     */
    @Query("SELECT COUNT(vd) FROM VentaDetalle vd WHERE vd.ventaRegistro.idVenta = :idVenta")
    long countByIdVenta(@Param("idVenta") Long idVenta);

    /**
     * Eliminar detalles de una venta
     */
    @Query("DELETE FROM VentaDetalle vd WHERE vd.ventaRegistro.idVenta = :idVenta")
    void deleteByIdVenta(@Param("idVenta") Long idVenta);
}
