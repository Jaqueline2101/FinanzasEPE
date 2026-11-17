package com.pe.finanzas.paquetes.repository;

import com.pe.finanzas.paquetes.entity.PaqueteDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repositorio para consultas de detalles de paquetes
 */
@Repository
public interface PaqueteDetalleRepository extends JpaRepository<PaqueteDetalle, Long> {

    /**
     * Listar detalles de un paquete
     */
    @Query("SELECT pd FROM PaqueteDetalle pd WHERE pd.paquete.idPaquete = :idPaquete")
    List<PaqueteDetalle> findByPaqueteIdOrdenado(@Param("idPaquete") Long idPaquete);

    /**
     * Contar servicios en un paquete
     */
    @Query("SELECT COUNT(pd) FROM PaqueteDetalle pd WHERE pd.paquete.idPaquete = :idPaquete")
    long countByPaqueteId(@Param("idPaquete") Long idPaquete);

    /**
     * Eliminar detalles de un paquete
     */
    void deleteByPaqueteIdPaquete(Long idPaquete);
}
