package com.pe.finanzas.activos_fijos.service;

import com.pe.finanzas.activos_fijos.entity.Activo;
import java.util.List;
import java.util.Optional;

public interface ActivoService {
    List<Activo> findAll();

    Optional<Activo> findById(Long id);

    Optional<Activo> findByCodigoPatrimonial(String codigo);

    Activo save(Activo activo);

    Activo update(Long id, Activo activo);

    void delete(Long id);
}
