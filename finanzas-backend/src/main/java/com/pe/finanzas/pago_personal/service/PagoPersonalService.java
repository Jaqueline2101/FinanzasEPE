package com.pe.finanzas.pago_personal.service;

import com.pe.finanzas.pago_personal.dto.PagoPersonalRequest;
import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;

import java.util.List;

public interface PagoPersonalService {
    PagoComprobanteDto registrarPago(PagoPersonalRequest request);

    List<PagoComprobanteDto> listarPagos();

    PagoComprobanteDto obtenerPorId(Long id);
    List<PagoComprobanteDto> registrarPagosMasivos(List<PagoPersonalRequest> requests);
}
