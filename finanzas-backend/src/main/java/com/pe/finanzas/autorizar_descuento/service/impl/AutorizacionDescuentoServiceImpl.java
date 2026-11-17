package com.pe.finanzas.autorizar_descuento.service.impl;

import com.pe.finanzas.autorizar_descuento.dto.AutorizacionRequestDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResolucionDto;
import com.pe.finanzas.autorizar_descuento.dto.AutorizacionResponseDto;
import com.pe.finanzas.autorizar_descuento.entity.AutorizacionDescuento;
import com.pe.finanzas.autorizar_descuento.repository.AutorizacionDescuentoRepository;
import com.pe.finanzas.autorizar_descuento.service.AutorizacionDescuentoService;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AutorizacionDescuentoServiceImpl implements AutorizacionDescuentoService {

    private final AutorizacionDescuentoRepository repository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final com.pe.finanzas.service.KafkaProducerService kafkaProducerService;

    @Override
    @Transactional
    public AutorizacionResponseDto solicitarDescuento(AutorizacionRequestDto dto) {
        log.info("Creando solicitud de descuento por cajero {}", dto.getIdCajero());

        DatosPersonales cajero = datosPersonalesRepository.findById(dto.getIdCajero())
                .orElseThrow(() -> new RuntimeException("Cajero no encontrado en la base de datos"));

        AutorizacionDescuento entity = AutorizacionDescuento.builder()
                .idVenta(dto.getIdVenta())
                .montoOriginal(dto.getMontoOriginal())
                .montoDescuento(dto.getMontoDescuento())
                .montoFinal(dto.getMontoFinal())
                .motivoSolicitud(dto.getMotivoSolicitud())
                .estado("PENDIENTE")
                .pacienteNombre(dto.getPacienteNombre())
                .pacienteDni(dto.getPacienteDni())
                .doctorNombre(dto.getDoctorNombre())
                .cajero(cajero)
                .build();

        AutorizacionDescuento saved = repository.save(entity);

        // Notificar via Kafka
        try {
            AutorizacionResponseDto responseDto = mapToDto(saved);
            kafkaProducerService.enviarNotificacion(
                null, // A todos los autorizadores
                "AUTORIZACION_DESCUENTO",
                "Nueva Solicitud de Descuento",
                responseDto,
                "warning"
            );
        } catch (Exception e) {
            log.error("Error enviando notificación de descuento a Kafka", e);
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AutorizacionResponseDto aprobarDescuento(Long idAutorizacion, AutorizacionResolucionDto dto) {
        log.info("Aprobando descuento {} por el autorizador {}", idAutorizacion, dto.getIdAutorizador());

        AutorizacionDescuento entity = repository.findById(idAutorizacion)
                .orElseThrow(() -> new RuntimeException("Autorización no encontrada"));

        if (!"PENDIENTE".equals(entity.getEstado())) {
            throw new RuntimeException("Solo se pueden aprobar solicitudes en estado PENDIENTE");
        }

        DatosPersonales autorizador = datosPersonalesRepository.findById(dto.getIdAutorizador())
                .orElseThrow(() -> new RuntimeException("Autorizador no encontrado"));

        entity.setEstado("APROBADO");
        entity.setAutorizador(autorizador);
        entity.setFechaResolucion(LocalDateTime.now());

        AutorizacionDescuento saved = repository.save(entity);

        // Notificar via Kafka al cajero
        try {
            if (saved.getCajero() != null && saved.getCajero().getId() != null) {
                kafkaProducerService.enviarNotificacion(
                    saved.getCajero().getId().toString(),
                    "AUTORIZACION_DESCUENTO",
                    "Descuento Aprobado",
                    "La solicitud de descuento para " + saved.getPacienteNombre() + " ha sido aprobada.",
                    "success"
                );
            }
        } catch (Exception e) {
            log.error("Error enviando notificación de aprobación a Kafka", e);
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional
    public AutorizacionResponseDto rechazarDescuento(Long idAutorizacion, AutorizacionResolucionDto dto) {
        log.info("Rechazando descuento {} por el autorizador {}", idAutorizacion, dto.getIdAutorizador());

        AutorizacionDescuento entity = repository.findById(idAutorizacion)
                .orElseThrow(() -> new RuntimeException("Autorización no encontrada"));

        if (!"PENDIENTE".equals(entity.getEstado())) {
            throw new RuntimeException("Solo se pueden rechazar solicitudes en estado PENDIENTE");
        }

        DatosPersonales autorizador = datosPersonalesRepository.findById(dto.getIdAutorizador())
                .orElseThrow(() -> new RuntimeException("Autorizador no encontrado"));

        if (dto.getMotivoRechazo() == null || dto.getMotivoRechazo().trim().isEmpty()) {
            throw new RuntimeException("Debe proveer un motivo de rechazo");
        }

        entity.setEstado("RECHAZADO");
        entity.setAutorizador(autorizador);
        entity.setMotivoRechazo(dto.getMotivoRechazo());
        entity.setFechaResolucion(LocalDateTime.now());

        AutorizacionDescuento saved = repository.save(entity);

        // Notificar via Kafka al cajero
        try {
            if (saved.getCajero() != null && saved.getCajero().getId() != null) {
                kafkaProducerService.enviarNotificacion(
                    saved.getCajero().getId().toString(),
                    "AUTORIZACION_DESCUENTO",
                    "Descuento Rechazado",
                    "La solicitud de descuento para " + saved.getPacienteNombre() + " ha sido rechazada.",
                    "error"
                );
            }
        } catch (Exception e) {
            log.error("Error enviando notificación de rechazo a Kafka", e);
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutorizacionResponseDto> listarPendientes() {
        return repository.findByEstadoOrderByFechaSolicitudDesc("PENDIENTE").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutorizacionResponseDto> listarResueltos() {
        return repository.findByEstadoNotOrderByFechaResolucionDesc("PENDIENTE").stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<AutorizacionResponseDto> listarPorCajero(Long idCajero) {
        return repository.findByCajeroIdOrderByFechaSolicitudDesc(idCajero).stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    private AutorizacionResponseDto mapToDto(AutorizacionDescuento entity) {
        String nombreCajero = "";
        String dniCajero = "";
        if (entity.getCajero() != null) {
            nombreCajero = (entity.getCajero().getNombre() != null ? entity.getCajero().getNombre() + " " : "") +
                    (entity.getCajero().getApepat() != null ? entity.getCajero().getApepat() : "");
            dniCajero = entity.getCajero().getNumdoc();
        }

        String nombreAutorizador = "";
        if (entity.getAutorizador() != null) {
            nombreAutorizador = (entity.getAutorizador().getNombre() != null ? entity.getAutorizador().getNombre() + " "
                    : "") +
                    (entity.getAutorizador().getApepat() != null ? entity.getAutorizador().getApepat() : "");
        }

        return AutorizacionResponseDto.builder()
                .idAutorizacion(entity.getIdAutorizacion())
                .idVenta(entity.getIdVenta())
                .montoOriginal(entity.getMontoOriginal())
                .montoDescuento(entity.getMontoDescuento())
                .montoFinal(entity.getMontoFinal())
                .motivoSolicitud(entity.getMotivoSolicitud())
                .estado(entity.getEstado())
                .pacienteNombre(entity.getPacienteNombre())
                .pacienteDni(entity.getPacienteDni())
                .doctorNombre(entity.getDoctorNombre())
                .cajeroNombre(nombreCajero.trim())

                .cajeroDni(dniCajero)
                .autorizadorNombre(nombreAutorizador.trim())
                .motivoRechazo(entity.getMotivoRechazo())
                .fechaSolicitud(entity.getFechaSolicitud())
                .fechaResolucion(entity.getFechaResolucion())

                .build();
    }
}
