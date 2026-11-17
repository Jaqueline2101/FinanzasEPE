package com.pe.finanzas.sucursales.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.sucursales.entity.Sucursal;

import java.util.List;
import java.util.Optional;

@Repository
public interface SucursalRepository extends JpaRepository<Sucursal, Long> {

    // ========== BÚSQUEDA SIN PAGINACIÓN ==========

    Optional<Sucursal> findByNombreSucursal(String nombreSucursal);

    boolean existsByNombreSucursal(String nombreSucursal);

    List<Sucursal> findByNombreSucursalContainingIgnoreCase(String nombreSucursal);

    List<Sucursal> findByEstado(Integer estado);

    @Query("SELECT s FROM Sucursal s WHERE s.estado = 1")
    List<Sucursal> findAllActivas();

    // ========== BÚSQUEDA CON PAGINACIÓN ==========

    Page<Sucursal> findByEstado(Integer estado, Pageable pageable);

    Page<Sucursal> findByNombreSucursalContainingIgnoreCase(String nombreSucursal, Pageable pageable);

    @Query("SELECT s FROM Sucursal s WHERE s.estado = 1")
    Page<Sucursal> findAllActivas(Pageable pageable);

    // ========== PERSONAL Y ESTADÍSTICAS ==========

    @Query("SELECT COUNT(dp) FROM DatosPersonales dp JOIN dp.sucursalesAsignadas s WHERE s.idSucursal = :idSucursal AND dp.active = true")
    Long countPersonalActivoBySucursal(@Param("idSucursal") Long idSucursal);

    @Query("SELECT s, (SELECT COUNT(dp) FROM DatosPersonales dp JOIN dp.sucursalesAsignadas sa WHERE sa.idSucursal = s.idSucursal AND dp.active = true) FROM Sucursal s")
    Page<Object[]> findSucursalesConCantidadPersonal(Pageable pageable);

    @Query("SELECT s, (SELECT COUNT(dp) FROM DatosPersonales dp JOIN dp.sucursalesAsignadas sa WHERE sa.idSucursal = s.idSucursal AND dp.active = true) FROM Sucursal s")
    List<Object[]> findSucursalesConCantidadPersonal();

    @Query("SELECT s FROM Sucursal s WHERE NOT EXISTS (SELECT dp FROM DatosPersonales dp JOIN dp.sucursalesAsignadas sa WHERE sa.idSucursal = s.idSucursal)")
    List<Sucursal> findSucursalesSinPersonal();

    // ========== CONSULTAS ESPECIALES ==========

    @Query("SELECT COUNT(s) FROM Sucursal s WHERE s.estado = :estado")
    Long countByEstado(@Param("estado") Integer estado);

    @Query("SELECT COUNT(s) FROM Sucursal s WHERE s.estado = 1")
    Long countActivas();
}
