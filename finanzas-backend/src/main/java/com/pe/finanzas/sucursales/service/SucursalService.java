package com.pe.finanzas.sucursales.service;

import org.springframework.data.domain.Pageable;

import com.pe.finanzas.sucursales.dto.SucursalDto;
import com.pe.finanzas.shared.dto.PageResponse;

import java.util.List;
import java.util.Optional;

public interface SucursalService {

    // ========== CRUD BÁSICO ==========

    SucursalDto crear(SucursalDto dto);

    SucursalDto actualizar(Long id, SucursalDto dto);

    void eliminar(Long id);

    Optional<SucursalDto> obtenerPorId(Long id);

    // ========== LISTADOS CON PAGINACIÓN ==========

    PageResponse<SucursalDto> obtenerTodas(Pageable pageable);

    PageResponse<SucursalDto> obtenerPorEstado(Integer estado, Pageable pageable);

    PageResponse<SucursalDto> buscarPorNombre(String nombre, Pageable pageable);

    PageResponse<SucursalDto> obtenerConPersonal(Pageable pageable);

    // ========== LISTADOS SIN PAGINACIÓN ==========

    List<SucursalDto> obtenerTodasAsList();

    List<SucursalDto> obtenerActivasAsList();

    List<SucursalDto> obtenerPorEstadoAsList(Integer estado);

    List<SucursalDto> obtenerSinPersonalAsList();

    // ========== BÚSQUEDAS ESPECIALIZADAS ==========

    Optional<SucursalDto> obtenerPorNombre(String nombre);

    List<SucursalDto> obtenerConCantidadPersonal();

    List<SucursalDto> obtenerSinPersonal();

    // ========== UTILIDADES ==========

    Long contarPorEstado(Integer estado);

    Long contarActivas();

    Long contarTotal();

    boolean existePorNombre(String nombre);

    // ========== OPERACIONES ESPECIALES ==========

    void cambiarEstado(Long id, Integer nuevoEstado);

    void activar(Long id);

    void inactivar(Long id);
}
