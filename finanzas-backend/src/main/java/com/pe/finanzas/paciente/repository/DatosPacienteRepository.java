package com.pe.finanzas.paciente.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.paciente.entity.DatosPaciente;

@Repository
public interface DatosPacienteRepository extends JpaRepository<DatosPaciente, Long> {
    Optional<DatosPaciente> findByDatosPersonalesId(Long idDatosPersonales);
}
