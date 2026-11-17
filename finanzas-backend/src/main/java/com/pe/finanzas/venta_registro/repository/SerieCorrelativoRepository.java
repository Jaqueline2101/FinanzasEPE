package com.pe.finanzas.venta_registro.repository;

import com.pe.finanzas.venta_registro.entity.SerieCorrelativo;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SerieCorrelativoRepository extends JpaRepository<SerieCorrelativo, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM SerieCorrelativo s WHERE s.idSucursal = :idSucursal AND s.tipoDoc = :tipoDoc AND s.serie = :serie")
    Optional<SerieCorrelativo> findBySucursalAndTipoAndSerieForUpdate(
            @Param("idSucursal") Long idSucursal,
            @Param("tipoDoc") String tipoDoc,
            @Param("serie") String serie);

    Optional<SerieCorrelativo> findByIdSucursalAndTipoDocAndSerie(Long idSucursal, String tipoDoc, String serie);
}
