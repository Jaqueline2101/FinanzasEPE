package com.pe.finanzas.plan_cuentas_pmt.service;

import java.util.List;

import com.pe.finanzas.plan_cuentas_pmt.dto.HistorialMovimientoDTO;
import com.pe.finanzas.plan_cuentas_pmt.dto.PlanCuentasRegistroDTO;

public interface PlanCuentasService {
    PlanCuentasRegistroDTO registrar(PlanCuentasRegistroDTO dto);

    PlanCuentasRegistroDTO obtenerPorId(Long id, String tipoRegistro);

    List<HistorialMovimientoDTO> obtenerHistorial(Long id);
    
    void eliminar(Long id);

    List<PlanCuentasRegistroDTO> listarTodos();
}
