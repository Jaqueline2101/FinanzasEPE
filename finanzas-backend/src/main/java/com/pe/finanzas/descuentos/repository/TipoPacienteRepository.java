package com.pe.finanzas.descuentos.repository;

import com.pe.finanzas.descuentos.entity.TipoPaciente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TipoPacienteRepository extends JpaRepository<TipoPaciente, Long> {

    Optional<TipoPaciente> findByCodigo(String codigo);

    boolean existsByCodigo(String codigo);

    List<TipoPaciente> findByEstado(Integer estado);

    @Query("SELECT tp FROM TipoPaciente tp WHERE tp.dsc > 0 AND tp.estado = 1")
    List<TipoPaciente> findActivosConDescuento();

    @Query("SELECT tp FROM TipoPaciente tp WHERE tp.cob > 0 AND tp.estado = 1")
    List<TipoPaciente> findActivosConCobertura();

    @Query("SELECT tp FROM TipoPaciente tp WHERE tp.frq = '1' AND tp.estado = 1")
    List<TipoPaciente> findFrecuentes();
}
