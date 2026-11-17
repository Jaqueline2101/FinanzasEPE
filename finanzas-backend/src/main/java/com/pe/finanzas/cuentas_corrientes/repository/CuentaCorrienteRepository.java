package com.pe.finanzas.cuentas_corrientes.repository;

import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CuentaCorrienteRepository extends JpaRepository<CuentaCorriente, Long> {
    java.util.Optional<CuentaCorriente> findByNumeroCuenta(String numeroCuenta);

    java.util.List<CuentaCorriente> findByNombreTitular(String nombreTitular);

    List<CuentaCorriente> findByEstado(String estado);
}
