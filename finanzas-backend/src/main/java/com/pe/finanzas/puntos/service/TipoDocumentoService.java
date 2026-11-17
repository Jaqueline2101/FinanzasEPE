package com.pe.finanzas.puntos.service;

import java.util.List;

import com.pe.finanzas.puntos.dto.TipoDocumentoDto;

public interface TipoDocumentoService {

    // CRUD básico
    TipoDocumentoDto crear(TipoDocumentoDto dto);

    TipoDocumentoDto actualizar(Long id, TipoDocumentoDto dto);

    TipoDocumentoDto obtenerPorId(Long id);

    void eliminar(Long id);

    // Listados
    List<TipoDocumentoDto> listarTodos();

    List<TipoDocumentoDto> listarActivos();

    // Búsqueda
    TipoDocumentoDto buscarPorCodigo(String codigo);

    List<TipoDocumentoDto> buscarPorNombre(String nombre);

    // Validaciones
    boolean existePorCodigo(String codigo);
}
