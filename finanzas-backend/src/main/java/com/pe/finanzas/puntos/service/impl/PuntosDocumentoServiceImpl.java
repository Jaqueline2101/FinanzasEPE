package com.pe.finanzas.puntos.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.puntos.dto.PuntosDocumentoDto;
import com.pe.finanzas.puntos.dto.SerieCorrelativoDto;
import com.pe.finanzas.puntos.entity.Punto;
import com.pe.finanzas.puntos.entity.PuntosDocumento;
import com.pe.finanzas.puntos.entity.TipoDocumento;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.puntos.repository.PuntosDocumentoRepository;
import com.pe.finanzas.puntos.repository.TipoDocumentoRepository;
import com.pe.finanzas.puntos.service.PuntosDocumentoService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PuntosDocumentoServiceImpl implements PuntosDocumentoService {

        private final PuntosDocumentoRepository puntosDocumentoRepository;
        private final PuntoRepository puntoRepository;
        private final TipoDocumentoRepository tipoDocumentoRepository;

        @Override
        public PuntosDocumentoDto asignarDocumentoAPunto(Long idPunto, PuntosDocumentoDto dto) {
                log.info("Asignando documento tipo {} al punto {}", dto.getIdTipoDocumento(), idPunto);

                // Validar que no exista ya este tipo de documento en el punto
                if (puntosDocumentoRepository.existsByPuntoPuntoAndTipoDocumentoIdTipoDocumento(
                                idPunto, dto.getIdTipoDocumento())) {
                        throw new IllegalArgumentException(
                                        "Este tipo de documento ya está asignado al punto");
                }

                // Validar que la serie no exista
                if (puntosDocumentoRepository.existsBySerie(dto.getSerie())) {
                        throw new IllegalArgumentException(
                                        "La serie '" + dto.getSerie() + "' ya está en uso");
                }

                // Obtener punto
                Punto punto = puntoRepository.findById(idPunto)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Punto no encontrado con ID: " + idPunto));

                // Obtener tipo de documento
                TipoDocumento tipoDocumento = tipoDocumentoRepository.findById(dto.getIdTipoDocumento())
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de documento no encontrado con ID: " + dto.getIdTipoDocumento()));

                PuntosDocumento puntosDocumento = PuntosDocumento.builder()
                                .punto(punto)
                                .tipoDocumento(tipoDocumento)
                                .serie(dto.getSerie())
                                .numeroActual(dto.getNumeroActual() != null ? dto.getNumeroActual() : 1)
                                .activo(dto.getActivo() == null || dto.getActivo())
                                .build();

                puntosDocumento = puntosDocumentoRepository.save(puntosDocumento);
                log.info("Documento asignado con ID: {}", puntosDocumento.getId());

                return convertirADto(puntosDocumento);
        }

        @Override
        public PuntosDocumentoDto actualizar(Long id, PuntosDocumentoDto dto) {
                log.info("Actualizando documento ID: {}", id);

                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + id));

                // Validar serie única si cambió
                if (!puntosDocumento.getSerie().equals(dto.getSerie())) {
                        if (puntosDocumentoRepository.existsBySerie(dto.getSerie())) {
                                throw new IllegalArgumentException(
                                                "La serie '" + dto.getSerie() + "' ya está en uso");
                        }
                }

                puntosDocumento.setSerie(dto.getSerie());
                puntosDocumento.setNumeroActual(dto.getNumeroActual());

                puntosDocumento = puntosDocumentoRepository.save(puntosDocumento);
                log.info("Documento actualizado: {}", id);

                return convertirADto(puntosDocumento);
        }

        @Override
        @Transactional(readOnly = true)
        public PuntosDocumentoDto obtenerPorId(Long id) {
                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + id));
                return convertirADto(puntosDocumento);
        }

        @Override
        public void eliminar(Long id) {
                log.info("Eliminando documento ID: {}", id);

                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + id));

                puntosDocumento.setActivo(false);
                puntosDocumentoRepository.save(puntosDocumento);
                log.info("Documento desactivado: {}", id);
        }

        @Override
        @Transactional(readOnly = true)
        public List<PuntosDocumentoDto> listarPorPunto(Long idPunto) {
                return puntosDocumentoRepository.findByPuntoPunto(idPunto).stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public List<PuntosDocumentoDto> listarActivosPorPunto(Long idPunto) {
                return puntosDocumentoRepository.findByPuntoPuntoAndActivoTrue(idPunto).stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional(readOnly = true)
        public SerieCorrelativoDto obtenerSiguienteNumero(Long idPuntoDocumento) {
                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(idPuntoDocumento)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + idPuntoDocumento));

                return SerieCorrelativoDto.builder()
                                .idPuntoDocumento(puntosDocumento.getId())
                                .serie(puntosDocumento.getSerie())
                                .numeroActual(puntosDocumento.getNumeroActual())
                                .siguienteNumero(puntosDocumento.obtenerSiguienteNumero())
                                .tipoDocumento(puntosDocumento.getTipoDocumento().getNombre())
                                .build();
        }

        @Override
        @Transactional(readOnly = true)
        public SerieCorrelativoDto obtenerSiguienteNumeroPorPuntoYTipo(Long idPunto, Long idTipoDocumento) {
                PuntosDocumento puntosDocumento = puntosDocumentoRepository
                                .findActiveByPuntoAndTipo(idPunto, idTipoDocumento)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "No se encontró documento activo para el punto y tipo especificados"));

                return SerieCorrelativoDto.builder()
                                .idPuntoDocumento(puntosDocumento.getId())
                                .serie(puntosDocumento.getSerie())
                                .numeroActual(puntosDocumento.getNumeroActual())
                                .siguienteNumero(puntosDocumento.obtenerSiguienteNumero())
                                .tipoDocumento(puntosDocumento.getTipoDocumento().getNombre())
                                .build();
        }

        @Override
        public synchronized void incrementarCorrelativo(Long idPuntoDocumento) {
                log.info("Incrementando correlativo del documento ID: {}", idPuntoDocumento);

                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(idPuntoDocumento)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + idPuntoDocumento));

                puntosDocumento.incrementarNumero();
                puntosDocumentoRepository.save(puntosDocumento);
                log.info("Correlativo incrementado a: {}", puntosDocumento.getNumeroActual());
        }

        @Override
        public void cambiarEstado(Long id, Boolean activo) {
                log.info("Cambiando estado del documento ID: {} a {}", id, activo);

                PuntosDocumento puntosDocumento = puntosDocumentoRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Documento no encontrado con ID: " + id));

                puntosDocumento.setActivo(activo);
                puntosDocumentoRepository.save(puntosDocumento);
                log.info("Estado del documento actualizado: {}", id);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean existeDocumentoEnPunto(Long idPunto, Long idTipoDocumento) {
                return puntosDocumentoRepository.existsByPuntoPuntoAndTipoDocumentoIdTipoDocumento(
                                idPunto, idTipoDocumento);
        }

        @Override
        @Transactional(readOnly = true)
        public boolean existeSerie(String serie) {
                return puntosDocumentoRepository.existsBySerie(serie);
        }

        // Métodos de conversión
        private PuntosDocumentoDto convertirADto(PuntosDocumento entity) {
                return PuntosDocumentoDto.builder()
                                .idPuntoDocumento(entity.getId())
                                .idPunto(entity.getPunto().getPunto())
                                .nombrePunto(entity.getPunto().getNombre())
                                .idTipoDocumento(entity.getTipoDocumento().getIdTipoDocumento())
                                .codigoTipoDocumento(entity.getTipoDocumento().getCodigo())
                                .nombreTipoDocumento(entity.getTipoDocumento().getNombre())
                                .serie(entity.getSerie())
                                .numeroActual(entity.getNumeroActual())
                                .siguienteNumero(entity.obtenerSiguienteNumero())
                                .activo(entity.getActivo())
                                .createdAt(entity.getCreatedAt())
                                .updatedAt(entity.getUpdatedAt())
                                .build();
        }
}
