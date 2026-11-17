package com.pe.finanzas.puntos.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.puntos.dto.TipoDocumentoDto;
import com.pe.finanzas.puntos.entity.TipoDocumento;
import com.pe.finanzas.puntos.repository.TipoDocumentoRepository;
import com.pe.finanzas.puntos.service.TipoDocumentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TipoDocumentoServiceImpl implements TipoDocumentoService {

    private final TipoDocumentoRepository tipoDocumentoRepository;

    @Override
    public TipoDocumentoDto crear(TipoDocumentoDto dto) {
        log.info("Creando tipo de documento: {}", dto.getCodigo());

        if (tipoDocumentoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un tipo de documento con el código: " + dto.getCodigo());
        }

        TipoDocumento tipoDocumento = TipoDocumento.builder()
                .codigo(dto.getCodigo())
                .nombre(dto.getNombre())
                .descripcion(dto.getDescripcion())
                .requiereRuc(dto.getRequiereRuc() != null ? dto.getRequiereRuc() : false)
                .activo(dto.getActivo() != null ? dto.getActivo() : true)
                .build();

        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        log.info("Tipo de documento creado con ID: {}", tipoDocumento.getIdTipoDocumento());

        return convertirADto(tipoDocumento);
    }

    @Override
    public TipoDocumentoDto actualizar(Long id, TipoDocumentoDto dto) {
        log.info("Actualizando tipo de documento ID: {}", id);

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con ID: " + id));

        // Validar código único si cambió
        if (!tipoDocumento.getCodigo().equals(dto.getCodigo()) &&
                tipoDocumentoRepository.existsByCodigo(dto.getCodigo())) {
            throw new IllegalArgumentException("Ya existe un tipo de documento con el código: " + dto.getCodigo());
        }

        tipoDocumento.setCodigo(dto.getCodigo());
        tipoDocumento.setNombre(dto.getNombre());
        tipoDocumento.setDescripcion(dto.getDescripcion());
        tipoDocumento.setRequiereRuc(dto.getRequiereRuc());

        tipoDocumento = tipoDocumentoRepository.save(tipoDocumento);
        log.info("Tipo de documento actualizado: {}", id);

        return convertirADto(tipoDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoDto obtenerPorId(Long id) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con ID: " + id));
        return convertirADto(tipoDocumento);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando tipo de documento ID: {}", id);

        TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de documento no encontrado con ID: " + id));

        tipoDocumento.setActivo(false);
        tipoDocumentoRepository.save(tipoDocumento);
        log.info("Tipo de documento desactivado: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDto> listarTodos() {
        return tipoDocumentoRepository.findAll().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDto> listarActivos() {
        return tipoDocumentoRepository.findByActivoTrue().stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public TipoDocumentoDto buscarPorCodigo(String codigo) {
        TipoDocumento tipoDocumento = tipoDocumentoRepository.findByCodigo(codigo)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Tipo de documento no encontrado con código: " + codigo));
        return convertirADto(tipoDocumento);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TipoDocumentoDto> buscarPorNombre(String nombre) {
        return tipoDocumentoRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .map(this::convertirADto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public boolean existePorCodigo(String codigo) {
        return tipoDocumentoRepository.existsByCodigo(codigo);
    }

    // Métodos de conversión
    private TipoDocumentoDto convertirADto(TipoDocumento entity) {
        return TipoDocumentoDto.builder()
                .idTipoDocumento(entity.getIdTipoDocumento())
                .codigo(entity.getCodigo())
                .nombre(entity.getNombre())
                .descripcion(entity.getDescripcion())
                .requiereRuc(entity.getRequiereRuc())
                .activo(entity.getActivo())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
