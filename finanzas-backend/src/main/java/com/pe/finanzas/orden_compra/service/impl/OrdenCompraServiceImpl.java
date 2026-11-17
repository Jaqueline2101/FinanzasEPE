package com.pe.finanzas.orden_compra.service.impl;

import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.orden_compra.dto.OrdenCompraDetalleDto;
import com.pe.finanzas.orden_compra.dto.OrdenCompraRequest;
import com.pe.finanzas.orden_compra.dto.OrdenCompraResponse;
import com.pe.finanzas.orden_compra.entity.OrdenCompra;
import com.pe.finanzas.orden_compra.entity.OrdenCompraDetalle;
import com.pe.finanzas.orden_compra.repository.OrdenCompraRepository;
import com.pe.finanzas.orden_compra.service.OrdenCompraService;
import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrdenCompraServiceImpl implements OrdenCompraService {

    private final OrdenCompraRepository ordenCompraRepository;
    private final ProveedorRepository proveedorRepository;

    @Override
    @Transactional(readOnly = true)
    public List<OrdenCompraResponse> listar() {
        return ordenCompraRepository.findAll().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrdenCompraResponse obtenerPorId(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden de compra no encontrada con ID: " + id));
        return convertToResponse(orden);
    }

    @Override
    @Transactional
    public OrdenCompraResponse registrar(OrdenCompraRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        OrdenCompra orden = OrdenCompra.builder()
                .numeroOrden(request.getNumeroOrden())
                .fechaEmision(request.getFechaEmision())
                .fechaEstimadaEntrega(request.getFechaEstimadaEntrega())
                .tipoCompra(request.getTipoCompra())
                .centroCosto(request.getCentroCosto())
                .areaSolicitante(request.getAreaSolicitante())
                .responsableCompra(request.getResponsableCompra())
                .moneda(request.getMoneda())
                .tipoCambio(request.getTipoCambio())
                .estado("PENDIENTE")
                .observaciones(request.getObservaciones())
                .permiteAdelanto(request.getPermiteAdelanto())
                .tipoAdelanto(request.getTipoAdelanto())
                .porcentajeAdelanto(request.getPorcentajeAdelanto())
                .montoAdelanto(request.getMontoAdelanto())
                .fechaAdelanto(request.getFechaAdelanto())
                .estadoAdelanto(request.getPermiteAdelanto() ? "PENDIENTE" : null)
                .saldoPendienteAdelanto(request.getMontoAdelanto())
                .proveedor(proveedor)
                .subtotal(request.getSubtotal())
                .totalDescuento(request.getTotalDescuento())
                .totalIgv(request.getTotalIgv())
                .totalGeneral(request.getTotalGeneral())
                .totalAdelantado(BigDecimal.ZERO)
                .saldoPorPagar(request.getTotalGeneral())
                .estadoPago("SIN PAGO")
                .lugarEntrega(request.getLugarEntrega())
                .direccionEntrega(request.getDireccionEntrega())
                .fechaEntregaAcordada(request.getFechaEntregaAcordada())
                .responsableRecepcion(request.getResponsableRecepcion())
                .observacionesLogisticas(request.getObservacionesLogisticas())
                .usuarioRegistro(request.getUsuarioRegistro())
                .numCotizacionRelacionada(request.getNumCotizacionRelacionada())
                .numRequerimientoInterno(request.getNumRequerimientoInterno())
                .condicionPago(request.getCondicionPago())
                .plazoDias(request.getPlazoDias())
                .build();

        if (request.getDetalles() != null) {
            List<OrdenCompraDetalle> detalles = request.getDetalles().stream()
                    .map(d -> convertToEntity(d, orden))
                    .collect(Collectors.toList());
            orden.setDetalles(detalles);
        }

        return convertToResponse(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenCompraResponse actualizar(Long id, OrdenCompraRequest request) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));

        if (!"BORRADOR".equals(orden.getEstado())) {
            throw new RuntimeException("Solo se pueden editar órdenes en estado BORRADOR");
        }

        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
                .orElseThrow(() -> new ResourceNotFoundException("Proveedor no encontrado"));

        orden.setFechaEmision(request.getFechaEmision());
        orden.setFechaEstimadaEntrega(request.getFechaEstimadaEntrega());
        orden.setTipoCompra(request.getTipoCompra());
        orden.setCentroCosto(request.getCentroCosto());
        orden.setAreaSolicitante(request.getAreaSolicitante());
        orden.setResponsableCompra(request.getResponsableCompra());
        orden.setMoneda(request.getMoneda());
        orden.setTipoCambio(request.getTipoCambio());
        orden.setObservaciones(request.getObservaciones());
        orden.setPermiteAdelanto(request.getPermiteAdelanto());
        orden.setTipoAdelanto(request.getTipoAdelanto());
        orden.setPorcentajeAdelanto(request.getPorcentajeAdelanto());
        orden.setMontoAdelanto(request.getMontoAdelanto());
        orden.setFechaAdelanto(request.getFechaAdelanto());
        orden.setProveedor(proveedor);
        orden.setSubtotal(request.getSubtotal());
        orden.setTotalDescuento(request.getTotalDescuento());
        orden.setTotalIgv(request.getTotalIgv());
        orden.setTotalGeneral(request.getTotalGeneral());
        orden.setSaldoPorPagar(request.getTotalGeneral()); // Recalcular saldo
        orden.setLugarEntrega(request.getLugarEntrega());
        orden.setDireccionEntrega(request.getDireccionEntrega());
        orden.setFechaEntregaAcordada(request.getFechaEntregaAcordada());
        orden.setResponsableRecepcion(request.getResponsableRecepcion());
        orden.setObservacionesLogisticas(request.getObservacionesLogisticas());
        orden.setNumCotizacionRelacionada(request.getNumCotizacionRelacionada());
        orden.setNumRequerimientoInterno(request.getNumRequerimientoInterno());
        orden.setCondicionPago(request.getCondicionPago());
        orden.setPlazoDias(request.getPlazoDias());

        // Actualizar detalles (reemplazo simple para este ejemplo)
        orden.getDetalles().clear();
        if (request.getDetalles() != null) {
            List<OrdenCompraDetalle> nuevosDetalles = request.getDetalles().stream()
                    .map(d -> convertToEntity(d, orden))
                    .collect(Collectors.toList());
            orden.getDetalles().addAll(nuevosDetalles);
        }

        return convertToResponse(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional
    public void eliminar(Long id) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        if (!"BORRADOR".equals(orden.getEstado())) {
            throw new RuntimeException("Solo se pueden eliminar órdenes en estado BORRADOR");
        }
        ordenCompraRepository.delete(orden);
    }

    @Override
    @Transactional
    public OrdenCompraResponse aprobar(Long id, String usuario) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada"));
        orden.setEstado("APROBADO");
        orden.setUsuarioAprobacion(usuario);
        orden.setFechaAprobacion(LocalDateTime.now());
        return convertToResponse(ordenCompraRepository.save(orden));
    }

    @Override
    @Transactional
    public OrdenCompraResponse anular(Long id, String usuario, String motivo) {
        OrdenCompra orden = ordenCompraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada con ID: " + id));

        // Validaciones
        if ("ANULADO".equals(orden.getEstado())) {
            throw new RuntimeException("La orden ya se encuentra anulada");
        }

        if ("RECIBIDO".equals(orden.getEstado())) {
            throw new RuntimeException("No se puede anular una orden que ya ha sido recibida");
        }

        if ("PAGADO TOTAL".equals(orden.getEstadoPago())) {
            throw new RuntimeException("No se puede anular una orden que ya ha sido pagada totalmente");
        }

        if ("CON ADELANTO".equals(orden.getEstadoPago())) {
            // De acuerdo a la regla de negocio, podríamos permitirlo pero alertando,
            // o bloquearlo si hay dinero de por medio sin devolución.
            // Por simplicidad en esta versión, permitiremos anular pero marcando el rastro.
        }

        orden.setEstado("ANULADO");
        orden.setMotivoAnulacion(motivo);
        orden.setUsuarioAnulacion(usuario);
        orden.setFechaAnulacion(LocalDateTime.now());

        return convertToResponse(ordenCompraRepository.save(orden));
    }

    private OrdenCompraResponse convertToResponse(OrdenCompra entity) {
        return OrdenCompraResponse.builder()
                .id(entity.getId())
                .numeroOrden(entity.getNumeroOrden())
                .fechaEmision(entity.getFechaEmision())
                .fechaEstimadaEntrega(entity.getFechaEstimadaEntrega())
                .tipoCompra(entity.getTipoCompra())
                .centroCosto(entity.getCentroCosto())
                .areaSolicitante(entity.getAreaSolicitante())
                .responsableCompra(entity.getResponsableCompra())
                .moneda(entity.getMoneda())
                .tipoCambio(entity.getTipoCambio())
                .estado(entity.getEstado())
                .observaciones(entity.getObservaciones())
                .permiteAdelanto(entity.getPermiteAdelanto())
                .tipoAdelanto(entity.getTipoAdelanto())
                .porcentajeAdelanto(entity.getPorcentajeAdelanto())
                .montoAdelanto(entity.getMontoAdelanto())
                .fechaAdelanto(entity.getFechaAdelanto())
                .estadoAdelanto(entity.getEstadoAdelanto())
                .saldoPendienteAdelanto(entity.getSaldoPendienteAdelanto())
                .idProveedor(entity.getProveedor().getId())
                .rucProveedor(entity.getProveedor().getNumDocIdent())
                .razonSocialProveedor(entity.getProveedor().getRazonSocial())
                .subtotal(entity.getSubtotal())
                .totalDescuento(entity.getTotalDescuento())
                .totalIgv(entity.getTotalIgv())
                .totalGeneral(entity.getTotalGeneral())
                .totalAdelantado(entity.getTotalAdelantado())
                .saldoPorPagar(entity.getSaldoPorPagar())
                .estadoPago(entity.getEstadoPago())
                .lugarEntrega(entity.getLugarEntrega())
                .direccionEntrega(entity.getDireccionEntrega())
                .fechaEntregaAcordada(entity.getFechaEntregaAcordada())
                .responsableRecepcion(entity.getResponsableRecepcion())
                .observacionesLogisticas(entity.getObservacionesLogisticas())
                .usuarioRegistro(entity.getUsuarioRegistro())
                .fechaRegistro(entity.getFechaRegistro())
                .usuarioAprobacion(entity.getUsuarioAprobacion())
                .fechaAprobacion(entity.getFechaAprobacion())
                .motivoAnulacion(entity.getMotivoAnulacion())
                .numCotizacionRelacionada(entity.getNumCotizacionRelacionada())
                .numRequerimientoInterno(entity.getNumRequerimientoInterno())
                .condicionPago(entity.getCondicionPago())
                .plazoDias(entity.getPlazoDias())
                .detalles(entity.getDetalles().stream().map(this::convertToDetailDto).collect(Collectors.toList()))
                .build();
    }

    private OrdenCompraDetalleDto convertToDetailDto(OrdenCompraDetalle entity) {
        return OrdenCompraDetalleDto.builder()
                .id(entity.getId())
                .codigoItem(entity.getCodigoItem())
                .descripcion(entity.getDescripcion())
                .unidadMedida(entity.getUnidadMedida())
                .cantidad(entity.getCantidad())
                .precioUnitario(entity.getPrecioUnitario())
                .descuento(entity.getDescuento())
                .subtotal(entity.getSubtotal())
                .afectoIgv(entity.getAfectoIgv())
                .igv(entity.getIgv())
                .total(entity.getTotal())
                .build();
    }

    private OrdenCompraDetalle convertToEntity(OrdenCompraDetalleDto dto, OrdenCompra orden) {
        return OrdenCompraDetalle.builder()
                .ordenCompra(orden)
                .codigoItem(dto.getCodigoItem())
                .descripcion(dto.getDescripcion())
                .unidadMedida(dto.getUnidadMedida())
                .cantidad(dto.getCantidad())
                .precioUnitario(dto.getPrecioUnitario())
                .descuento(dto.getDescuento())
                .subtotal(dto.getSubtotal())
                .afectoIgv(dto.getAfectoIgv())
                .igv(dto.getIgv())
                .total(dto.getTotal())
                .build();
    }
}
