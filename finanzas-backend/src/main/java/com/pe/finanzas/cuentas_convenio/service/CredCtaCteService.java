package com.pe.finanzas.cuentas_convenio.service;

import java.util.List;

import com.pe.finanzas.cuentas_convenio.dto.CredCtaCteDto;
import com.pe.finanzas.cuentas_convenio.entity.CredCtaCte;

public interface CredCtaCteService {
    CredCtaCte crearCuenta(CredCtaCteDto dto);

    CredCtaCte actualizarCuenta(Long id, CredCtaCteDto dto);

    void eliminarCuenta(Long id);

    CredCtaCte obtenerPorId(Long id);

    List<CredCtaCte> listarTodas();

    String previsualizarNroCtacte(Long idCompania);

    List<CredCtaCte> listarPorPaciente(Long idPaciente);
    
    List<CredCtaCte> listarPorDni(String dni);
}
