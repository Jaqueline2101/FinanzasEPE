package com.pe.finanzas.puntos.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.pe.finanzas.puntos.dto.PuntoDto;

public interface PuntoService {

    // CRUD básico
    PuntoDto crear(PuntoDto puntoDto);

    PuntoDto actualizar(Long id, PuntoDto puntoDto);

    PuntoDto obtenerPorId(Long id);

    void eliminar(Long id);

    // Listados
    Page<PuntoDto> listarTodos(Pageable pageable);

    List<PuntoDto> listarActivos();

    List<PuntoDto> listarPorSucursal(Long idSucursal);

    Page<PuntoDto> listarPorSucursal(Long idSucursal, Pageable pageable);

    List<PuntoDto> listarActivosPorSucursal(Long idSucursal);

    // Búsqueda
    Page<PuntoDto> buscarPorNombre(String nombre, Pageable pageable);

    // Activar/Desactivar
    void cambiarEstado(Long id, Boolean activo);

    // Validaciones
    boolean existePorNombreYSucursal(String nombrePunto, Long idSucursal);

    // Estadísticas
    long contarPorSucursal(Long idSucursal);

    long contarActivosPorSucursal(Long idSucursal);
}
