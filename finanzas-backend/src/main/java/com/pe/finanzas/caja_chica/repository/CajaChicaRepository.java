package com.pe.finanzas.caja_chica.repository;

import com.pe.finanzas.caja_chica.entity.CajaChica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CajaChicaRepository extends JpaRepository<CajaChica, Long> {
    Optional<CajaChica> findByNombreAndIdSucursal(String nombre, Long idSucursal);

    @Query(value = """
        SELECT * FROM caja_chica 
        WHERE estado = 'ABIERTA' 
        AND (id_usuario_cajero = :usuario OR id_usuario_cajero = :username) 
        AND (:sucursalId IS NULL OR id_sucursal = :sucursalId) 
        AND (:puntoId IS NULL OR id_punto_venta = :puntoId) 
        ORDER BY id DESC LIMIT 1
    """, nativeQuery = true)
    Optional<CajaChica> findOpenBoxNative(
            @Param("usuario") String usuario, 
            @Param("username") String username,
            @Param("puntoId") Long puntoId,
            @Param("sucursalId") Long sucursalId);

    Optional<CajaChica> findTopByEstadoAndIdPuntoVentaAndIdUsuarioCajeroOrderByIdDesc(
            CajaChica.EstadoCaja estado, Long idPuntoVenta, String idUsuarioCajero);

    Optional<CajaChica> findFirstByEstadoAndIdUsuarioCajeroOrderByIdDesc(
            CajaChica.EstadoCaja estado, String idUsuarioCajero);

    @Query(value = "SELECT COUNT(*) > 0 FROM caja_chica WHERE id_usuario_cajero = :usuario AND DATE(created_at) = CURRENT_DATE", nativeQuery = true)
    boolean existsCajaTodayForUser(@Param("usuario") String usuario);
}
