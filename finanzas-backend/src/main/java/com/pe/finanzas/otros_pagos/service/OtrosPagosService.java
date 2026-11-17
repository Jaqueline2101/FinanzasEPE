package com.pe.finanzas.otros_pagos.service;

import com.pe.finanzas.otros_pagos.dto.OtroPagoRequestDTO;
import com.pe.finanzas.otros_pagos.dto.OtroPagoResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OtrosPagosService {
    Object registrarOtroPago(OtroPagoRequestDTO dto);
    Page<OtroPagoResponseDTO> listarOtrosPagos(Long idSucursal, Pageable pageable);
}