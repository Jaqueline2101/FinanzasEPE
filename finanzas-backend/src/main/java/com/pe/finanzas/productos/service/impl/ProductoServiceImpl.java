package com.pe.finanzas.productos.service.impl;

import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.productos.service.ProductoService;
import com.pe.finanzas.kardex.entity.Kardex;
import com.pe.finanzas.kardex.service.KardexService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.pe.finanzas.users.entity.DatosPersonales;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final com.pe.finanzas.catalogo.repository.CatalogoRepository catalogoRepository;
    private final com.pe.finanzas.niveles.repository.NivelRepository nivelRepository;
    private final com.pe.finanzas.catalogo.repository.TipoAtencionRepository tipoAtencionRepository;
    private final com.pe.finanzas.catalogo.repository.TipoTemperaturaRepository tipoTemperaturaRepository;
    private final KardexService kardexService;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findAll() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> findBySucursal(Long idSucursal) {
        return productoRepository.findByIdSucursal(idSucursal);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Producto> findById(Long id) {
        return productoRepository.findById(id);
    }

    @Override
    @Transactional
    public Producto save(Producto producto) {
        // Lógica para crear o actualizar Catálogo "al vuelo"
        if (producto.getIdCatalogo() == null && producto.getNombreProducto() != null
                && !producto.getNombreProducto().isEmpty()) {

            // 1. Obtener Nivel Raíz (Fallback robusto: ID 1 -> Cualquier raíz disponible)
            com.pe.finanzas.niveles.entity.Nivel nivelRoot = nivelRepository.findById(1L)
                    .orElseGet(() -> {
                        List<com.pe.finanzas.niveles.entity.Nivel> raices = nivelRepository.findByNivelPadreIsNullOrderByOrden();
                        if (raices.isEmpty()) {
                            log.warn("⚠️ No se encontró ningún nivel raíz en el sistema. Intentando obtener el primer nivel disponible.");
                            List<com.pe.finanzas.niveles.entity.Nivel> todos = nivelRepository.findAll();
                            return todos.isEmpty() ? null : todos.get(0);
                        }
                        return raices.get(0);
                    });

            String manualCode = producto.getCodigo();
            String finalCodigo = (manualCode != null && !manualCode.trim().isEmpty()) ? manualCode.trim() : null;

            // 2. BUSCAR CATÁLOGO EXISTENTE PARA REUTILIZAR Y SINCRONIZAR
            Optional<com.pe.finanzas.catalogo.entity.Catalogo> existingCat = Optional.empty();
            if (producto.getCodigoBarra() != null && !producto.getCodigoBarra().trim().isEmpty()) {
                existingCat = catalogoRepository.findByCodigoBarra(producto.getCodigoBarra().trim());
            }
            if (existingCat.isEmpty() && finalCodigo != null) {
                existingCat = catalogoRepository.findByCodigo(finalCodigo);
            }

            final com.pe.finanzas.catalogo.entity.Catalogo catToSave;
            if (existingCat.isPresent()) {
                // REUTILIZAR Y ACTUALIZAR: Evita "datos cruzados" sincronizando el nombre/modelo/etc
                catToSave = existingCat.get();
                if (producto.getNombreProducto() != null) {
                    catToSave.setNombreServicio(producto.getNombreProducto());
                    catToSave.setNombre(producto.getNombreProducto());
                }
                if (producto.getModelo() != null) catToSave.setModelo(producto.getModelo());
                if (producto.getCategoria() != null) catToSave.setTipoMedicamento(producto.getCategoria());
                if (producto.getUnidadMedida() != null) catToSave.setPresentacion(producto.getUnidadMedida());
                if (producto.getRegSanitario() != null) catToSave.setRegSanitario(producto.getRegSanitario());
                if (producto.getMoneda() != null) catToSave.setMoneda(producto.getMoneda());
                
                // --- CATEGORIZACIÓN CRÍTICA ---
                catToSave.setTipoCatalogo("producto");
                catToSave.setNombreCatalogo(producto.getNombreProducto());
                
                // --- SINCRONIZACIÓN CRÍTICA ---
                // Sincronizar Código Maestro si se envió manual
                if (finalCodigo != null) catToSave.setCodigo(finalCodigo);
                // Sincronizar Código de Barras
                if (producto.getCodigoBarra() != null) catToSave.setCodigoBarra(producto.getCodigoBarra().trim());
                // Sincronizar TIPO (Evita modales cruzados al editar)
                catToSave.setTipo("SERVICIO".equals(producto.getTipo()) 
                        ? com.pe.finanzas.catalogo.entity.Catalogo.Tipo.SERVICIO 
                        : com.pe.finanzas.catalogo.entity.Catalogo.Tipo.PRODUCTO);

                // Actualizar Detalle (Marca, Ubicación, Almacenamiento)
                String marca = producto.getMarcaLaboratorio() != null ? producto.getMarcaLaboratorio() : "GENERICO";
                String ubic = producto.getUbicacion() != null ? " | Ubicación: " + producto.getUbicacion() : "";
                String alm = producto.getAlmacenamiento() != null ? " | Almacenamiento: " + producto.getAlmacenamiento() : "";
                
                catToSave.setDetalle("Marca: " + marca + ubic + alm);

            } else {
                // CREAR NUEVO
                if (finalCodigo == null) {
                    String maxCodigo = catalogoRepository.findMaxCodigo();
                    int nextCodigo = (maxCodigo != null && maxCodigo.matches("^[0-9]{7}$")) ? Integer.parseInt(maxCodigo) + 1 : 1000001;
                    finalCodigo = String.format("%07d", nextCodigo);
                }

                BigDecimal pCompra = producto.getPrecioCompra() != null ? producto.getPrecioCompra() : java.math.BigDecimal.ZERO;
                
                catToSave = com.pe.finanzas.catalogo.entity.Catalogo.builder()
                        .codigo(finalCodigo)
                        .nombreServicio(producto.getNombreProducto())
                        .nombre(producto.getNombreProducto())
                        .modelo(producto.getModelo())
                        .codigoBarra(producto.getCodigoBarra() != null ? producto.getCodigoBarra() : finalCodigo)
                        .moneda(producto.getMoneda() != null ? producto.getMoneda() : "PEN")
                        .nivel(nivelRoot)
                        .tipo("SERVICIO".equals(producto.getTipo()) ? com.pe.finanzas.catalogo.entity.Catalogo.Tipo.SERVICIO : com.pe.finanzas.catalogo.entity.Catalogo.Tipo.PRODUCTO)
                        .tipoAtencion(tipoAtencionRepository.findById(1L).orElse(null))
                        .tipoMedicamento(producto.getCategoria())
                        .presentacion(producto.getUnidadMedida())
                        .unidad(java.math.BigDecimal.ONE)
                        .honorariosMedicos(false)
                        .estado(1)
                        .manejaUnidad(producto.getManejaUnidad() != null ? producto.getManejaUnidad() : true)
                        .detalle("Marca: " + (producto.getMarcaLaboratorio() != null ? producto.getMarcaLaboratorio() : "GENERICO") + 
                                 (producto.getUbicacion() != null ? " | Ubicación: " + producto.getUbicacion() : "") +
                                 (producto.getAlmacenamiento() != null ? " | Almacenamiento: " + producto.getAlmacenamiento() : ""))
                        .precioCompra(pCompra)
                        .precioSinIgv(pCompra)
                        .precioConIgv(pCompra)
                        .precioVentaUnitario(java.math.BigDecimal.ZERO)
                        .precioVentaBlister(java.math.BigDecimal.ZERO)
                        .precioVentaCaja(java.math.BigDecimal.ZERO)
                        .tipoPrecio(com.pe.finanzas.catalogo.entity.Catalogo.TipoPrecio.PRECIO_FIJO)
                        .tipoCatalogo("producto")
                        .nombreCatalogo(producto.getNombreProducto())
                        .build();
            }

            com.pe.finanzas.catalogo.entity.Catalogo savedCat = catalogoRepository.save(catToSave);
            producto.setIdCatalogo(savedCat.getId());
        }

        if (producto.getIdCatalogo() == null) {
            throw new RuntimeException("ID de catálogo no proporcionado");
        }

        log.info("🚀 Intentando guardar producto con ID Catalogo: {}", producto.getIdCatalogo());
        Producto savedProducto = productoRepository.save(producto);
        log.info("✅ Producto guardado exitosamente con ID: {}", savedProducto.getIdProducto());

        // ✅ Registrar obligatoriamente el movimiento de Kardex
        Kardex mov = Kardex.builder()
                .idAlmArticulo(savedProducto.getIdProducto())
                .idCatalogo(savedProducto.getIdCatalogo())
                .idSucursal(savedProducto.getIdSucursal() != null ? savedProducto.getIdSucursal() : 1L)
                .idUsuario(getCurrentUserId())
                .fecha(LocalDateTime.now())
                .operacion("INGRESO_COMPRA")
                .signo("+")
                .idDocumento("ALMACEN")
                .numDoc("SALDO INICIAL")
                .detalle("Carga de stock inicial de lote: " + (savedProducto.getRegistroInvimaLote() != null ? savedProducto.getRegistroInvimaLote() : "SIN LOTE"))
                .cantidad(savedProducto.getStockReal() != null ? savedProducto.getStockReal() : BigDecimal.ZERO)
                .costoTotal((savedProducto.getStockReal() != null ? savedProducto.getStockReal() : BigDecimal.ZERO)
                        .multiply(savedProducto.getPrecioCompra() != null ? savedProducto.getPrecioCompra() : BigDecimal.ZERO))
                .costoUnitario(savedProducto.getPrecioCompra() != null ? savedProducto.getPrecioCompra() : BigDecimal.ZERO)
                .fechaVenc(savedProducto.getFechaVencimiento())
                .nroLote(savedProducto.getRegistroInvimaLote())
                .origenId(savedProducto.getIdProducto().toString())
                .origenTipo("PRODUCTO_MANUAL")
                .presentacion(savedProducto.getUnidadMedida())
                .build();
        
        log.debug("📦 Preparando movimiento Kardex: Operacion={}, OrigenId={}, OrigenTipo={}", 
            mov.getOperacion(), mov.getOrigenId(), mov.getOrigenTipo());
        
        try {
            kardexService.registrarMovimiento(mov);
        } catch (Exception e) {
            log.error("Fallo crítico al registrar Kardex para nuevo producto {}: {}", savedProducto.getIdProducto(), e.getMessage());
            throw new RuntimeException("No se pudo registrar el movimiento inicial en Kardex. Transacción abortada para mantener integridad.");
        }

        return savedProducto;
    }

    @Override
    @Transactional
    public Producto update(Long id, Producto producto) {
        return productoRepository.findById(id).map(existing -> {
            // Huella Digital: Detectar cambios en el stock para el Kardex
            BigDecimal oldStock = existing.getStockReal() != null ? existing.getStockReal() : BigDecimal.ZERO;
            BigDecimal newStock = producto.getStockReal() != null ? producto.getStockReal() : BigDecimal.ZERO;
            BigDecimal difference = newStock.subtract(oldStock);

            if (difference.compareTo(BigDecimal.ZERO) != 0) {
                Kardex mov = Kardex.builder()
                    .idAlmArticulo(existing.getIdProducto())
                    .idCatalogo(existing.getIdCatalogo())
                    .idSucursal(existing.getIdSucursal() != null ? existing.getIdSucursal() : 1L)
                    .idUsuario(getCurrentUserId())
                    .fecha(LocalDateTime.now())
                    .operacion(difference.compareTo(BigDecimal.ZERO) > 0 ? "AJUSTE_ENTRADA" : "AJUSTE_SALIDA")
                    .signo(difference.compareTo(BigDecimal.ZERO) > 0 ? "+" : "-")
                    .idDocumento("ALMACEN")
                    .numDoc("AJUSTE MANUAL")
                    .detalle("Ajuste manual de stock (Huella Digital)")
                    .cantidad(difference.abs())
                    .costoUnitario(existing.getPrecioCompra() != null ? existing.getPrecioCompra() : BigDecimal.ZERO)
                    .costoTotal(difference.abs().multiply(existing.getPrecioCompra() != null ? existing.getPrecioCompra() : BigDecimal.ZERO))
                    .nroLote(existing.getRegistroInvimaLote())
                    .origenId(existing.getIdProducto().toString())
                    .origenTipo("PRODUCTO_MANUAL")
                    .presentacion(existing.getUnidadMedida())
                    .build();

                log.debug("🔄 Preparando ajuste Kardex (Update): Diferencia={}, OrigenId={}", 
                    difference, mov.getOrigenId());
                
                try {
                    kardexService.registrarMovimiento(mov);
                } catch (Exception e) {
                    log.error("Fallo crítico al registrar Ajuste en Kardex para producto {}: {}", existing.getIdProducto(), e.getMessage());
                    throw new RuntimeException("Error en integridad de Kardex al procesar ajuste de stock.");
                }
            }

            // Update Producto fields
            existing.setStockReal(producto.getStockReal());
            existing.setPrecioCompra(producto.getPrecioCompra());
            existing.setStockMinimo(producto.getStockMinimo());
            existing.setStockMaximo(producto.getStockMaximo());
            existing.setFechaVencimiento(producto.getFechaVencimiento());
            existing.setRegistroInvimaLote(producto.getRegistroInvimaLote());
            existing.setFactorCompra(producto.getFactorCompra());
            existing.setTipoAfectacionCompra(producto.getTipoAfectacionCompra());
            existing.setAlmacenamiento(producto.getAlmacenamiento());

            // Update Catalogo fields if present
            if (existing.getIdCatalogo() != null) {
                catalogoRepository.findById(existing.getIdCatalogo()).ifPresent(cat -> {
                    if (producto.getNombreProducto() != null) {
                        cat.setNombreServicio(producto.getNombreProducto());
                        cat.setNombre(producto.getNombreProducto());
                    }
                    if (producto.getModelo() != null)
                        cat.setModelo(producto.getModelo());

                    if (producto.getCodigo() != null)
                        cat.setCodigo(producto.getCodigo());

                    if (producto.getCodigoBarra() != null)
                        cat.setCodigoBarra(producto.getCodigoBarra());

                    if (producto.getMarcaLaboratorio() != null || producto.getUbicacion() != null || producto.getAlmacenamiento() != null) {
                        String marca = producto.getMarcaLaboratorio();
                        String ubic = producto.getUbicacion();
                        String alm = producto.getAlmacenamiento();

                        // Simple overwrite strategy for now to ensure data is saved
                        String newDetalle = "Marca: " + (marca != null ? marca : "GENERICO")
                                + (ubic != null ? " | Ubicación: " + ubic : "")
                                + (alm != null ? " | Almacenamiento: " + alm : "");
                        cat.setDetalle(newDetalle);
                    }
                    if (producto.getCategoria() != null)
                        cat.setTipoMedicamento(producto.getCategoria());
                    if (producto.getUnidadMedida() != null)
                        cat.setPresentacion(producto.getUnidadMedida());
                    if (producto.getRegSanitario() != null)
                        cat.setRegSanitario(producto.getRegSanitario());
                    if (producto.getMoneda() != null)
                        cat.setMoneda(producto.getMoneda());
                    
                    if (producto.getIdTipoTemperatura() != null) {
                        tipoTemperaturaRepository.findById(producto.getIdTipoTemperatura())
                            .ifPresent(cat::setTipoTemperatura);
                    } else if (producto.getIdTipoTemperatura() == null && Boolean.TRUE.equals(producto.getManejaUnidad())) {
                        // Si se envía null explícitamente (ej: "No especificado"), se limpia
                        cat.setTipoTemperatura(null);
                    }

                    // Sincronizar campos de catálogo obligatorios
                    cat.setTipoCatalogo("producto");
                    if (producto.getNombreProducto() != null) {
                        cat.setNombreCatalogo(producto.getNombreProducto());
                    }

                    catalogoRepository.save(cat);
                });
            }

            return productoRepository.save(existing);
        }).orElseThrow(() -> new RuntimeException("Producto no encontrado"));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        productoRepository.deleteById(id);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof DatosPersonales) {
            return ((DatosPersonales) authentication.getPrincipal()).getId();
        }
        return 1L; // Fallback al admin por defecto
    }
}
