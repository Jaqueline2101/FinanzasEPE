package com.pe.finanzas.puntos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.puntos.entity.Punto;

@Repository
public interface PuntoRepository extends JpaRepository<Punto, Long> {

    // Buscar por nombre
    Optional<Punto> findByNombre(String nombre);

    // Buscar por sucursal
    List<Punto> findBySucursalIdSucursal(Long idSucursal);

    Page<Punto> findBySucursalIdSucursal(Long idSucursal, Pageable pageable);

    // Buscar válidos (activos)
    @Query("SELECT p FROM Punto p WHERE p.valido = '1'")
    Page<Punto> findByActivoTrue(Pageable pageable);

    @Query("SELECT p FROM Punto p WHERE p.valido = '1'")
    List<Punto> findByActivoTrue();

    // Buscar por sucursal y válidos
    @Query("SELECT p FROM Punto p WHERE p.sucursal.idSucursal = :idSucursal AND p.valido = '1'")
    List<Punto> findBySucursalIdSucursalAndActivoTrue(@Param("idSucursal") Long idSucursal);

    // Verificar si existe por nombre y sucursal
    boolean existsByNombreAndSucursalIdSucursal(String nombre, Long idSucursal);

    // Buscar por nombre (búsqueda parcial)
    @Query("SELECT p FROM Punto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    Page<Punto> searchByNombre(@Param("nombre") String nombre, Pageable pageable);

    // Contar puntos por sucursal
    long countBySucursalIdSucursal(Long idSucursal);

    // Contar puntos activos por sucursal
    @Query("SELECT COUNT(p) FROM Punto p WHERE p.sucursal.idSucursal = :idSucursal AND p.valido = '1'")
    long countBySucursalIdSucursalAndActivoTrue(@Param("idSucursal") Long idSucursal);
}
