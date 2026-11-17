package com.pe.finanzas.rep_comprobantes_anulados.service;

import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosRequestDTO;
import com.pe.finanzas.rep_comprobantes_anulados.dto.RepAnuladosResumenDTO;
import org.springframework.data.domain.Pageable;

public interface RepAnuladosService {
    RepAnuladosResumenDTO generarReporte(RepAnuladosRequestDTO request, Pageable pageable);
}
