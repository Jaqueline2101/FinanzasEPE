package com.pe.finanzas.pagorec_honorarios.service;

import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;
import com.pe.finanzas.pagorec_honorarios.dto.PagoHonorariosRequest;

import java.util.List;

public interface PagoHonorariosService {
    PagoComprobanteDto registrarPago(PagoHonorariosRequest request);

    List<PagoComprobanteDto> listarPagos();

    PagoComprobanteDto obtenerPorId(Long id);
}
