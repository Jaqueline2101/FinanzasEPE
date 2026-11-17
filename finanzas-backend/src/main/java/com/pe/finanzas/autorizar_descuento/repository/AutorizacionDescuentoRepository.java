package com.pe.finanzas.autorizar_descuento.repository;

import com.pe.finanzas.autorizar_descuento.entity.AutorizacionDescuento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AutorizacionDescuentoRepository extends JpaRepository<AutorizacionDescuento, Long> {

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AutorizacionDescuento a " +
            "LEFT JOIN FETCH a.cajero " +
            "LEFT JOIN FETCH a.autorizador " +
            "WHERE a.estado = :estado ORDER BY a.fechaSolicitud DESC")
    List<AutorizacionDescuento> findByEstadoOrderByFechaSolicitudDesc(@org.springframework.data.repository.query.Param("estado") String estado);

    @org.springframework.data.jpa.repository.Query("SELECT a FROM AutorizacionDescuento a " +
            "LEFT JOIN FETCH a.cajero " +
            "LEFT JOIN FETCH a.autorizador " +
            "WHERE a.estado <> :estado ORDER BY a.fechaResolucion DESC")
    List<AutorizacionDescuento> findByEstadoNotOrderByFechaResolucionDesc(@org.springframework.data.repository.query.Param("estado") String estado);

        // Si necesitas filtrar por cajero
        // Si necesitas filtrar por cajero
        List<AutorizacionDescuento> findByCajeroIdOrderByFechaSolicitudDesc(Long idCajero);

    @org.springframework.data.jpa.repository.Query(value = "SELECT ad.* FROM autorizacion_descuento ad " +
            "JOIN datos_personales c on c.id = ad.id_cajero " +
            "WHERE (ad.fecha_resolucion BETWEEN :inicio AND :fin) AND " +
            "(:estado IS NULL OR ad.estado = :estado) AND " +
            "(:query IS NULL OR " +
            " LOWER(ad.paciente_nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(ad.paciente_dni::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(ad.doctor_nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(c.nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            " LOWER(c.apepat::text) LIKE LOWER(CONCAT('%', :query, '%')))",
            countQuery = "SELECT count(*) FROM autorizacion_descuento ad " +
                    "JOIN datos_personales c on c.id = ad.id_cajero " +
                    "WHERE (ad.fecha_resolucion BETWEEN :inicio AND :fin) AND " +
                    "(:estado IS NULL OR ad.estado = :estado) AND " +
                    "(:query IS NULL OR " +
                    " LOWER(ad.paciente_nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    " LOWER(ad.paciente_dni::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    " LOWER(ad.doctor_nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    " LOWER(c.nombre::text) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
                    " LOWER(c.apepat::text) LIKE LOWER(CONCAT('%', :query, '%')))",
            nativeQuery = true)
    org.springframework.data.domain.Page<AutorizacionDescuento> buscarParaReporte(
            @org.springframework.data.repository.query.Param("inicio") java.time.LocalDateTime inicio,
            @org.springframework.data.repository.query.Param("fin") java.time.LocalDateTime fin,
            @org.springframework.data.repository.query.Param("estado") String estado,
            @org.springframework.data.repository.query.Param("query") String query,
            org.springframework.data.domain.Pageable pageable);

}
