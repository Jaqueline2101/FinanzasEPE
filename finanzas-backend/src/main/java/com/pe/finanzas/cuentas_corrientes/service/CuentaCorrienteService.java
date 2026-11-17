package com.pe.finanzas.cuentas_corrientes.service;

import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import java.util.List;
import java.util.Optional;

public interface CuentaCorrienteService {
    List<CuentaCorriente> findAll();

    List<CuentaCorriente> findActive();

    Optional<CuentaCorriente> findById(Long id);

    CuentaCorriente save(CuentaCorriente cuenta);

    CuentaCorriente update(Long id, CuentaCorriente cuenta);

    void delete(Long id); // Logical delete
}
