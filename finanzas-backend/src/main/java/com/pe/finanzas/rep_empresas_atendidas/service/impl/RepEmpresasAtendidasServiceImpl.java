package com.pe.finanzas.rep_empresas_atendidas.service.impl;

import com.pe.finanzas.rep_empresas_atendidas.dto.EmpresaAtendidaDTO;
import com.pe.finanzas.rep_empresas_atendidas.service.RepEmpresasAtendidasService;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RepEmpresasAtendidasServiceImpl implements RepEmpresasAtendidasService {

    private final VentaRegistroRepository ventaRegistroRepository;

    @Override
    public List<EmpresaAtendidaDTO> getEmpresasMasAtendidas(LocalDate fechaInicio, LocalDate fechaFin) {
        return ventaRegistroRepository.findEmpresasMasAtendidas(fechaInicio, fechaFin);
    }
}
