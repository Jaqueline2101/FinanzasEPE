package com.pe.finanzas.rep_descuentos.service;

import com.pe.finanzas.autorizar_descuento.entity.AutorizacionDescuento;
import com.pe.finanzas.autorizar_descuento.repository.AutorizacionDescuentoRepository;
import com.pe.finanzas.rep_descuentos.dto.RepDescuentoRequestDTO;
import com.pe.finanzas.rep_descuentos.dto.RepDescuentoResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
public class RepDescuentoService {

        private final AutorizacionDescuentoRepository repository;

    @Transactional(readOnly = true)
    public org.springframework.data.domain.Page<RepDescuentoResponseDTO> generarReporte(RepDescuentoRequestDTO filter, org.springframework.data.domain.Pageable pageable) {
        LocalDateTime inicio = filter.getFechaInicio() != null 
                ? filter.getFechaInicio().atStartOfDay() 
                : LocalDateTime.now().minusMonths(1).with(LocalTime.MIN);
        
        LocalDateTime fin = filter.getFechaFin() != null 
                ? filter.getFechaFin().atTime(LocalTime.MAX) 
                : LocalDateTime.now().with(LocalTime.MAX);
        
        org.springframework.data.domain.Page<AutorizacionDescuento> entities = repository.buscarParaReporte(
                inicio, fin, filter.getEstado(), filter.getQuery(), pageable);

        return entities.map(this::mapToDto);
    }

        private RepDescuentoResponseDTO mapToDto(AutorizacionDescuento entity) {
                String nombreCajero = entity.getCajero() != null
                                ? entity.getCajero().getNombre() + " " + entity.getCajero().getApepat()
                                : "Sistema";

                String nombreAutorizador = entity.getAutorizador() != null
                                ? entity.getAutorizador().getNombre() + " " + entity.getAutorizador().getApepat()
                                : "N/A";

                return RepDescuentoResponseDTO.builder()
                                .idAutorizacion(entity.getIdAutorizacion())
                                .fechaSolicitud(entity.getFechaSolicitud())
                                .fechaResolucion(entity.getFechaResolucion())
                                .pacienteNombre(entity.getPacienteNombre())
                                .pacienteDni(entity.getPacienteDni())
                                .doctorNombre(entity.getDoctorNombre())
                                .cajeroNombre(nombreCajero.trim())
                                .autorizadorNombre(nombreAutorizador.trim())
                                .montoOriginal(entity.getMontoOriginal())
                                .montoDescuento(entity.getMontoDescuento())
                                .montoFinal(entity.getMontoFinal())
                                .estado(entity.getEstado())
                                .motivoSolicitud(entity.getMotivoSolicitud())
                                .motivoRechazo(entity.getMotivoRechazo())
                                .build();
        }

}
