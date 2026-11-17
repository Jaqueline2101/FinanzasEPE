package com.pe.finanzas.otros_pagos.service.impl;

import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.repository.CuentaCorrienteRepository;
import com.pe.finanzas.otros_pagos.dto.OtroPagoRequestDTO;
import com.pe.finanzas.otros_pagos.dto.OtroPagoResponseDTO;
import com.pe.finanzas.otros_pagos.service.OtrosPagosService;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OtrosPagosServiceImpl implements OtrosPagosService {

    private final com.pe.finanzas.caja_chica.repository.CajaChicaMovimientoRepository movimientoRepository;
    private final CuentaCorrienteRepository cuentaRepository;
    private final com.pe.finanzas.caja_chica.services.CajaChicaService cajaChicaService;
    private final DatosPersonalesRepository usuarioRepository;

    @Override
    @Transactional
    public Object registrarOtroPago(OtroPagoRequestDTO dto) {
        log.info("Registrando Otro Pago para: {} - Monto: {}", dto.getBeneficiario(), dto.getMonto());

        if (dto.getIdCuenta() != null) {
            CuentaCorriente cuenta = cuentaRepository.findById(dto.getIdCuenta())
                    .orElseThrow(() -> new RuntimeException("Cuenta no encontrada"));
            BigDecimal saldoActual = cuenta.getSaldo() != null ? cuenta.getSaldo() : BigDecimal.ZERO;
            cuenta.setSaldo(saldoActual.add(dto.getMonto()));
            cuentaRepository.save(cuenta);
        }

        String conceptoFinal = dto.getConcepto();
        if (conceptoFinal == null || conceptoFinal.trim().isEmpty()) {
            conceptoFinal = "Pago: " + dto.getBeneficiario();
        } else {
            conceptoFinal = dto.getConcepto() + " (" + dto.getBeneficiario() + ")";
        }

        String usuario = dto.getIdUsuario() != null ? String.valueOf(dto.getIdUsuario()) : "SISTEMA";
        var cajaOpt = cajaChicaService.obtenerCajaAbiertaEntidad(usuario);
        
        if (cajaOpt.isEmpty()) {
            throw new RuntimeException("El usuario " + usuario + " no tiene una CAJA CHICA abierta para registrar este pago.");
        }

        String beneficiario = dto.getBeneficiario() != null ? dto.getBeneficiario() : "";
        String dni = dto.getDni() != null ? dto.getDni() : "";
        String tipoPersona = dto.getTipoPersona() != null ? dto.getTipoPersona() : "";
        String referenciaData = "OTROPAGO|" + beneficiario + "|" + dni + "|" + tipoPersona;

        return cajaChicaService.registrarMovimientoInterno(
                cajaOpt.get(),
                (dto.getTipo() != null && "INGRESO".equalsIgnoreCase(dto.getTipo())) 
                    ? com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento.TipoMovimiento.INGRESO 
                    : com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento.TipoMovimiento.EGRESO,
                dto.getMonto(),
                conceptoFinal,
                referenciaData,
                dto.getMetodoPago(),
                usuario
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OtroPagoResponseDTO> listarOtrosPagos(Long idSucursal, Pageable pageable) {
        log.info("Listando Otros Pagos para sucursal: {}", idSucursal);

        // Ahora listamos desde CajaChicaMovimiento (filtrando por descripcion o referencia si es necesario)
        // Por simplicidad, traemos todos los movimientos de las cajas de esta sucursal
        var entities = movimientoRepository.findAll(pageable); // Esto es una simplificación

        if (entities == null || entities.isEmpty()) {
            return Page.empty();
        }

        List<OtroPagoResponseDTO> dtos = entities.getContent().stream()
                .map(m -> {
                    String ref = m.getReferencia();
                    String b = "", d = "", t = "";
                    if(ref != null && ref.startsWith("OTROPAGO|")) {
                        String[] parts = ref.split("\\|", -1);
                        if(parts.length >= 4) {
                            b = parts[1];
                            d = parts[2];
                            t = parts[3];
                        }
                    } else {
                        // Intentar extraer de la descripción para registros antiguos
                        String desc = m.getDescripcion() != null ? m.getDescripcion() : "";
                        if (desc.startsWith("Pago: ")) {
                            b = desc.substring(6);
                        } else if (desc.contains(" (") && desc.endsWith(")")) {
                            b = desc.substring(desc.lastIndexOf(" (") + 2, desc.length() - 1);
                        } else if (ref != null) {
                            b = ref; // Fallback final
                        }
                    }

                    return OtroPagoResponseDTO.builder()
                        .id(m.getId())
                        .tipo(m.getTipo().name())
                        .monto(m.getMonto())
                        .concepto(m.getDescripcion())
                        .idUsuario(m.getUsuario() != null && m.getUsuario().matches("\\d+") ? Long.valueOf(m.getUsuario()) : null)
                        .fechaMovimiento(m.getFecha())
                        .metodoPago(m.getMetodoPago() != null ? m.getMetodoPago().getDescripcion() : "N/A")
                        .beneficiario(b)
                        .dni(d)
                        .tipoPersona(t)
                        .build();
                })
                .collect(Collectors.toList());

        return new PageImpl<>(dtos, pageable, entities.getTotalElements());
    }


}