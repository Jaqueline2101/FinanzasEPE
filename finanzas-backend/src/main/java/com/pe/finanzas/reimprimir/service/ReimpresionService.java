package com.pe.finanzas.reimprimir.service;

import com.pe.finanzas.reimprimir.dto.DocumentoReimpresionDTO;
import com.pe.finanzas.reimprimir.dto.ReimpresionFilterDTO;

import java.util.List;

public interface ReimpresionService {
    List<DocumentoReimpresionDTO> buscarDocumentos(ReimpresionFilterDTO filtros);
}
