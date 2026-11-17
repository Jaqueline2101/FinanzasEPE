package com.pe.finanzas.autorizar_descuento.service;

import com.pe.finanzas.autorizar_descuento.dto.AutorizacionRequestDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResolucionDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResponseDto;

import java.util.List;

public interface AutorizacionDescuentoService {

    AutorizacionResponseDto solicitarDescuento(AutorizacionRequestDto dto);

    AutorizacionResponseDto aprobarDescuento(Long idAutorizacion, AutorizacionResolucionDto dto);

    AutorizacionResponseDto rechazarDescuento(Long idAutorizacion, AutorizacionResolucionDto dto);

    List<AutorizacionResponseDto> listarPendientes();

    List<AutorizacionResponseDto> listarResueltos();

    List<AutorizacionResponseDto> listarPorCajero(Long idCajero);

}
