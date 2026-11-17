package com.pe.finanzas.puntos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.puntos.entity.PuntosDocumento;

@Repository
public interface PuntosDocumentoRepository extends JpaRepository<PuntosDocumento, Long> {

    // Buscar documentos de un punto
    List<PuntosDocumento> findByPuntoPunto(Long idPunto);

    // Buscar documentos activos de un punto
    List<PuntosDocumento> findByPuntoPuntoAndActivoTrue(Long idPunto);

    // Buscar por punto y tipo de documento
    Optional<PuntosDocumento> findByPuntoPuntoAndTipoDocumentoIdTipoDocumento(Long idPunto, Long idTipoDocumento);

    // Buscar por serie
    Optional<PuntosDocumento> findBySerie(String serie);

    // Verificar si existe combinación punto-tipo documento
    boolean existsByPuntoPuntoAndTipoDocumentoIdTipoDocumento(Long idPunto, Long idTipoDocumento);

    // Verificar si existe serie
    boolean existsBySerie(String serie);

    // Buscar documentos por tipo
    List<PuntosDocumento> findByTipoDocumentoIdTipoDocumento(Long idTipoDocumento);

    // Obtener documento activo por punto y tipo
    @Query("SELECT pd FROM PuntosDocumento pd WHERE pd.punto.punto = :idPunto " +
            "AND pd.tipoDocumento.idTipoDocumento = :idTipoDocumento AND pd.activo = true")
    Optional<PuntosDocumento> findActiveByPuntoAndTipo(@Param("idPunto") Long idPunto,
            @Param("idTipoDocumento") Long idTipoDocumento);

    // Contar documentos por punto
    long countByPuntoPunto(Long idPunto);

    // Eliminar documentos de un punto
    void deleteByPuntoPunto(Long idPunto);
}
