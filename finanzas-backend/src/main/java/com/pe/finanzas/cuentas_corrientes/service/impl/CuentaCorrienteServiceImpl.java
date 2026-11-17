package com.pe.finanzas.cuentas_corrientes.service.impl;

import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.repository.CuentaCorrienteRepository;
import com.pe.finanzas.cuentas_corrientes.service.CuentaCorrienteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CuentaCorrienteServiceImpl implements CuentaCorrienteService {

    private final CuentaCorrienteRepository repository;

    @Override
    @Transactional(readOnly = true)
    public List<CuentaCorriente> findAll() {
        return repository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuentaCorriente> findActive() {
        return repository.findByEstado("A");
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CuentaCorriente> findById(Long id) {
        return repository.findById(id);
    }

    @Override
    @Transactional
    public CuentaCorriente save(CuentaCorriente cuenta) {
        if (cuenta.getEstado() == null) {
            cuenta.setEstado("A");
        }
        return repository.save(cuenta);
    }

    @Override
    @Transactional
    public CuentaCorriente update(Long id, CuentaCorriente cuentaDetails) {
        return repository.findById(id).map(cuenta -> {
            cuenta.setNombreTitular(cuentaDetails.getNombreTitular());
            cuenta.setBanco(cuentaDetails.getBanco());
            cuenta.setNumeroCuenta(cuentaDetails.getNumeroCuenta());
            cuenta.setCci(cuentaDetails.getCci());
            cuenta.setMoneda(cuentaDetails.getMoneda());
            cuenta.setAlias(cuentaDetails.getAlias());
            cuenta.setEstado(cuentaDetails.getEstado());
            cuenta.setCuentaContable(cuentaDetails.getCuentaContable());
            cuenta.setCodigoSBS(cuentaDetails.getCodigoSBS());

            if (cuentaDetails.getSaldo() != null) {
                cuenta.setSaldo(cuentaDetails.getSaldo());
            }
            return repository.save(cuenta);
        }).orElseThrow(() -> new RuntimeException("Cuenta Corriente no encontrada con id " + id));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        repository.findById(id).ifPresent(cuenta -> {
            cuenta.setEstado("I");
            repository.save(cuenta);
        });
    }
}
