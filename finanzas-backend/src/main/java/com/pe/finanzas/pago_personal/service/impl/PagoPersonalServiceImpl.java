package com.pe.finanzas.pago_personal.service.impl;

import com.pe.finanzas.pago_personal.dto.PagoPersonalConceptoRequest;
import com.pe.finanzas.pago_personal.dto.PagoPersonalRequest;
import com.pe.finanzas.pago_personal.service.PagoPersonalService;
import com.pe.finanzas.pagorec_honorarios.dto.PagoComprobanteDto;
import com.pe.finanzas.pagorec_honorarios.dto.PagoConceptoDetalleDto;
import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.entity.PagoConceptoDetalle;
import com.pe.finanzas.pago_personal.repository.PagoPersonalRepository;
import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.repository.CuentaCorrienteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PagoPersonalServiceImpl implements PagoPersonalService {

    private final PagoPersonalRepository pagoRepository;
    private final CuentaCorrienteRepository cuentaCorrienteRepository;

    @Override
    @Transactional
    public PagoComprobanteDto registrarPago(PagoPersonalRequest request) {
        PagoComprobante entity = PagoComprobante.builder()
                .tipoPago(request.getTipoPago())
                .periodoMes(request.getPeriodoMes())
                .periodoAnio(request.getPeriodoAnio())
                .fechaEmision(request.getFechaPago()) // Sincronizar para búsqueda de reimpresión
                .fechaPago(request.getFechaPago())
                .fechaProceso(request.getFechaProceso())
                .conceptoPago(request.getConceptoPago())
                .estadoPago(request.getEstadoPago() != null ? request.getEstadoPago() : "PAGADO")
                .observacionPago(request.getObservacionPago())
                // Datos Trabajador
                .tipoBeneficiario("TRABAJADOR")
                .numeroDocumento(request.getNumeroDocumento())
                .nombreRazonSocial(request.getNombreRazonSocial())
                .cargoTrabajador(request.getCargoTrabajador())
                .areaTrabajador(request.getAreaTrabajador())
                .centroCosto(request.getCentroCosto())
                .tipoContrato(request.getTipoContrato())
                .estadoLaboral(request.getEstadoLaboral())
                // Datos AFP/ONP
                .sistemaPensionario(request.getSistemaPensionario())
                .afpNombre(request.getAfpNombre())
                .tipoComision(request.getTipoComision())
                .porcentajeComision(request.getPorcentajeComision())
                .porcentajeSeguro(request.getPorcentajeSeguro())
                // Montos
                .montoBruto(request.getMontoBruto())
                .otrosDescuentos(request.getTotalDescuentos())
                .montoNeto(request.getMontoNeto())
                .moneda(request.getMoneda())
                .tipoCambio(request.getTipoCambio())
                // Forma de Pago
                .medioPago(request.getMedioPago())
                .bancoDestino(request.getBancoDestino())
                .numeroCuenta(request.getNumeroCuenta())
                .numeroOperacion(request.getNumeroOperacion())
                .codigoTransferencia(request.getCodigoTransferencia())
                .fechaTransferencia(request.getFechaDeposito())
                // Contable
                .cuentaContable(request.getCuentaContable())
                .subcuenta(request.getSubcuenta())
                .numeroAsiento(request.getNumeroAsiento())
                .glosaContable(request.getGlosaContable())
                .periodoContable(request.getPeriodoContable())
                // Control
                .fechaRegistro(LocalDateTime.now())
                .estadoValidacion(request.getEstadoValidacion() != null ? request.getEstadoValidacion() : "PENDIENTE")
                .build();

        if (request.getDetalles() != null) {
            for (PagoPersonalConceptoRequest detReq : request.getDetalles()) {
                PagoConceptoDetalle detEntity = PagoConceptoDetalle.builder()
                        .nombreConcepto(detReq.getNombreConcepto())
                        .tipoConcepto(detReq.getTipoConcepto())
                        .monto(detReq.getMonto())
                        .build();
                entity.addDetalle(detEntity);
            }
        }

        PagoComprobante saved = pagoRepository.save(entity);

        // Descontar el monto del saldo de la cuenta bancaria
        if (request.getIdCuenta() != null && request.getMontoNeto() != null) {
            CuentaCorriente cuenta = cuentaCorrienteRepository.findById(request.getIdCuenta())
                    .orElseThrow(() -> new RuntimeException("Cuenta bancaria no encontrada con ID: " + request.getIdCuenta()));
            BigDecimal montoDescontar = request.getMontoNeto();
            BigDecimal nuevoSaldo = cuenta.getSaldo().subtract(montoDescontar);
            cuenta.setSaldo(nuevoSaldo);
            cuentaCorrienteRepository.save(cuenta);
            log.info("Saldo actualizado para cuenta {}: {} -> {} (descuento: {})",
                    cuenta.getIdCuenta(), cuenta.getSaldo().add(montoDescontar), nuevoSaldo, montoDescontar);
        }

        return mapToDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoComprobanteDto> listarPagos() {
        // Filtramos para obtener solo pagos de tipo personal/planilla si es necesario,
        // pero por ahora devolvemos todos los que no son 'HONORARIO' o según lógica
        return pagoRepository.findAll().stream()
                .filter(p -> p.getTipoPago() != null && !p.getTipoPago().equals("HONORARIO"))
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

    @Override
    @Transactional
    public List<PagoComprobanteDto> registrarPagosMasivos(List<PagoPersonalRequest> requests) {
        log.info("Iniciando registro masivo de {} pagos", requests.size());
        List<PagoComprobanteDto> resultados = new ArrayList<>();
        
        for (PagoPersonalRequest request : requests) {
            try {
                resultados.add(this.registrarPago(request));
            } catch (Exception e) {
                log.error("Error al registrar pago individual para: {} (Doc: {}). Causa: {}", 
                    request.getNombreRazonSocial(), request.getNumeroDocumento(), e.getMessage());
                throw e; // Relanzar para rollback total de la transacción masiva
            }
        }
        
        log.info("Registro masivo completado exitosamente");
        return resultados;
    }

    private PagoComprobanteDto mapToDto(PagoComprobante entity) {
        return PagoComprobanteDto.builder()
                .idPago(entity.getIdPago())
                .tipoPago(entity.getTipoPago())
                .periodoMes(entity.getPeriodoMes())
                .periodoAnio(entity.getPeriodoAnio())
                .fechaPago(entity.getFechaPago())
                .fechaProceso(entity.getFechaProceso())
                .conceptoPago(entity.getConceptoPago())
                .estadoPago(entity.getEstadoPago())
                .numeroDocumento(entity.getNumeroDocumento())
                .nombreRazonSocial(entity.getNombreRazonSocial())
                .cargoTrabajador(entity.getCargoTrabajador())
                .areaTrabajador(entity.getAreaTrabajador())
                .centroCosto(entity.getCentroCosto())
                .tipoContrato(entity.getTipoContrato())
                .estadoLaboral(entity.getEstadoLaboral())
                .sistemaPensionario(entity.getSistemaPensionario())
                .afpNombre(entity.getAfpNombre())
                .tipoComision(entity.getTipoComision())
                .porcentajeComision(entity.getPorcentajeComision())
                .porcentajeSeguro(entity.getPorcentajeSeguro())
                .montoBruto(entity.getMontoBruto())
                .otrosDescuentos(entity.getOtrosDescuentos())
                .montoNeto(entity.getMontoNeto())
                .moneda(entity.getMoneda())
                .tipoCambio(entity.getTipoCambio())
                .medioPago(entity.getMedioPago())
                .bancoDestino(entity.getBancoDestino())
                .numeroCuenta(entity.getNumeroCuenta())
                .numeroOperacion(entity.getNumeroOperacion())
                .codigoTransferencia(entity.getCodigoTransferencia())
                .fechaTransferencia(entity.getFechaTransferencia())
                .cuentaContable(entity.getCuentaContable())
                .subcuenta(entity.getSubcuenta())
                .numeroAsiento(entity.getNumeroAsiento())
                .glosaContable(entity.getGlosaContable())
                .periodoContable(entity.getPeriodoContable())
                .fechaRegistro(entity.getFechaRegistro())
                .detalles(entity.getDetalles().stream().map(det -> PagoConceptoDetalleDto.builder()
                        .idDetalle(det.getIdDetalle())
                        .nombreConcepto(det.getNombreConcepto())
                        .tipoConcepto(det.getTipoConcepto())
                        .monto(det.getMonto())
                        .build()).collect(Collectors.toList()))
                .build();
    }
}
