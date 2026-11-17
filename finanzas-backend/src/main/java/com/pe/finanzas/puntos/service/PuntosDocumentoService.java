package com.pe.finanzas.puntos.service;

import java.util.List;

import com.pe.finanzas.puntos.dto.PuntosDocumentoDto;
import com.pe.finanzas.puntos.dto.SerieCorrelativoDto;

public interface PuntosDocumentoService {

    // CRUD básico
    PuntosDocumentoDto asignarDocumentoAPunto(Long idPunto, PuntosDocumentoDto dto);

    PuntosDocumentoDto actualizar(Long id, PuntosDocumentoDto dto);

    PuntosDocumentoDto obtenerPorId(Long id);

    void eliminar(Long id);

    // Listados
    List<PuntosDocumentoDto> listarPorPunto(Long idPunto);

    List<PuntosDocumentoDto> listarActivosPorPunto(Long idPunto);

    // Gestión de correlativos
    SerieCorrelativoDto obtenerSiguienteNumero(Long idPuntoDocumento);

    SerieCorrelativoDto obtenerSiguienteNumeroPorPuntoYTipo(Long idPunto, Long idTipoDocumento);

    void incrementarCorrelativo(Long idPuntoDocumento);

    // Activar/Desactivar
    void cambiarEstado(Long id, Boolean activo);

    // Validaciones
    boolean existeDocumentoEnPunto(Long idPunto, Long idTipoDocumento);

    boolean existeSerie(String serie);
}
