package com.pe.finanzas.arqueo_caja.repository;

import com.pe.finanzas.arqueo_caja.entity.ArqueoCaja;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ArqueoCajaRepository extends JpaRepository<ArqueoCaja, Long> {

    Page<ArqueoCaja> findByIdSucursalOrderByFechaHoraDesc(Long idSucursal, Pageable pageable);

    Page<ArqueoCaja> findByIdSucursalAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long idSucursal, LocalDateTime start, LocalDateTime end, Pageable pageable);
            
    // New methods for more scoping
    Page<ArqueoCaja> findByIdSucursalAndIdPuntoOrderByFechaHoraDesc(Long idSucursal, Long idPunto, Pageable pageable);
    
    Page<ArqueoCaja> findByIdSucursalAndIdPuntoAndFechaHoraBetweenOrderByFechaHoraDesc(
            Long idSucursal, Long idPunto, LocalDateTime start, LocalDateTime end, Pageable pageable);
            
    Page<ArqueoCaja> findByIdUsuarioOrderByFechaHoraDesc(Long idUsuario, Pageable pageable);
            
    Optional<ArqueoCaja> findFirstByIdSucursalOrderByFechaHoraDesc(Long idSucursal);
    
    Optional<ArqueoCaja> findFirstByIdSucursalAndIdPuntoOrderByFechaHoraDesc(Long idSucursal, Long idPunto);
}
