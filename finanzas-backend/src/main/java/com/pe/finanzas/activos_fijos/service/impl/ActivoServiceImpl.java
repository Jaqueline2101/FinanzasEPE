package com.pe.finanzas.activos_fijos.service.impl;

import com.pe.finanzas.activos_fijos.entity.Activo;
import com.pe.finanzas.activos_fijos.repository.ActivoRepository;
import com.pe.finanzas.activos_fijos.service.ActivoService;
import com.pe.finanzas.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ActivoServiceImpl implements ActivoService {

    private final ActivoRepository activoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Activo> findAll() {
        return activoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Activo> findById(Long id) {
        return activoRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Activo> findByCodigoPatrimonial(String codigo) {
        return activoRepository.findByCodigoPatrimonial(codigo);
    }

    @Override
    @Transactional
    public Activo save(Activo activo) {
        // Aquí se podrían agregar validaciones de negocio (ej: serie duplicada)
        return activoRepository.save(activo);
    }

    @Override
    @Transactional
    public Activo update(Long id, Activo activoDetails) {
        Activo activo = activoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));

        // Actualizar campos permitidos
        activo.setCatalogo(activoDetails.getCatalogo());
        activo.setCodigoPatrimonial(activoDetails.getCodigoPatrimonial());
        activo.setNumeroSerie(activoDetails.getNumeroSerie());
        activo.setEstadoFisico(activoDetails.getEstadoFisico());
        activo.setSituacion(activoDetails.getSituacion());
        activo.setValorCompra(activoDetails.getValorCompra());
        activo.setFechaCompra(activoDetails.getFechaCompra());
        activo.setFechaInicioUso(activoDetails.getFechaInicioUso());
        activo.setVidaUtilAnios(activoDetails.getVidaUtilAnios());
        activo.setUbicacionDetallada(activoDetails.getUbicacionDetallada());
        activo.setIdResponsable(activoDetails.getIdResponsable());
        activo.setObservaciones(activoDetails.getObservaciones());

        return activoRepository.save(activo);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Activo activo = activoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Activo no encontrado con id: " + id));
        activoRepository.delete(activo);
    }
}
