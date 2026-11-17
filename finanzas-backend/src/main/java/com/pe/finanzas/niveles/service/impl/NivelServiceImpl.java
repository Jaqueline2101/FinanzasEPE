package com.pe.finanzas.niveles.service.impl;

import com.pe.finanzas.niveles.dto.*;
import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.mapper.NivelMapper;
import com.pe.finanzas.niveles.repository.NivelRepository;
import com.pe.finanzas.niveles.service.NivelService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class NivelServiceImpl implements NivelService {

    private final NivelRepository nivelRepository;
    private final NivelMapper nivelMapper;
    private final CodigoNivelGenerator codigoNivelGenerator;
    private final com.pe.finanzas.niveles.repository.TipoNivelRepository tipoNivelRepository;

    @Override
    @Transactional(readOnly = true)
    public List<com.pe.finanzas.niveles.entity.TipoNivel> listarTiposActivos() {
        List<com.pe.finanzas.niveles.entity.TipoNivel> tipos = tipoNivelRepository.findAllActivos();

        if (tipos.isEmpty()) {
            return java.util.Arrays.asList(
                    com.pe.finanzas.niveles.entity.TipoNivel.builder().id(1L).codigo("PADRE").nombre("Padre").orden(1)
                            .estado(1).build(),
                    com.pe.finanzas.niveles.entity.TipoNivel.builder().id(2L).codigo("HIJO").nombre("Hijo").orden(2)
                            .estado(1).build(),
                    com.pe.finanzas.niveles.entity.TipoNivel.builder().id(3L).codigo("NIETO").nombre("Nieto").orden(3)
                            .estado(1).build());
        }

        return tipos;
    }

    @Override
    public NivelDto crear(NivelCreateDto dto) {
        log.info("Creando nuevo nivel: {}", dto.getNombre());

        if (nivelRepository.existsByNombreIgnoreCaseAndNivelPadreIdNivel(
                dto.getNombre(), dto.getIdNivelPadre())) {
            throw new IllegalArgumentException(
                    "Ya existe un nivel con ese nombre en el mismo nivel padre");
        }

        String codigoGenerado = codigoNivelGenerator.generarCodigo(
                dto.getIdNivelPadre(),
                dto.getNumNivel());

        Nivel nivel = nivelMapper.toEntity(dto);

        nivel.setNumNivel(codigoGenerado);

        Nivel guardado = nivelRepository.save(nivel);
        log.info("Nivel creado con ID: {} y código: {}", guardado.getIdNivel(), guardado.getNumNivel());

        return nivelMapper.toDto(guardado);
    }

    @Override
    public NivelDto actualizar(Long id, NivelDto dto) {
        log.info("Actualizando nivel con ID: {}", id);

        Nivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

        String nombreAnterior = nivel.getNombre();
        String codigoAnterior = nivel.getNumNivel();

        if (dto.getNumNivel() != null && !dto.getNumNivel().equals(codigoAnterior)) {
            log.warn("Intento de cambiar código de nivel. Operación no permitida.");
            throw new IllegalArgumentException(
                    "No se puede modificar el código de un nivel existente. " +
                            "El código forma parte de la estructura jerárquica.");
        }

        nivelMapper.updateEntity(dto, nivel);

        Nivel actualizado = nivelRepository.save(nivel);
        log.info("Nivel actualizado: {}", id);

        return nivelMapper.toDto(actualizado);
    }

    @Override
    public void eliminar(Long id) {
        log.info("Eliminando nivel con ID: {}", id);

        Nivel nivel = nivelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

        Long hijos = nivelRepository.countByNivelPadreIdNivel(id);
        if (hijos > 0) {
            throw new IllegalArgumentException(
                    "No se puede eliminar el nivel porque tiene " + hijos + " subniveles");
        }

        String nombreEliminado = nivel.getNombre();
        String codigoEliminado = nivel.getNumNivel();
        nivelRepository.deleteById(id);
        log.info("Nivel eliminado: {} (código: {})", id, codigoEliminado);

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NivelDto> obtenerPorId(Long id) {
        return nivelRepository.findById(id)
                .map(nivelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<NivelDto> obtenerPorCodigo(String codigo) {
        return nivelRepository.findByNumNivel(codigo)
                .map(nivelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelDto> obtenerTodos() {
        return nivelRepository.findAll().stream()
                .map(nivelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NivelDto> obtenerTodosPaginado(Pageable pageable) {
        return nivelRepository.findAll(pageable)
                .map(nivelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelDto> obtenerNivelesRaiz() {
        return nivelRepository.findByNivelPadreIsNullOrderByOrden().stream()
                .map(nivelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NivelDto> obtenerNivelesRaizPaginado(Pageable pageable) {
        return nivelRepository.findByNivelPadreIsNull(pageable)
                .map(nivelMapper::toDto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelDto> obtenerHijos(Long idNivelPadre) {
        return nivelRepository.findByNivelPadreIdNivelOrderByOrden(idNivelPadre).stream()
                .map(nivelMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelTreeDto> obtenerArbolCompleto() {
        List<Nivel> raices = nivelRepository.findByNivelPadreIsNullOrderByOrden();
        return raices.stream()
                .map(this::construirArbol)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<NivelTreeDto> obtenerArbolDesde(Long idNivel) {
        Nivel nivel = nivelRepository.findById(idNivel)
                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

        List<NivelTreeDto> arbol = new ArrayList<>();
        arbol.add(construirArbol(nivel));
        return arbol;
    }

    @Override
    public void moverNivel(NivelMoverDto dto) {
        log.info("Moviendo nivel {} a nuevo padre {}", dto.getIdNivel(), dto.getIdNivelPadreNuevo());

        Nivel nivel = nivelRepository.findById(dto.getIdNivel())
                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

        log.warn("ADVERTENCIA: Mover niveles requiere regenerar códigos de toda la rama");
        throw new IllegalArgumentException(
                "No se puede mover niveles una vez creados porque esto requeriría " +
                        "regenerar los códigos de toda la rama jerárquica. " +
                        "Si necesita reorganizar, considere eliminar y recrear la estructura.");
    }

    @Override
    public void cambiarOrden(Long idNivel, Integer nuevoOrden) {
        Nivel nivel = nivelRepository.findById(idNivel)
                .orElseThrow(() -> new IllegalArgumentException("Nivel no encontrado"));

        nivel.setOrden(nuevoOrden);
        nivelRepository.save(nivel);

        log.info("Orden actualizado para nivel {} (código: {}): {}",
                idNivel, nivel.getNumNivel(), nuevoOrden);
    }

    @Override
    @Transactional(readOnly = true)
    public EstadisticasNivelDto obtenerEstadisticas() {
        EstadisticasNivelDto stats = new EstadisticasNivelDto();

        stats.setTotalNiveles(nivelRepository.count());
        stats.setNivelesActivos(nivelRepository.countByEstado(1));
        stats.setNivelesInactivos(nivelRepository.countByEstado(0));
        stats.setNivelMaximoJerarquia(nivelRepository.obtenerNivelMaximoJerarquia());
        stats.setTotalNivelesRaiz((long) nivelRepository.findByNivelPadreIsNullOrderByOrden().size());
        stats.setTotalNivelesConHijos((long) nivelRepository.findNivelesConHijos().size());
        stats.setTotalNivelesSinHijos(stats.getTotalNiveles() - stats.getTotalNivelesConHijos());

        return stats;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<NivelDto> buscarConFiltros(String nombre, String tipo, Integer estado,
            Long idNivelPadre, Pageable pageable) {
        return nivelRepository.buscarConFiltros(nombre, tipo, estado, idNivelPadre, pageable)
                .map(nivelMapper::toDto);
    }

    private NivelTreeDto construirArbol(Nivel nivel) {
        NivelTreeDto dto = nivelMapper.toTreeDto(nivel);

        List<Nivel> hijos = nivelRepository.findByNivelPadreIdNivelOrderByOrden(nivel.getIdNivel());
        if (!hijos.isEmpty()) {
            dto.setHijos(hijos.stream()
                    .map(this::construirArbol)
                    .collect(Collectors.toList()));
        }

        return dto;
    }

    private void actualizarJerarquia(Nivel nivel) {
        if (nivel.getIdNivelPadre() != null) {
            Nivel padre = nivelRepository.findById(nivel.getIdNivelPadre()).orElse(null);
            if (padre != null) {
                nivel.setNivelJerarquia(padre.getNivelJerarquia() + 1);
            }
        } else {
            nivel.setNivelJerarquia(1);
        }
    }

    private void validarMovimiento(Long idNivel, Long idNivelPadreNuevo) {
        List<Nivel> jerarquia = nivelRepository.obtenerJerarquiaCompleta(idNivel);
        boolean esDescendiente = jerarquia.stream()
                .anyMatch(n -> n.getIdNivel().equals(idNivelPadreNuevo));

        if (esDescendiente) {
            throw new IllegalArgumentException(
                    "No se puede mover un nivel a uno de sus descendientes");
        }
    }
}
