package com.pe.finanzas.descuentos.repository;

import com.pe.finanzas.descuentos.entity.TipoPacienteServicio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPacienteServicioRepository extends JpaRepository<TipoPacienteServicio, Long> {

    @Query("SELECT tps FROM TipoPacienteServicio tps WHERE tps.tipoPaciente.idTipoPaciente = :idTipoPaciente")
    List<TipoPacienteServicio> findByTipoPacienteId(@Param("idTipoPaciente") Long idTipoPaciente);

    @Query("SELECT tps FROM TipoPacienteServicio tps WHERE tps.tipoPaciente.idTipoPaciente = :idTipoPaciente AND tps.tipoServicio = :tipoServicio")
    Optional<TipoPacienteServicio> findByTipoPacienteIdAndTipoServicio(
            @Param("idTipoPaciente") Long idTipoPaciente,
            @Param("tipoServicio") String tipoServicio);

    @Query("SELECT tps FROM TipoPacienteServicio tps WHERE tps.tipoPaciente.idTipoPaciente = :idTipoPaciente AND tps.estado = '1'")
    List<TipoPacienteServicio> findActivosByTipoPacienteId(@Param("idTipoPaciente") Long idTipoPaciente);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    void deleteByTipoPacienteIdTipoPaciente(Long idTipoPaciente);

    boolean existsByTipoPacienteIdTipoPacienteAndTipoServicio(Long idTipoPaciente, String tipoServicio);
}
