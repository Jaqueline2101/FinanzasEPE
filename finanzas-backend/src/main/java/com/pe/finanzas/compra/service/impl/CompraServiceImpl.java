package com.pe.finanzas.compra.service.impl;

import com.pe.finanzas.compra.dto.CompraDTO;
import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.entity.CronogramaPago;
import com.pe.finanzas.compra.entity.DetalleCompra;
import com.pe.finanzas.compra.repository.CompraRepository;
import com.pe.finanzas.compra.service.CompraService;
import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import com.pe.finanzas.proveedores.service.CuentaProveedorService;
import com.pe.finanzas.service.KafkaProducerService;
import com.pe.finanzas.compra.repository.CronogramaPagoRepository;
import com.pe.finanzas.shared.service.FileStorageService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CompraServiceImpl implements CompraService {

    private final CompraRepository compraRepository;
    private final ProveedorRepository proveedorRepository;
    private final ProductoRepository productoRepository;
    private final KafkaProducerService kafkaProducerService;
    private final CronogramaPagoRepository cronogramaPagoRepository;
    private final FileStorageService fileStorageService;
    private final CuentaProveedorService cuentaProveedorService;

    @Override
    public Page<Compra> listBySucursal(
            Long idSucursal,
            String term,
            String searchType,
            String estado,
            String fechaDesde,
            String fechaHasta,
            Pageable pageable) {

        Specification<Compra> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("idSucursal"), idSucursal));

            if (term != null && !term.isBlank()) {
                String pattern = "%" + term.toLowerCase() + "%";
                if ("ALL".equalsIgnoreCase(searchType) || searchType == null) {
                    Predicate seriePred = cb.like(cb.lower(root.get("serie")), pattern);
                    Predicate correlativoPred = cb.like(cb.lower(root.get("correlativo")), pattern);
                    Predicate proveedorPred = cb.like(cb.lower(root.get("proveedor").get("razonSocial")), pattern);
                    predicates.add(cb.or(seriePred, correlativoPred, proveedorPred));
                } else if ("proveedor".equalsIgnoreCase(searchType)) {
                    predicates.add(cb.like(cb.lower(root.get("proveedor").get("razonSocial")), pattern));
                } else {
                    predicates.add(cb.like(cb.lower(root.get(searchType)), pattern));
                }
            }

            if (estado != null && !"TODOS".equalsIgnoreCase(estado)) {
                Integer estadoValue = "ACTIVO".equalsIgnoreCase(estado) ? 1 : 0;
                predicates.add(cb.equal(root.get("estado"), estadoValue));
            }

            if (fechaDesde != null && !fechaDesde.isBlank()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("fechaEmision"), LocalDate.parse(fechaDesde)));
            }
            if (fechaHasta != null && !fechaHasta.isBlank()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("fechaEmision"), LocalDate.parse(fechaHasta)));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return compraRepository.findAll(spec, pageable);
    }

    @Override
    @Transactional
    public Compra registrarCompra(CompraDTO dto) {
        log.info("Registrando nueva compra para Proveedor ID: {}", dto.getIdProveedor());

        Proveedor proveedor = proveedorRepository.findById(dto.getIdProveedor())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con ID: " + dto.getIdProveedor()));

        Compra compra = Compra.builder()
                .proveedor(proveedor)
                .idSucursal(dto.getIdSucursal())
                .tipoComprobante(dto.getTipoComprobante())
                .serie(dto.getSerie())
                .correlativo(dto.getCorrelativo())
                .fechaEmision(dto.getFechaEmision())
                .fechaVencimiento(dto.getFechaVencimiento())
                .condicionPago(dto.getCondicionPago())
                .moneda(dto.getMoneda())
                .valorVentaGravado(BigDecimal.ZERO)
                .valorVentaInafecto(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .igv(BigDecimal.ZERO)
                .total(BigDecimal.ZERO)
                .percepcion(dto.getPercepcion() != null ? dto.getPercepcion() : BigDecimal.ZERO)
                .totalPagar(BigDecimal.ZERO)
                .porcentajeIgv(dto.getPorcentajeIgv() != null ? dto.getPorcentajeIgv() : new BigDecimal("18.00"))
                .estado(1)
                .detalles(new ArrayList<>())
                .build();

        if (dto.getDetalles() != null) {
            for (CompraDTO.DetalleCompraDTO detDto : dto.getDetalles()) {
                Producto producto = null;

                if (detDto.getIdProducto() != null) {
                    producto = productoRepository.findById(detDto.getIdProducto())
                            .orElseThrow(() -> new RuntimeException(
                                    "Producto no encontrado con ID: " + detDto.getIdProducto()));

                    if (!producto.getIdSucursal().equals(dto.getIdSucursal())) {
                        throw new RuntimeException("El producto " + producto.getNombreProducto()
                                + " no pertenece a la sucursal de la compra.");
                    }

                    BigDecimal cantidadCompra = detDto.getCantidad();
                    BigDecimal costoCompraUnitario = Boolean.TRUE.equals(detDto.getEsBonificacion())
                            ? BigDecimal.ZERO
                            : detDto.getPrecioUnitario();

                    BigDecimal stockActual = producto.getStockReal() != null ? producto.getStockReal()
                            : BigDecimal.ZERO;
                    BigDecimal costoPromedioActual = producto.getPrecioCompra() != null ? producto.getPrecioCompra()
                            : BigDecimal.ZERO;

                    BigDecimal nuevoStockTotal = stockActual.add(cantidadCompra);

                    if (nuevoStockTotal.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal valorInventarioActual = stockActual.multiply(costoPromedioActual);
                        BigDecimal valorNuevaCompra = cantidadCompra.multiply(costoCompraUnitario);

                        BigDecimal nuevoCostoPromedio = valorInventarioActual.add(valorNuevaCompra)
                                .divide(nuevoStockTotal, 4, RoundingMode.HALF_UP);

                        producto.setPrecioCompra(nuevoCostoPromedio);
                        producto.setStockReal(nuevoStockTotal);
                    } else {
                        producto.setStockReal(BigDecimal.ZERO);
                    }
                    productoRepository.save(producto);
                }

                String tipoAfectacion = detDto.getTipoAfectacion();
                if (tipoAfectacion == null || tipoAfectacion.isBlank()) {
                    tipoAfectacion = Boolean.TRUE.equals(detDto.getEsBonificacion())
                            ? "INAFECTO_GRATUITO"
                            : "GRAVADO_ONEROSO";
                }

                DetalleCompra detalle = DetalleCompra.builder()
                        .compra(compra)
                        .catalogo(producto != null ? producto.getCatalogo() : null)
                        .idProductoStock(producto != null ? producto.getIdProducto() : null)
                        .productoNombre(detDto.getProductoNombre())
                        .descripcion(detDto.getDescripcion())
                        .cantidad(detDto.getCantidad())
                        .unidadMedida(
                                detDto.getUnidadMedida() != null ? Integer.valueOf(detDto.getUnidadMedida()) : null)
                        .factorConversion(detDto.getFactorConversion())
                        .lote(detDto.getLote())
                        .fechaVencimiento(detDto.getFechaVencimiento())
                        .precioUnitario(detDto.getPrecioUnitario())
                        .porcentajeDescuento(detDto.getPorcentajeDescuento())
                        .esBonificacion(detDto.getEsBonificacion())
                        .tipoAfectacion(tipoAfectacion)
                        .valorVenta(detDto.getValorVenta())
                        .build();

                compra.getDetalles().add(detalle);
            }
        }

        if ("CREDITO".equalsIgnoreCase(dto.getCondicionPago())) {
            int cuotas = dto.getCuotas() != null ? dto.getCuotas() : 1;
            int diasFrecuencia = dto.getDiasFrecuencia() != null ? dto.getDiasFrecuencia() : 30;

        }

        recalcularTotalesCompra(compra);

        if ("CREDITO".equalsIgnoreCase(dto.getCondicionPago())) {
            int cuotas = dto.getCuotas() != null ? dto.getCuotas() : 1;
            int diasFrecuencia = dto.getDiasFrecuencia() != null ? dto.getDiasFrecuencia() : 30;

            BigDecimal totalPagar = compra.getTotalPagar();
            BigDecimal montoCuotaBase = totalPagar.divide(BigDecimal.valueOf(cuotas), 2, RoundingMode.HALF_UP);

            BigDecimal acumulado = BigDecimal.ZERO;

            if (compra.getCronograma() == null) {
                compra.setCronograma(new ArrayList<>());
            }

            for (int i = 1; i <= cuotas; i++) {
                BigDecimal montoActual = (i == cuotas)
                        ? totalPagar.subtract(acumulado)
                        : montoCuotaBase;

                acumulado = acumulado.add(montoActual);

                CronogramaPago cuota = CronogramaPago.builder()
                        .compra(compra)
                        .numeroCuota(i)
                        .fechaVencimiento(compra.getFechaEmision().plusDays((long) i * diasFrecuencia))
                        .montoCuota(montoActual)
                        .estado(2)
                        .build();

                compra.getCronograma().add(cuota);
            }
            log.info("Cronograma generado: {} cuotas para Compra {}", cuotas,
                    compra.getSerie() + "-" + compra.getCorrelativo());
        }

        return compraRepository.save(compra);
    }

    private void recalcularTotalesCompra(Compra compra) {
        BigDecimal totalGravado = BigDecimal.ZERO;
        BigDecimal totalInafecto = BigDecimal.ZERO;
        BigDecimal totalExonerado = BigDecimal.ZERO;

        for (DetalleCompra detalle : compra.getDetalles()) {
            BigDecimal valorLinea = detalle.getValorVenta() != null ? detalle.getValorVenta() : BigDecimal.ZERO;
            String tipoAfectacion = detalle.getTipoAfectacion();

            if (tipoAfectacion != null) {
                if (tipoAfectacion.startsWith("GRAVADO")) {

                    totalGravado = totalGravado.add(valorLinea);
                } else if (tipoAfectacion.startsWith("INAFECTO")) {

                    totalInafecto = totalInafecto.add(valorLinea);
                } else if (tipoAfectacion.startsWith("EXONERADO")) {

                    totalExonerado = totalExonerado.add(valorLinea);
                }
            } else {

                totalGravado = totalGravado.add(valorLinea);
            }
        }

        BigDecimal taxRate = (compra.getPorcentajeIgv() != null)
                ? compra.getPorcentajeIgv().divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP)
                : new BigDecimal("0.18");
        BigDecimal igv = totalGravado.multiply(taxRate).setScale(2, RoundingMode.HALF_UP);

        BigDecimal subtotal = totalGravado.add(totalInafecto).add(totalExonerado);

        BigDecimal total = subtotal.add(igv);

        BigDecimal percepcion = compra.getPercepcion() != null ? compra.getPercepcion() : BigDecimal.ZERO;
        BigDecimal totalPagar = total.add(percepcion);

        compra.setValorVentaGravado(totalGravado);
        compra.setValorVentaInafecto(totalInafecto.add(totalExonerado));
        compra.setSubtotal(subtotal);
        compra.setIgv(igv);
        compra.setTotal(total);
        compra.setTotalPagar(totalPagar);

        log.info("Totales recalculados - Gravado: {}, Inafecto: {}, IGV: {}, Total: {}",
                totalGravado, totalInafecto.add(totalExonerado), igv, total);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Compra> findById(Long id) {
        return compraRepository.findById(id);
    }

    @Override
    @Transactional
    public void eliminarCompra(Long id) {
        Compra compra = compraRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Compra no encontrada con ID: " + id));

        for (DetalleCompra detalle : compra.getDetalles()) {
            BigDecimal cantidad = detalle.getCantidad();
            Long idStock = detalle.getIdProductoStock();

            if (idStock != null) {
                productoRepository.findById(idStock).ifPresent(producto -> {
                    if (producto.getStockReal() != null) {
                        producto.setStockReal(producto.getStockReal().subtract(cantidad));
                        if (producto.getStockReal().compareTo(BigDecimal.ZERO) < 0) {
                            producto.setStockReal(BigDecimal.ZERO);
                        }
                        log.info("Reversión de compra: Producto {} Stock reducido en {}", producto.getIdProducto(),
                                cantidad);
                        productoRepository.save(producto);
                    }
                });
            }
        }

        compraRepository.delete(compra);
        log.info("Compra eliminada con ID: {}", id);
    }

    @Override
    public Page<CronogramaPago> listarDeudas(java.util.Map<String, Object> params, Pageable pageable) {
        return cronogramaPagoRepository.findAll(pageable);
    }

    @Override
    @Transactional
    public CronogramaPago registrarPagoCuota(Long idCronograma, String numeroOperacion,
            org.springframework.web.multipart.MultipartFile voucher) {
        CronogramaPago cuota = cronogramaPagoRepository.findById(idCronograma)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cuota no encontrada"));

        if (cuota.getEstado() != null && cuota.getEstado() == 16) {
            throw new IllegalStateException("La cuota ya está pagada.");
        }

        String path = null;
        if (voucher != null && !voucher.isEmpty()) {
            path = fileStorageService.storeFile(voucher);
        }

        String infoPago = "OP: " + numeroOperacion;
        if (path != null)
            infoPago += " | FILE: " + path;

        cuota.setComprobantePago(infoPago);
        cuota.setEstado(16);
        cuota.setFechaPagoReal(java.time.LocalDateTime.now());

        log.info("Cuota {} pagada. Info: {}", idCronograma, infoPago);

        // Registro en la cuenta del proveedor (Abono)
        try {
            Long idProveedor = cuota.getCompra().getProveedor().getId();
            BigDecimal monto = cuota.getMontoCuota();
            String descripcion = "PAGO CUOTA #" + cuota.getNumeroCuota() + " - COMPRA: " +
                    cuota.getCompra().getSerie() + "-" + cuota.getCompra().getCorrelativo();

            cuentaProveedorService.registrarMovimiento(
                    idProveedor,
                    "ABONO",
                    monto,
                    descripcion,
                    "SISTEMA", // Usuario por defecto o sistema
                    cuota.getCompra().getIdSucursal(),
                    false);
            log.info("Abono registrado en cuenta de proveedor ID: {}", idProveedor);
        } catch (Exception e) {
            log.error("Error al registrar abono en cuenta de proveedor: {}", e.getMessage());
        }

        kafkaProducerService.enviarNotificacion(
                null,
                "COMPRAS",
                "Pago Realizado",
                "Se ha registrado el pago de la cuota " + cuota.getNumeroCuota() + " de la compra #"
                        + cuota.getCompra().getId(),
                "SUCCESS");

        return cronogramaPagoRepository.save(cuota);
    }

    @Override
    @Transactional
    public java.util.List<CronogramaPago> registrarPagosMasivos(java.util.List<Long> idsCronograma,
            String numeroOperacion, org.springframework.web.multipart.MultipartFile voucher) {
        if (idsCronograma == null || idsCronograma.isEmpty()) {
            return new java.util.ArrayList<>();
        }

        String path = null;
        if (voucher != null && !voucher.isEmpty()) {
            path = fileStorageService.storeFile(voucher);
        }

        String infoPago = "OP: " + numeroOperacion;
        if (path != null) {
            infoPago += " | FILE: " + path;
        }

        java.util.List<CronogramaPago> resultados = new java.util.ArrayList<>();
        java.time.LocalDateTime now = java.time.LocalDateTime.now();

        for (Long id : idsCronograma) {
            CronogramaPago cuota = cronogramaPagoRepository.findById(id)
                    .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Cuota no encontrada: " + id));

            if (cuota.getEstado() != null && cuota.getEstado() == 16) {
                throw new IllegalStateException("La cuota con ID " + id + " ya está pagada.");
            }

            cuota.setComprobantePago(infoPago);
            cuota.setEstado(16);
            cuota.setFechaPagoReal(now);

            // Registro en la cuenta del proveedor (Abono)
            try {
                Long idProveedor = cuota.getCompra().getProveedor().getId();
                BigDecimal monto = cuota.getMontoCuota();
                String descripcion = "PAGO MASIVO CUOTA #" + cuota.getNumeroCuota() + " - COMPRA: " +
                        cuota.getCompra().getSerie() + "-" + cuota.getCompra().getCorrelativo() +
                        " (OP: " + numeroOperacion + ")";

                cuentaProveedorService.registrarMovimiento(
                        idProveedor,
                        "ABONO",
                        monto,
                        descripcion,
                        "SISTEMA",
                        cuota.getCompra().getIdSucursal(),
                        false);
            } catch (Exception e) {
                log.error("Error al registrar abono masivo en cuenta de proveedor: {}", e.getMessage());
            }

            resultados.add(cronogramaPagoRepository.save(cuota));
        }

        log.info("Pago masivo procesado exitosamente para {} cuotas. OP: {}", idsCronograma.size(), numeroOperacion);
        return resultados;
    }
}