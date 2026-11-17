package com.pe.finanzas.niveles.service;

import com.pe.finanzas.niveles.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface NivelService {
    NivelDto crear(NivelCreateDto dto);

    NivelDto actualizar(Long id, NivelDto dto);

    void eliminar(Long id);

    Optional<NivelDto> obtenerPorId(Long id);

    Optional<NivelDto> obtenerPorCodigo(String codigo);

    List<NivelDto> obtenerTodos();

    Page<NivelDto> obtenerTodosPaginado(Pageable pageable);

    // Jerarquía
    List<NivelDto> obtenerNivelesRaiz();

    Page<NivelDto> obtenerNivelesRaizPaginado(Pageable pageable);

    List<NivelDto> obtenerHijos(Long idNivelPadre);

    List<NivelTreeDto> obtenerArbolCompleto();

    List<NivelTreeDto> obtenerArbolDesde(Long idNivel);

    // Operaciones especiales
    void moverNivel(NivelMoverDto dto);

    void cambiarOrden(Long idNivel, Integer nuevoOrden);

    EstadisticasNivelDto obtenerEstadisticas();

    // Tipos
    List<com.pe.finanzas.niveles.entity.TipoNivel> listarTiposActivos();

    // Búsquedas
    Page<NivelDto> buscarConFiltros(String nombre, String tipo, Integer estado,
            Long idNivelPadre, Pageable pageable);
}
