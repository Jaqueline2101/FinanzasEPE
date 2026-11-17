package com.pe.finanzas.venta_registro.service.impl;

import com.pe.finanzas.descuentos.dto.DescuentoAplicable;
import com.pe.finanzas.descuentos.service.AplicacionDescuentoService;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.venta_registro.dto.VentaDetalleDto;
import com.pe.finanzas.venta_registro.dto.VentaRegistroDto;
import com.pe.finanzas.venta_registro.dto.VentaRequest;
import com.pe.finanzas.venta_registro.entity.VentaDetalle;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaDetalleRepository;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import com.pe.finanzas.venta_registro.repository.SerieCorrelativoRepository;
import com.pe.finanzas.venta_registro.entity.SerieCorrelativo;
import com.pe.finanzas.venta_registro.service.CalculoVentaService;
import com.pe.finanzas.venta_registro.service.VentaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.pe.finanzas.venta_registro.dto.LabOrdenDto;
import com.pe.finanzas.venta_registro.dto.PagarOrdenRequest;
import com.pe.finanzas.venta_registro.entity.LabOrden;
import com.pe.finanzas.venta_registro.entity.LabOrdenDetalle;
import com.pe.finanzas.venta_registro.service.LiquidacionSeguroService;
import com.pe.finanzas.autorizar_descuento.repository.AutorizacionDescuentoRepository;
import com.pe.finanzas.venta_registro.repository.LabOrdenRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class VentaServiceImpl implements VentaService {

        private final VentaRegistroRepository ventaRegistroRepository;
        private final VentaDetalleRepository ventaDetalleRepository;
        private final CatalogoRepository servicioRepository;
        private final com.pe.finanzas.paquetes.repository.PaqueteRepository paqueteRepository;
        private final CalculoVentaService calculoService;
        private final AplicacionDescuentoService aplicacionDescuentoService;
        private final com.pe.finanzas.users.repository.DatosPersonalesRepository datosPersonalesRepository;
        private final com.pe.finanzas.motor_precios.service.MotorPreciosService motorPreciosService;
        private final com.pe.finanzas.caja_chica.services.CajaChicaService cajaChicaService;
        private final SerieCorrelativoRepository correlativoRepository;
        private final LabOrdenRepository labOrdenRepository;
        private final com.pe.finanzas.motor_precios.repository.CredPlanRepository credPlanRepository;
        private final LiquidacionSeguroService liquidacionSeguroService;
        private final AutorizacionDescuentoRepository autorizacionDescuentoRepository;

        @Override

        public VentaRegistroDto crearVenta(VentaRequest request) {
                log.info("Creando venta - Serie: {}, Número: {}", request.getSerie(), request.getNumdoc());

                boolean esAutogenerado = request.getNumdoc() == null ||
                                request.getNumdoc().equals("00000000") ||
                                request.getNumdoc().equalsIgnoreCase("GENERATED") ||
                                request.getNumdoc().isEmpty();

                String numeroFinal = request.getNumdoc();

                if (esAutogenerado) {
                        String tipoDocSimple = request.getTipoDoc();
                        SerieCorrelativo serieCorr = correlativoRepository
                                        .findBySucursalAndTipoAndSerieForUpdate(request.getIdSucursal(), tipoDocSimple,
                                                        request.getSerie())
                                        .orElseGet(() -> {

                                                return SerieCorrelativo.builder()
                                                                .idSucursal(request.getIdSucursal())
                                                                .tipoDoc(tipoDocSimple)
                                                                .serie(request.getSerie())
                                                                .ultimoNumero(0L)
                                                                .longitudNumero(8)
                                                                .build();
                                        });

                        numeroFinal = serieCorr.getNextNumeroPadded();
                        serieCorr.setUltimoNumero(serieCorr.getUltimoNumero() + 1);
                        correlativoRepository.save(serieCorr);

                        log.info("🔢 Generado nuevo correlativo ATÓMICO: {} - {}", request.getSerie(), numeroFinal);
                } else {
                        if (ventaRegistroRepository.existsBySerieAndNumdoc(request.getSerie(), request.getNumdoc())) {
                                throw new IllegalArgumentException(
                                                "Ya existe una venta con la serie " + request.getSerie()
                                                                + " y número " + request.getNumdoc());
                        }
                }

                if (request.getIdOrden() != null) {
                        if (ventaRegistroRepository.existsByIdOrden(request.getIdOrden())) {
                                throw new IllegalStateException("Esta orden ya tiene una venta registrada");
                        }
                }

                List<String> codes = new ArrayList<>();
                if (request.getMontoEfectivo() != null && request.getMontoEfectivo().compareTo(BigDecimal.ZERO) > 0)
                        codes.add("EF");
                if (request.getMontoTarjeta() != null && request.getMontoTarjeta().compareTo(BigDecimal.ZERO) > 0)
                        codes.add("TJ");
                if (request.getMontoTransferencia() != null
                                && request.getMontoTransferencia().compareTo(BigDecimal.ZERO) > 0)
                        codes.add("TR");

                String finalMetodo = request.getMetodoPago();
                if (!codes.isEmpty()) {
                        finalMetodo = String.join("+", codes); // e.g., "EF+TJ"
                }

                StringBuilder refs = new StringBuilder();
                if (request.getRefMetodoPago() != null && !request.getRefMetodoPago().isEmpty())
                        refs.append(request.getRefMetodoPago());

                if (codes.size() > 1 || !codes.contains("EF")) {
                        if (request.getRefTarjeta() != null && !request.getRefTarjeta().isEmpty())
                                refs.append(refs.length() > 0 ? ", " : "").append("Tarj: ")
                                                .append(request.getRefTarjeta());
                        if (request.getRefTransferencia() != null && !request.getRefTransferencia().isEmpty())
                                refs.append(refs.length() > 0 ? ", " : "").append("Trans: ")
                                                .append(request.getRefTransferencia());
                }

                String tipoAtencionNorm = null;
                if (request.getTipoAtencion() != null && !request.getTipoAtencion().isBlank()) {
                        tipoAtencionNorm = request.getTipoAtencion().toUpperCase().trim();
                }

                // MAPEADO SENIOR: Convertimos palabras completas a códigos legacy (1-2 chars)
                // para que quepan en la DB
                String tipoDocFinal = request.getTipoDoc();
                if ("BOLETA".equalsIgnoreCase(tipoDocFinal))
                        tipoDocFinal = "B";
                else if ("FACTURA".equalsIgnoreCase(tipoDocFinal))
                        tipoDocFinal = "F";
                else if ("TICKET".equalsIgnoreCase(tipoDocFinal))
                        tipoDocFinal = "T";
                else if ("NOTA_CREDITO".equalsIgnoreCase(tipoDocFinal))
                        tipoDocFinal = "NC";
                else if ("NOTA_DEBITO".equalsIgnoreCase(tipoDocFinal))
                        tipoDocFinal = "ND";

                VentaRegistro venta = VentaRegistro.builder()
                                .idSucursal(request.getIdSucursal())
                                .idCtacte(request.getIdCtacte())
                                .idPersonal(request.getIdPersonal())
                                .idAlmacen(request.getIdAlmacen())
                                .fecha(request.getFecha())
                                .tipoDoc(tipoDocFinal)
                                .serie(request.getSerie())
                                .numdoc(numeroFinal)
                                .nombrePac(request.getNombrePac())
                                .nroDni(request.getNroDni())
                                .tipoDni(request.getTipoDni())
                                .ruc(request.getRuc())
                                .razon(request.getRazon())
                                .direcRuc(request.getDirecRuc())
                                .metodoPago(finalMetodo != null && finalMetodo.matches("\\d+")
                                                ? Long.valueOf(finalMetodo)
                                                : null)
                                .refMetodoPago(refs.toString())
                                .importeSoles(request.getMontoEfectivo())
                                .importeTarjeta(request.getMontoTarjeta())
                                .importeDolares(request.getMontoTransferencia())
                                .vuelto(request.getVuelto())
                                .tipoAtencion(tipoAtencionNorm)
                                .tipoPac(request.getTipoPac())
                                .idPlan(request.getIdPlan())
                                .estado(16)
                                .detalles(new ArrayList<>())
                                .build();

                BigDecimal totalValorAfecto = BigDecimal.ZERO;
                BigDecimal totalValorInaf = BigDecimal.ZERO;
                BigDecimal totalDescuento = BigDecimal.ZERO;
                BigDecimal totalCobertura = BigDecimal.ZERO;
                BigDecimal totalBaseImp = BigDecimal.ZERO;
                BigDecimal totalIgv = BigDecimal.ZERO;
                BigDecimal totalGeneral = BigDecimal.ZERO;

                for (VentaRequest.VentaItemRequest itemRequest : request.getItems()) {
                        log.debug("Procesando item - Servicio ID: {}, Cantidad: {}",
                                        itemRequest.getIdArticulo(), itemRequest.getCantidad());

                        if ("PAQUETE".equalsIgnoreCase(itemRequest.getTipoArticulo())) {
                                log.info("📦 Detectado PAQUETE explícito con ID: {}. Iniciando desglose...",
                                                itemRequest.getIdArticulo());

                                var paqueteOpt = paqueteRepository.findById(itemRequest.getIdArticulo());
                                if (paqueteOpt.isPresent()) {
                                        var paquete = paqueteOpt.get();
                                        BigDecimal precioOriginalPaquete = paquete.getPrecioPaquete();
                                        BigDecimal precioCobradoPaquete = itemRequest.getPrecioUnitario() != null
                                                        ? itemRequest.getPrecioUnitario()
                                                        : (precioOriginalPaquete.subtract(paquete.getDescuento() != null
                                                                        ? paquete.getDescuento()
                                                                        : BigDecimal.ZERO));

                                        BigDecimal factorEscala = precioOriginalPaquete.compareTo(BigDecimal.ZERO) > 0
                                                        ? precioCobradoPaquete.divide(precioOriginalPaquete, 4,
                                                                        RoundingMode.HALF_UP)
                                                        : BigDecimal.ONE;

                                        for (var pDetalle : paquete.getDetalles()) {
                                                Catalogo subServicio = pDetalle.getServicio();
                                                BigDecimal cantidadDetalle = pDetalle.getCantidad() != null
                                                                ? pDetalle.getCantidad()
                                                                : BigDecimal.ONE;
                                                BigDecimal cantidadComprada = cantidadDetalle
                                                                .multiply(itemRequest.getCantidad());

                                                BigDecimal precioUnitarioBase = pDetalle.getPrecio();
                                                if (precioUnitarioBase == null) {
                                                        precioUnitarioBase = subServicio.getPrecioConIgv() != null
                                                                        ? subServicio.getPrecioConIgv()
                                                                        : (subServicio.getPrecioSinIgv() != null
                                                                                        ? subServicio.getPrecioSinIgv()
                                                                                        : BigDecimal.ZERO);
                                                }

                                                BigDecimal precioNormalDetalle = precioUnitarioBase
                                                                .multiply(cantidadDetalle);
                                                BigDecimal precioNormalTotal = precioNormalDetalle
                                                                .multiply(itemRequest.getCantidad());

                                                BigDecimal precioFinalDetalle = pDetalle.getPrecio() != null
                                                                ? pDetalle.calcularPrecioFinal()
                                                                : precioNormalDetalle;

                                                BigDecimal precioFinalItem = precioFinalDetalle
                                                                .multiply(factorEscala)
                                                                .setScale(4, RoundingMode.HALF_UP);
                                                BigDecimal totalCobradoItem = precioFinalItem
                                                                .multiply(itemRequest.getCantidad());

                                                BigDecimal descuentoReal = precioNormalTotal.subtract(totalCobradoItem);
                                                if (descuentoReal.compareTo(BigDecimal.ZERO) < 0)
                                                        descuentoReal = BigDecimal.ZERO;

                                                boolean incluyeIgv = subServicio.getPrecioConIgv() != null;
                                                boolean esInafecto = !incluyeIgv;

                                                BigDecimal valorVentaPuro = calculoService.calcularValorVenta(
                                                                precioUnitarioBase, cantidadComprada);

                                                BigDecimal baseCalculo = calculoService.calcularBaseImponible(
                                                                totalCobradoItem, BigDecimal.ZERO);

                                                BigDecimal baseImpItem = BigDecimal.ZERO;
                                                BigDecimal igvItem = BigDecimal.ZERO;
                                                BigDecimal valorAfectoItem = BigDecimal.ZERO;
                                                BigDecimal valorInafectoItem = BigDecimal.ZERO;

                                                if (esInafecto) {
                                                        valorInafectoItem = baseCalculo;
                                                        baseImpItem = baseCalculo;
                                                } else {
                                                        baseImpItem = baseCalculo.divide(new BigDecimal("1.18"), 4,
                                                                        RoundingMode.HALF_UP);
                                                        igvItem = baseCalculo.subtract(baseImpItem);
                                                        valorAfectoItem = baseImpItem;
                                                }

                                                VentaDetalle detalle = VentaDetalle.builder()
                                                                .idArticulo(subServicio.getId())
                                                                .glosa(subServicio.getNombre() + " (Paquete)")
                                                                .cantidad(cantidadComprada)
                                                                .precioUnitario(precioUnitarioBase.setScale(4,
                                                                                RoundingMode.HALF_UP)) // PRECIO REAL
                                                                .valorVenta(valorVentaPuro)
                                                                .descuento(descuentoReal) // DESCUENTO REAL
                                                                .cobertura(BigDecimal.ZERO)
                                                                .valorAf(valorAfectoItem)
                                                                .valorInaf(valorInafectoItem)
                                                                .baseImp(baseImpItem)
                                                                .igv(igvItem)
                                                                .total(totalCobradoItem)
                                                                .porcIgv(esInafecto ? BigDecimal.ZERO
                                                                                : new BigDecimal("18.00"))
                                                                .porcDsc(pDetalle.getDescuentoPorcentaje())
                                                                .porcCob(BigDecimal.ZERO)
                                                                .fecha(request.getFecha())
                                                                .estado(1)
                                                                .idPaquete(paquete.getIdPaquete())
                                                                .build();

                                                venta.addDetalle(detalle);

                                                totalBaseImp = totalBaseImp.add(detalle.getBaseImp());
                                                totalGeneral = totalGeneral.add(detalle.getTotal());
                                                totalDescuento = totalDescuento.add(detalle.getDescuento());
                                        }
                                        continue;
                                } else {
                                        throw new ResourceNotFoundException(
                                                        "Paquete no encontrado con ID: " + itemRequest.getIdArticulo());
                                }
                        }

                        // --- LÓGICA PARA SERVICIOS INDIVIDUALES (EXISTENTE) ---
                        Catalogo servicio = servicioRepository.findById(itemRequest.getIdArticulo())
                                        .orElseThrow(() -> new ResourceNotFoundException(
                                                        "Servicio no encontrado: " + itemRequest.getIdArticulo()));

                        // (Por si acaso viene PAQUETE legacy sin la bandera)
                        if (servicio.getTipo() == Catalogo.Tipo.PAQUETE
                                        && !"PAQUETE".equalsIgnoreCase(itemRequest.getTipoArticulo())) {
                                log.warn("Paquete detectado en catálogo pero no fue enviado con tipoArticulo='PAQUETE'. Se omitió la explosión detallada.");
                        }

                        // --- LÓGICA PARA SERVICIOS INDIVIDUALES (EXISTENTE) ---
                        boolean incluyeIgv = false;
                        BigDecimal precioBase;

                        if (servicio.getPrecioConIgv() != null) {
                                precioBase = servicio.getPrecioConIgv();
                                incluyeIgv = true;
                        } else {
                                precioBase = servicio.getPrecioSinIgv();
                                incluyeIgv = false;
                        }

                        boolean esInafecto = false;
                        BigDecimal precioBaseParaCalculo;

                        if (itemRequest.getPrecioUnitario() != null) {
                                precioBaseParaCalculo = itemRequest.getPrecioUnitario();
                        } else {
                                var precioCalculado = motorPreciosService.calcularPrecio(
                                                request.getIdCtacte(),
                                                servicio.getId(),
                                                precioBase,
                                                "AMBULATORIO",
                                                servicio.getNivel() != null
                                                                ? servicio.getNivel().getIdNivel().intValue()
                                                                : 1);
                                precioBaseParaCalculo = precioCalculado.getPrecioBase();
                        }

                        BigDecimal valorVentaItem = calculoService.calcularValorVenta(
                                        precioBaseParaCalculo, itemRequest.getCantidad());

                        BigDecimal porcentajeDesc = BigDecimal.ZERO;
                        BigDecimal porcentajeCob = BigDecimal.ZERO;
                        String tipoDescuentoAplicado = itemRequest.getTipoDescuento();

                        if (Boolean.TRUE.equals(request.getAplicarDescuentoAutomatico()) &&
                                        request.getTipoPac() != null) {

                                Long idCompaniaKey = null;
                                Long idEmpresaKey = null;

                                if (request.getIdCtacte() != null) {
                                        var paciente = datosPersonalesRepository.findById(request.getIdCtacte())
                                                        .orElse(null);
                                        if (paciente != null) {
                                                idCompaniaKey = paciente.getIdCompania();
                                                idEmpresaKey = paciente.getIdEmpresa();
                                        }
                                }
                                Long idNivelItem = servicio.getNivel() != null ? servicio.getNivel().getIdNivel()
                                                : null;
                                List<DescuentoAplicable> descuentosAplicables = aplicacionDescuentoService
                                                .obtenerDescuentosAplicables(
                                                                itemRequest.getIdArticulo(),
                                                                request.getTipoPac(),
                                                                request.getFecha(),
                                                                request.getIdSucursal(),
                                                                idCompaniaKey,
                                                                idEmpresaKey,
                                                                idNivelItem);

                                var mejorDescuento = aplicacionDescuentoService
                                                .seleccionarMejorDescuento(descuentosAplicables);

                                if (mejorDescuento.isPresent()) {
                                        DescuentoAplicable desc = mejorDescuento.get();
                                        if ("PRECIO_FIJO".equals(desc.getTipoDescuento())
                                                        && desc.getPrecioFijo() != null) {
                                                // Override the base calculated price entirely
                                                valorVentaItem = calculoService.calcularValorVenta(
                                                                desc.getPrecioFijo(),
                                                                itemRequest.getCantidad());
                                                porcentajeDesc = BigDecimal.ZERO;
                                                porcentajeCob = BigDecimal.ZERO;
                                                tipoDescuentoAplicado = desc.getTipoDescuento();
                                                itemRequest.setPrecioUnitario(desc.getPrecioFijo()); // align entity
                                                                                                     // records
                                        } else {
                                                porcentajeDesc = desc.getPorcentajeDescuento();
                                                porcentajeCob = desc.getPorcentajeCobertura();
                                                tipoDescuentoAplicado = desc.getTipoDescuento();
                                        }
                                } else {
                                        porcentajeDesc = itemRequest.getPorcentajeDescuento() != null
                                                        ? itemRequest.getPorcentajeDescuento()
                                                        : BigDecimal.ZERO;
                                        porcentajeCob = itemRequest.getPorcentajeCobertura() != null
                                                        ? itemRequest.getPorcentajeCobertura()
                                                        : BigDecimal.ZERO;
                                }
                        } else {
                                porcentajeDesc = itemRequest.getPorcentajeDescuento() != null
                                                ? itemRequest.getPorcentajeDescuento()
                                                : BigDecimal.ZERO;
                                porcentajeCob = itemRequest.getPorcentajeCobertura() != null
                                                ? itemRequest.getPorcentajeCobertura()
                                                : BigDecimal.ZERO;
                        }

                        // Se respeta el descuento enviado por el frontend aunque envíen precioUnitario
                        // base

                        BigDecimal descuentoItem = calculoService.calcularDescuento(
                                        valorVentaItem, porcentajeDesc);

                        BigDecimal coberturaItem = calculoService.calcularDescuento(
                                        valorVentaItem, porcentajeCob);

                        BigDecimal baseCalculo = calculoService.calcularBaseImponible(
                                        valorVentaItem, descuentoItem);

                        BigDecimal igvItem = BigDecimal.ZERO;
                        BigDecimal valorAfectoItem = BigDecimal.ZERO;
                        BigDecimal valorInafItem = BigDecimal.ZERO;
                        BigDecimal baseImpItem = BigDecimal.ZERO;

                        if (!esInafecto) {
                                if (incluyeIgv) {
                                        baseImpItem = baseCalculo.divide(new BigDecimal("1.18"), 2,
                                                        java.math.RoundingMode.HALF_UP);
                                        igvItem = baseCalculo.subtract(baseImpItem);
                                        valorAfectoItem = baseImpItem;
                                } else {
                                        baseImpItem = baseCalculo;
                                        igvItem = calculoService.calcularIGV(baseImpItem);
                                        valorAfectoItem = baseImpItem;
                                }
                        } else {
                                baseImpItem = baseCalculo;
                                valorInafItem = baseImpItem;
                        }

                        BigDecimal totalItem = calculoService.calcularTotal(baseImpItem, igvItem);

                        VentaDetalle detalle = VentaDetalle.builder()
                                        .idArticulo(itemRequest.getIdArticulo())
                                        .glosa(itemRequest.getGlosa())
                                        .cantidad(itemRequest.getCantidad())
                                        .precioUnitario(itemRequest.getPrecioUnitario() != null
                                                        ? itemRequest.getPrecioUnitario()
                                                        : precioBase)
                                        .valorVenta(valorVentaItem)
                                        .descuento(descuentoItem)
                                        .cobertura(coberturaItem)
                                        .valorAf(valorAfectoItem)
                                        .valorInaf(valorInafItem)
                                        .baseImp(baseImpItem)
                                        .igv(igvItem)
                                        .total(totalItem)
                                        .porcIgv(esInafecto ? BigDecimal.ZERO : new BigDecimal("18.00"))
                                        .porcDsc(porcentajeDesc)
                                        .porcCob(porcentajeCob)
                                        .fecha(request.getFecha())
                                        .estado(1)
                                        .tipoDesc(tipoDescuentoAplicado)
                                        .idMedicoSer(itemRequest.getIdMedicoSer())
                                        .consultorio(itemRequest.getConsultorio())
                                        .build();

                        venta.addDetalle(detalle);
                        totalValorAfecto = totalValorAfecto.add(valorAfectoItem);
                        totalValorInaf = totalValorInaf.add(valorInafItem);
                        totalDescuento = totalDescuento.add(descuentoItem);
                        totalCobertura = totalCobertura.add(coberturaItem);
                        totalBaseImp = totalBaseImp.add(baseImpItem);
                        totalIgv = totalIgv.add(igvItem);
                        totalGeneral = totalGeneral.add(totalItem);
                }

                venta.setValorAfecto(calculoService.normalizarDecimales(totalValorAfecto));
                venta.setValorInaf(calculoService.normalizarDecimales(totalValorInaf));
                venta.setValorVenta(calculoService.normalizarDecimales(totalValorAfecto.add(totalValorInaf)));
                venta.setDescuento(calculoService.normalizarDecimales(totalDescuento));
                venta.setCobertura(calculoService.normalizarDecimales(totalCobertura));
                venta.setBaseImp(calculoService.normalizarDecimales(totalBaseImp));
                venta.setIgv(calculoService.normalizarDecimales(totalIgv));
                venta.setTotal(calculoService.normalizarDecimales(totalGeneral));

                // --- LÓGICA DE SEGURO MÉDICO (LIQUIDACIÓN) ---
                if (venta.getIdPlan() != null) {
                        var planOpt = credPlanRepository.findById(venta.getIdPlan());
                        if (planOpt.isPresent()) {
                                var plan = planOpt.get();
                                BigDecimal mCopago = request.getCopago() != null ? request.getCopago()
                                                : (plan.getCopago() != null ? plan.getCopago() : BigDecimal.ZERO);
                                BigDecimal mDeducible = request.getDeducible() != null ? request.getDeducible()
                                                : (plan.getDeducible() != null ? plan.getDeducible() : BigDecimal.ZERO);
                                BigDecimal mCoaseguro = plan.getPorcentajeCoaseguro() != null
                                                ? plan.getPorcentajeCoaseguro()
                                                : BigDecimal.ZERO;

                                var liquidacion = liquidacionSeguroService.calcular(venta.getTotal(), mCopago,
                                                mDeducible, mCoaseguro);

                                venta.setCopago(liquidacion.getCopago());
                                venta.setCobertura(liquidacion.getMontoPagoSeguro());
                                // El total de la venta sigue siendo el bruto, pero se registra la parte que
                                // cubre el seguro
                        }
                }

                VentaRegistro ventaGuardada = ventaRegistroRepository.save(venta);

                String metodoStored = ventaGuardada.getMetodoPago() != null
                                ? String.valueOf(ventaGuardada.getMetodoPago())
                                : null;
                if (metodoStored != null && (metodoStored.contains("+") || "MIXTO".equals(metodoStored))) {
                        if (ventaGuardada.getImporteSoles() != null
                                        && ventaGuardada.getImporteSoles().compareTo(BigDecimal.ZERO) > 0) {
                                registrarMovimiento(ventaGuardada, "EFECTIVO", ventaGuardada.getImporteSoles(), "");
                        }
                        if (ventaGuardada.getImporteTarjeta() != null
                                        && ventaGuardada.getImporteTarjeta().compareTo(BigDecimal.ZERO) > 0) {
                                registrarMovimiento(ventaGuardada, "TARJETA", ventaGuardada.getImporteTarjeta(),
                                                request.getRefTarjeta());
                        }
                        if (ventaGuardada.getImporteDolares() != null
                                        && ventaGuardada.getImporteDolares().compareTo(BigDecimal.ZERO) > 0) {
                                registrarMovimiento(ventaGuardada, "TRANSFERENCIA", ventaGuardada.getImporteDolares(),
                                                request.getRefTransferencia());
                        }
                } else {
                        registrarMovimiento(ventaGuardada,
                                        ventaGuardada.getMetodoPago() != null
                                                        ? String.valueOf(ventaGuardada.getMetodoPago())
                                                        : null,
                                        ventaGuardada.getTotal(),
                                        ventaGuardada.getRefMetodoPago());
                }

                log.info("Venta creada exitosamente - ID: {}, Total: {}", ventaGuardada.getIdVenta(),
                                ventaGuardada.getTotal());

                if (request.getIdAutorizacionDescuento() != null) {
                        autorizacionDescuentoRepository.findById(request.getIdAutorizacionDescuento())
                                        .ifPresent(auth -> {
                                                auth.setEstado("USADO");
                                                if (auth.getFechaResolucion() == null) {
                                                        auth.setFechaResolucion(LocalDateTime.now());
                                                }
                                                autorizacionDescuentoRepository.save(auth);
                                                log.info("🔐 Autorización {} marcada como USADA",
                                                                auth.getIdAutorizacion());
                                        });
                }

                return convertirADto(ventaGuardada);
        }

        @Override
        @Transactional(readOnly = true)
        public VentaRegistroDto obtenerVenta(Long idVenta) {
                log.debug("Obteniendo venta ID: {}", idVenta);

                VentaRegistro venta = ventaRegistroRepository.findByIdVenta(idVenta)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venta no encontrada con ID: " + idVenta));

                return convertirADto(venta);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<VentaRegistroDto> listarVentas(LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
                log.debug("Listando ventas desde {} hasta {}", fechaInicio, fechaFin);

                return ventaRegistroRepository.findByFechaBetween(fechaInicio, fechaFin, pageable)
                                .map(this::convertirADto);
        }

        @Override
        @Transactional(readOnly = true)
        public Page<VentaRegistroDto> listarVentasPorSucursal(
                        Long idSucursal, LocalDate fechaInicio, LocalDate fechaFin, Pageable pageable) {
                log.debug("Listando ventas de sucursal {} desde {} hasta {}",
                                idSucursal, fechaInicio, fechaFin);

                return ventaRegistroRepository.findByIdSucursalAndFechaBetween(
                                idSucursal, fechaInicio, fechaFin, pageable)
                                .map(this::convertirADto);
        }

        @Override
        @Transactional(readOnly = true)
        public List<VentaDetalleDto> obtenerDetalles(Long idVenta) {
                log.debug("Obteniendo detalles de venta ID: {}", idVenta);

                List<VentaDetalle> detalles = ventaDetalleRepository.findByIdVenta(idVenta);
                return detalles.stream()
                                .map(this::convertirDetalleADto)
                                .collect(Collectors.toList());
        }

        @Override
        public void anularVenta(Long idVenta, String motivo) {
                log.info("Anulando venta ID: {} - Motivo: {}", idVenta, motivo);

                VentaRegistro venta = ventaRegistroRepository.findByIdVenta(idVenta)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Venta no encontrada con ID: " + idVenta));

                if (Integer.valueOf(10).equals(venta.getEstado())) {
                        throw new IllegalStateException("La venta ya está anulada");
                }

                venta.setEstado(10);
                venta.setDetalle(motivo);
                ventaRegistroRepository.save(venta);

                try {
                        // Migrado a Caja Chica: Buscamos el movimiento por referencia
                        log.info("Anulación de venta en Caja Chica aún no implementada detalladamente por referencia, pero se hará via trigger o búsqueda manual si es necesario.");
                } catch (Exception e) {
                        log.error("Error anulando movimiento financiero de venta {}", idVenta, e);
                }

                log.info("Venta anulada exitosamente - ID: {}", idVenta);
        }

        private VentaRegistroDto convertirADto(VentaRegistro entity) {
                List<VentaDetalleDto> detallesDto = entity.getDetalles().stream()
                                .map(this::convertirDetalleADto)
                                .collect(Collectors.toList());

                return VentaRegistroDto.builder()
                                .idVenta(entity.getIdVenta())
                                .idSucursal(entity.getIdSucursal())
                                .idCtacte(entity.getIdCtacte())
                                .idPersonal(entity.getIdPersonal())
                                .idAlmacen(entity.getIdAlmacen())
                                .fecha(entity.getFecha())
                                .tipoDoc(entity.getTipoDoc())
                                .serie(entity.getSerie())
                                .numdoc(entity.getNumdoc())
                                .valorAfecto(entity.getValorAfecto())
                                .valorInaf(entity.getValorInaf())
                                .valorVenta(entity.getValorVenta())
                                .descuento(entity.getDescuento())
                                .descuentoEsp(entity.getDescuentoEsp())
                                .cobertura(entity.getCobertura())
                                .baseImp(entity.getBaseImp())
                                .igv(entity.getIgv())
                                .total(entity.getTotal())
                                .copago(entity.getCopago())
                                .estado(entity.getEstado())
                                .nombrePac(entity.getNombrePac())
                                .ruc(entity.getRuc())
                                .razon(entity.getRazon())
                                .metodoPago(determinarMetodoPago(entity))
                                .refMetodoPago(entity.getRefMetodoPago())
                                .tipoAtencion(entity.getTipoAtencion())
                                .importeSoles(entity.getImporteSoles())
                                .importeTarjeta(entity.getImporteTarjeta())
                                .importeDolares(entity.getImporteDolares())
                                .detalles(detallesDto)
                                .createdAt(entity.getCreatedAt())
                                .updatedAt(entity.getUpdatedAt())
                                .build();
        }

        private VentaDetalleDto convertirDetalleADto(VentaDetalle entity) {
                return VentaDetalleDto.builder()
                                .idMovart(entity.getIdMovart())
                                .idVenta(entity.getVentaRegistro() != null ? entity.getVentaRegistro().getIdVenta()
                                                : null)
                                .idArticulo(entity.getIdArticulo())
                                .glosa(entity.getGlosa())
                                .cantidad(entity.getCantidad())
                                .valorAf(entity.getValorAf())
                                .valorInaf(entity.getValorInaf())
                                .valorVenta(entity.getValorVenta())
                                .descuento(entity.getDescuento())
                                .descuentoEsp(entity.getDescuentoEsp())
                                .cobertura(entity.getCobertura())
                                .baseImp(entity.getBaseImp())
                                .igv(entity.getIgv())
                                .total(entity.getTotal())
                                .precioUnitario(entity.getPrecioUnitario())
                                .porcIgv(entity.getPorcIgv())
                                .porcDsc(entity.getPorcDsc())
                                .porcCob(entity.getPorcCob())
                                .fecha(entity.getFecha())
                                .estado(entity.getEstado())
                                .tipoDesc(entity.getTipoDesc())
                                .build();
        }

        private void registrarMovimiento(VentaRegistro venta, String metodo, BigDecimal monto, String ref) {
                String nombreComprobante = "TICKET";
                if ("F".equalsIgnoreCase(venta.getTipoDoc()) || "FACTURA".equalsIgnoreCase(venta.getTipoDoc()))
                        nombreComprobante = "FACTURA";
                else if ("B".equalsIgnoreCase(venta.getTipoDoc()) || "BOLETA".equalsIgnoreCase(venta.getTipoDoc()))
                        nombreComprobante = "BOLETA";

                String concepto = nombreComprobante + " DE VENTA - " + venta.getSerie() + "-" + venta.getNumdoc() + " (" + metodo + ")";
                if (ref != null && !ref.isEmpty()) {
                        concepto += " Ref: " + ref;
                }

                String usuario = venta.getIdPersonal() != null ? String.valueOf(venta.getIdPersonal()) : "SISTEMA";
                
                // Buscamos la caja abierta del usuario
                var cajaOpt = cajaChicaService.obtenerCajaAbiertaEntidad(usuario);
                
                if (cajaOpt.isPresent()) {
                    cajaChicaService.registrarMovimientoInterno(
                        cajaOpt.get(),
                        com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento.TipoMovimiento.INGRESO,
                        monto,
                        concepto,
                        venta.getSerie() + "-" + venta.getNumdoc(),
                        metodo,
                        usuario
                    );
                    log.info("✅ Movimiento de venta registrado en CAJA CHICA: {}", cajaOpt.get().getNombre());
                } else {
                    log.warn("⚠️ No se pudo registrar el movimiento de venta: El usuario {} no tiene una CAJA CHICA abierta.", usuario);
                    throw new BadRequestException("No tienes una caja abierta. Debes abrir caja en Ingresos y Gastos antes de cobrar.");
                }
        }

        @Override
        @Transactional(readOnly = true)
        public LabOrdenDto obtenerOrden(Long idOrden) {
                log.debug("Obteniendo orden ID: {}", idOrden);
                LabOrden orden = labOrdenRepository.findById(idOrden)
                                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + idOrden));
                return convertirOrdenADto(orden);
        }

        @Override
        @Transactional(readOnly = true)
        public List<LabOrdenDto> buscarOrdenesPendientes(Long idPaciente) {

                log.debug("Buscando órdenes pendientes. Paciente: {}", idPaciente);
                List<LabOrden> ordenes;
                if (idPaciente != null) {
                        ordenes = labOrdenRepository.findOrdenesPendientesPorPaciente(idPaciente);
                } else {
                        ordenes = labOrdenRepository.findOrdenesPendientes();
                }

                return ordenes.stream()
                                .map(this::convertirOrdenADto)
                                .collect(Collectors.toList());
        }

        @Override
        @Transactional
        public VentaRegistroDto pagarOrden(Long idOrden, PagarOrdenRequest request) {
                log.info("Procesando pago de orden ID: {}", idOrden);

                LabOrden orden = labOrdenRepository.findById(idOrden)
                                .orElseThrow(() -> new ResourceNotFoundException("Orden no encontrada: " + idOrden));

                if (!"PENDIENTE".equalsIgnoreCase(orden.getEstado())) {
                        throw new IllegalStateException(
                                        "La orden no está en estado PENDIENTE. Estado actual: " + orden.getEstado());
                }

                // 1. Construir VentaRequest a partir de la orden
                VentaRequest ventaRequest = VentaRequest.builder()
                                .idOrden(orden.getIdOrden())
                                .idSucursal(request.getIdSucursal())
                                .idCtacte(orden.getIdPaciente())
                                .idPersonal(request.getIdPersonal())
                                .idAlmacen(1L) // Default warehouse
                                .fecha(LocalDate.now())
                                .tipoDoc(request.getTipoDoc())
                                .serie(request.getSerie())
                                .numdoc(request.getNumdoc())
                                .metodoPago(request.getMetodoPago())
                                .refMetodoPago(request.getRefMetodoPago())
                                .tipoAtencion(request.getTipoAtencion())
                                .montoEfectivo(request.getMontoEfectivo())
                                .montoTarjeta(request.getMontoTarjeta())
                                .montoTransferencia(request.getMontoTransferencia())
                                .refTarjeta(request.getRefTarjeta())
                                .refTransferencia(request.getRefTransferencia())
                                .vuelto(request.getVuelto())
                                .nroDni(request.getNroDni())
                                .tipoDni(request.getTipoDni())
                                .ruc(request.getRuc())
                                .razon(request.getRazon())
                                .direcRuc(request.getDirecRuc())
                                .aplicarDescuentoAutomatico(true)
                                .items(new ArrayList<>())
                                .idAutorizacionDescuento(request.getIdAutorizacionDescuento())
                                .build();

                // Si se enviaron items específicos, usarlos. De lo contrario, usar todos los de
                // la orden.
                if (request.getItems() != null && !request.getItems().isEmpty()) {
                        ventaRequest.setItems(request.getItems());
                } else {
                        for (LabOrdenDetalle det : orden.getDetalles()) {
                                ventaRequest.getItems().add(VentaRequest.VentaItemRequest.builder()
                                                .idArticulo(det.getIdExamen())
                                                .tipoArticulo("SERVICIO")
                                                .glosa(det.getNombre())
                                                .cantidad(BigDecimal.ONE)
                                                .precioUnitario(det.getPrecio())
                                                .build());
                        }
                }

                // 2. Crear la venta
                VentaRegistroDto ventaDto = crearVenta(ventaRequest);

                // 3. Marcar orden como PAGADA
                orden.setEstado("PAGADA");
                labOrdenRepository.save(orden);

                return ventaDto;
        }

        private LabOrdenDto convertirOrdenADto(LabOrden entity) {
                return LabOrdenDto.builder()
                                .idOrden(entity.getIdOrden())
                                .idPaciente(entity.getIdPaciente())
                                .numero(entity.getNumero())
                                .fecha(entity.getFecha())
                                .estado(entity.getEstado())
                                .totalEstimado(entity.getTotalEstimado())
                                .pacienteNombre(entity.getPacienteNombre())
                                .pacienteDoc(entity.getPacienteDoc())
                                .medicoNombre(entity.getMedicoNombre())
                                .origen(entity.getOrigen())
                                .prioridad(entity.getPrioridad())
                                .detalles(entity.getDetalles().stream()
                                                .map(d -> LabOrdenDto.LabOrdenDetalleDto.builder()
                                                                .idOrdenDet(d.getIdOrdenDet())
                                                                .idExamen(d.getIdExamen())
                                                                .codigo(d.getCodigo())
                                                                .nombre(d.getNombre())
                                                                .precio(d.getPrecio())
                                                                .estado(d.getEstado())
                                                                .build())
                                                .collect(Collectors.toList()))
                                .build();
        }
        private String determinarMetodoPago(VentaRegistro entity) {
                // If it was stored explicitly
                if (entity.getMetodoPago() != null) {
                        return String.valueOf(entity.getMetodoPago());
                }
                
                // Otherwise deduce from amounts (this matches how cobrar-servicio frontend works)
                BigDecimal efectivo = entity.getImporteSoles() != null ? entity.getImporteSoles() : BigDecimal.ZERO;
                BigDecimal tarjeta = entity.getImporteTarjeta() != null ? entity.getImporteTarjeta() : BigDecimal.ZERO;
                BigDecimal transferencia = entity.getImporteDolares() != null ? entity.getImporteDolares() : BigDecimal.ZERO;
                
                int count = 0;
                if (efectivo.compareTo(BigDecimal.ZERO) > 0) count++;
                if (tarjeta.compareTo(BigDecimal.ZERO) > 0) count++;
                if (transferencia.compareTo(BigDecimal.ZERO) > 0) count++;
                
                if (count > 1) return "MIXTO";
                if (efectivo.compareTo(BigDecimal.ZERO) > 0) return "EFECTIVO";
                if (tarjeta.compareTo(BigDecimal.ZERO) > 0) return "YAPE_PLIN";
                if (transferencia.compareTo(BigDecimal.ZERO) > 0) return "TRANSFERENCIA";
                
                return "EFECTIVO"; // Default
        }
}
