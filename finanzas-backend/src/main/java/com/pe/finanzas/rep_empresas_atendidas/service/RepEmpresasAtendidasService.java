package com.pe.finanzas.rep_empresas_atendidas.service;

import com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO;

import java.time.LocalDate;
import java.util.List;

public interface RepEmpresasAtendidasService {
    List<EmpresaAtendidaDTO> getEmpresasMasAtendidas(LocalDate fechaInicio, LocalDate fechaFin);
}
