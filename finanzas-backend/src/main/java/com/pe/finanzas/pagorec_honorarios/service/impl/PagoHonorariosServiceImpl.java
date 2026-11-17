package com.pe.finanzas.pagorec_honorarios.service.impl;

import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;
import com.pe.finanzas.pagorec_honorarios.dto.PagoConceptoDetalleDto;
import com.pe.finanzas.pagorec_honorarios.dto.PagoHonorariosRequest;
import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.entity.PagoConceptoDetalle;
import com.pe.finanzas.pagorec_honorarios.repository.PagoComprobanteRepository;
import com.pe.finanzas.pagorec_honorarios.service.PagoHonorariosService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class PagoHonorariosServiceImpl implements PagoHonorariosService {

    private final PagoComprobanteRepository pagoRepository;

    @Override
    @Transactional
    public PagoComprobanteDto registrarPago(PagoHonorariosRequest request) {
        // Server-side calculation to ensure data integrity
        BigDecimal montoBruto = request.getMontoBruto() != null ? request.getMontoBruto() : BigDecimal.ZERO;
        BigDecimal retencion = BigDecimal.ZERO;

        if ("HONORARIO".equalsIgnoreCase(request.getTipoPago())) {
            if (montoBruto.compareTo(new BigDecimal("1500.0")) > 0) {
                retencion = montoBruto.multiply(new BigDecimal("0.08"));
            }
        } else {
            // Se mantiene el valor enviado si no es honorario
            retencion = request.getRetencion() != null ? request.getRetencion() : BigDecimal.ZERO;
        }

        // Recalcular montos relacionados si se aplicó cálculo seguro
        BigDecimal descuento = request.getDescuento() != null ? request.getDescuento() : BigDecimal.ZERO;
        BigDecimal aporteAfpOnp = request.getAporteAfpOnp() != null ? request.getAporteAfpOnp() : BigDecimal.ZERO;
        BigDecimal otrosDescuentos = request.getOtrosDescuentos() != null ? request.getOtrosDescuentos()
                : BigDecimal.ZERO;

        BigDecimal totalRetenciones = retencion.add(aporteAfpOnp).add(otrosDescuentos);
        BigDecimal montoNeto = montoBruto.subtract(descuento).subtract(totalRetenciones);

        PagoComprobante entity = PagoComprobante.builder()
                .tipoPago(request.getTipoPago())
                .numeroRecibo(request.getNumeroRecibo())
                .serieComprobante(request.getSerieComprobante())
                .fechaEmision(request.getFechaEmision())
                .fechaPago(request.getFechaPago())
                .periodoMes(request.getPeriodoMes())
                .periodoAnio(request.getPeriodoAnio())
                .conceptoPago(request.getConceptoPago())
                .descripcionAdicional(request.getDescripcionAdicional())
                .estadoPago(request.getEstadoPago() != null ? request.getEstadoPago() : "PENDIENTE")
                .tipoBeneficiario(request.getTipoBeneficiario())
                .numeroDocumento(request.getNumeroDocumento())
                .nombreRazonSocial(request.getNombreRazonSocial())
                .areaServicio(request.getAreaServicio())
                .centroCosto(request.getCentroCosto())
                .montoBruto(montoBruto)
                .descuento(descuento)
                .retencion(retencion)
                .aporteAfpOnp(aporteAfpOnp)
                .otrosDescuentos(otrosDescuentos)
                .totalRetenciones(totalRetenciones)
                .montoNeto(montoNeto)
                .moneda(request.getMoneda())
                .tipoCambio(request.getTipoCambio())
                .medioPago(request.getMedioPago())
                .bancoDestino(request.getBancoDestino())
                .numeroCuenta(request.getNumeroCuenta())
                .numeroOperacion(request.getNumeroOperacion())
                .codigoTransferencia(request.getCodigoTransferencia())
                .fechaTransferencia(request.getFechaTransferencia())
                .observacionPago(request.getObservacionPago())
                .cuentaContable(request.getCuentaContable())
                .subcuenta(request.getSubcuenta())
                .tipoGasto(request.getTipoGasto())
                .numeroAsiento(request.getNumeroAsiento())
                .glosaContable(request.getGlosaContable())
                .periodoContable(request.getPeriodoContable())
                .fechaRegistro(LocalDateTime.now())
                .estadoValidacion(request.getEstadoValidacion() != null ? request.getEstadoValidacion() : "PENDIENTE")
                .build();

        if (request.getDetalles() != null && !request.getDetalles().isEmpty()) {
            for (PagoConceptoDetalleDto detDto : request.getDetalles()) {
                PagoConceptoDetalle detEntity = PagoConceptoDetalle.builder()
                        .fechaIngreso(detDto.getFechaIngreso())
                        .fechaCese(detDto.getFechaCese())
                        .tiempoTrabajado(detDto.getTiempoTrabajado())
                        .vacacionesPendientes(detDto.getVacacionesPendientes())
                        .cts(detDto.getCts())
                        .gratificacion(detDto.getGratificacion())
                        .indemnizacion(detDto.getIndemnizacion())
                        .totalLiquidacion(detDto.getTotalLiquidacion())
                        .build();
                entity.addDetalle(detEntity);
            }
        }

        PagoComprobante saved = pagoRepository.save(entity);
        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoComprobanteDto> listarPagos() {
        return pagoRepository.findAll().stream()
                .filter(p -> "HONORARIO".equalsIgnoreCase(p.getTipoPago()))
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PagoComprobanteDto obtenerPorId(Long id) {
        return pagoRepository.findById(id)
                .map(this::mapToDto)
                .orElse(null);
    }

    private PagoComprobanteDto mapToDto(PagoComprobante entity) {
        return PagoComprobanteDto.builder()
                .idPago(entity.getIdPago())
                .tipoPago(entity.getTipoPago())
                .numeroRecibo(entity.getNumeroRecibo())
                .serieComprobante(entity.getSerieComprobante())
                .fechaEmision(entity.getFechaEmision())
                .fechaPago(entity.getFechaPago())
                .periodoMes(entity.getPeriodoMes())
                .periodoAnio(entity.getPeriodoAnio())
                .conceptoPago(entity.getConceptoPago())
                .descripcionAdicional(entity.getDescripcionAdicional())
                .estadoPago(entity.getEstadoPago())
                .tipoBeneficiario(entity.getTipoBeneficiario())
                .numeroDocumento(entity.getNumeroDocumento())
                .nombreRazonSocial(entity.getNombreRazonSocial())
                .areaServicio(entity.getAreaServicio())
                .centroCosto(entity.getCentroCosto())
                .montoBruto(entity.getMontoBruto())
                .descuento(entity.getDescuento())
                .retencion(entity.getRetencion())
                .aporteAfpOnp(entity.getAporteAfpOnp())
                .otrosDescuentos(entity.getOtrosDescuentos())
                .totalRetenciones(entity.getTotalRetenciones())
                .montoNeto(entity.getMontoNeto())
                .moneda(entity.getMoneda())
                .tipoCambio(entity.getTipoCambio())
                .medioPago(entity.getMedioPago())
                .bancoDestino(entity.getBancoDestino())
                .numeroCuenta(entity.getNumeroCuenta())
                .numeroOperacion(entity.getNumeroOperacion())
                .codigoTransferencia(entity.getCodigoTransferencia())
                .fechaTransferencia(entity.getFechaTransferencia())
                .observacionPago(entity.getObservacionPago())
                .cuentaContable(entity.getCuentaContable())
                .subcuenta(entity.getSubcuenta())
                .tipoGasto(entity.getTipoGasto())
                .numeroAsiento(entity.getNumeroAsiento())
                .glosaContable(entity.getGlosaContable())
                .periodoContable(entity.getPeriodoContable())
                .usuarioRegistro(entity.getUsuarioRegistro())
                .usuarioAprobacion(entity.getUsuarioAprobacion())
                .fechaRegistro(entity.getFechaRegistro())
                .fechaAprobacion(entity.getFechaAprobacion())
                .estadoValidacion(entity.getEstadoValidacion())
                .motivoAnulacion(entity.getMotivoAnulacion())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .detalles(entity.getDetalles().stream().map(det -> PagoConceptoDetalleDto.builder()
                        .idDetalle(det.getIdDetalle())
                        .fechaIngreso(det.getFechaIngreso())
                        .fechaCese(det.getFechaCese())
                        .tiempoTrabajado(det.getTiempoTrabajado())
                        .vacacionesPendientes(det.getVacacionesPendientes())
                        .cts(det.getCts())
                        .gratificacion(det.getGratificacion())
                        .indemnizacion(det.getIndemnizacion())
                        .totalLiquidacion(det.getTotalLiquidacion())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
