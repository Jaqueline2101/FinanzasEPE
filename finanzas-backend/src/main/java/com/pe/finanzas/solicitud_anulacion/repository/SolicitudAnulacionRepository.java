package com.pe.finanzas.solicitud_anulacion.repository;

import com.pe.finanzas.solicitud_anulacion.entity.SolicitudAnulacion;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SolicitudAnulacionRepository extends JpaRepository<SolicitudAnulacion, Long> {

    /** Bandeja del supervisor: solicitudes por estado */
    Page<SolicitudAnulacion> findByEstadoOrderByFechaCreacionDesc(String estado, Pageable pageable);

    /** Solicitudes de una sucursal específica */
    Page<SolicitudAnulacion> findByIdSucursalOrderByFechaCreacionDesc(Long idSucursal, Pageable pageable);

    /** Solicitudes de una sucursal filtradas por estado */
    Page<SolicitudAnulacion> findByIdSucursalAndEstadoOrderByFechaCreacionDesc(
            Long idSucursal, String estado, Pageable pageable);

    /** Historial de un cajero específico */
    List<SolicitudAnulacion> findByIdSolicitanteOrderByFechaCreacionDesc(Long idSolicitante);

    /** Verificar si ya existe una solicitud PENDIENTE para la misma venta */
    Optional<SolicitudAnulacion> findByIdVentaAndEstado(Long idVenta, String estado);

    /** Contar solicitudes PENDIENTES por sucursal (para el badge del sidebar) */
    @Query("SELECT COUNT(s) FROM SolicitudAnulacion s WHERE s.idSucursal = :idSucursal AND s.estado = 'PENDIENTE'")
    long countPendientesBySucursal(@Param("idSucursal") Long idSucursal);
}
