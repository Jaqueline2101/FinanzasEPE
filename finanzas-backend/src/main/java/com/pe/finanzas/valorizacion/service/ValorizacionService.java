package com.pe.finanzas.valorizacion.service;

import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.valorizacion.dto.ValorizacionDTO;
import com.pe.finanzas.valorizacion.dto.ValorizacionResponseDTO;
import com.pe.finanzas.valorizacion.dto.ValorizacionResumenDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ValorizacionService {

    private final ProductoRepository productoRepository;

    public ValorizacionResponseDTO getReporte(Long idSucursal, String search, String categoria, String estadoStock, Pageable pageable) {
        // 1. Obtener productos (Si no hay sucursal, jalar de todos como en la pantalla de productos)
        List<Producto> allProductos;
        if (idSucursal != null && idSucursal > 0) {
            allProductos = productoRepository.findByIdSucursal(idSucursal);
        } else {
            allProductos = productoRepository.findAll();
        }

        // 2. Calcular Resumen
        ValorizacionResumenDTO resumen = calcularResumen(allProductos);

        // 3. Filtrar para el detalle
        List<Producto> filteredList = allProductos.stream()
                .filter(p -> matchesSearch(p, search))
                .filter(p -> matchesCategoria(p, categoria))
                .filter(p -> matchesEstadoStock(p, estadoStock))
                .collect(Collectors.toList());

        // 4. Mapear a DTO y Paginación manual (para este ejemplo, en producción usar Query dinámico)
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), filteredList.size());
        
        List<ValorizacionDTO> detailList = new ArrayList<>();
        if (start < filteredList.size()) {
            detailList = filteredList.subList(start, end).stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
        }

        Page<ValorizacionDTO> detailPage = new PageImpl<>(detailList, pageable, filteredList.size());

        return ValorizacionResponseDTO.builder()
                .resumen(resumen)
                .detalle(detailPage)
                .build();
    }

    private ValorizacionResumenDTO calcularResumen(List<Producto> productos) {
        BigDecimal valorTotal = BigDecimal.ZERO;
        long sinStock = 0;
        long stockBajo = 0;
        long proximosVencer = 0;
        LocalDate hoy = LocalDate.now();
        LocalDate limiteVencimiento = hoy.plusDays(90);

        Map<String, BigDecimal> porCategoria = new HashMap<>();
        List<ValorizacionDTO> allDtos = new ArrayList<>();

        for (Producto p : productos) {
            BigDecimal stock = p.getStockReal() != null ? p.getStockReal() : BigDecimal.ZERO;
            BigDecimal costo = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;
            BigDecimal valorProd = stock.multiply(costo);
            
            valorTotal = valorTotal.add(valorProd);

            if (stock.compareTo(BigDecimal.ZERO) <= 0) {
                sinStock++;
            } else if (p.getStockMinimo() != null && stock.compareTo(p.getStockMinimo()) <= 0) {
                stockBajo++;
            }

            if (p.getFechaVencimiento() != null && p.getFechaVencimiento().isBefore(limiteVencimiento) && p.getFechaVencimiento().isAfter(hoy)) {
                proximosVencer++;
            }

            // Agregación por categoría
            String cat = p.getCategoria() != null ? p.getCategoria() : "SIN CATEGORIA";
            porCategoria.put(cat, porCategoria.getOrDefault(cat, BigDecimal.ZERO).add(valorProd));
            
            allDtos.add(mapToDTO(p));
        }

        // Top 5 productos valorizados
        Map<String, BigDecimal> topProductos = allDtos.stream()
                .sorted(Comparator.comparing(ValorizacionDTO::getValorTotal).reversed())
                .limit(5)
                .collect(Collectors.toMap(
                        ValorizacionDTO::getNombre,
                        ValorizacionDTO::getValorTotal,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));

        return ValorizacionResumenDTO.builder()
                .totalProductos((long) productos.size())
                .valorTotalInventario(valorTotal)
                .productosSinStock(sinStock)
                .productosStockBajo(stockBajo)
                .productosProximosVencer(proximosVencer)
                .valorizacionPorCategoria(porCategoria)
                .topProductosValorizados(topProductos)
                .build();
    }

    private ValorizacionDTO mapToDTO(Producto p) {
        BigDecimal stock = p.getStockReal() != null ? p.getStockReal() : BigDecimal.ZERO;
        BigDecimal costo = p.getPrecioCompra() != null ? p.getPrecioCompra() : BigDecimal.ZERO;
        BigDecimal venta = (p.getCatalogo() != null && p.getCatalogo().getPrecioVentaUnitario() != null) 
                ? p.getCatalogo().getPrecioVentaUnitario() : BigDecimal.ZERO;
        
        String estado = "VERDE"; // NORMAL
        if (stock.compareTo(BigDecimal.ZERO) <= 0) {
            estado = "ROJO"; // SIN STOCK
        } else if (p.getStockMinimo() != null && stock.compareTo(p.getStockMinimo()) <= 0) {
            estado = "ROJO"; // STOCK BAJO
        }

        LocalDate hoy = LocalDate.now();
        if (p.getFechaVencimiento() != null) {
            if (p.getFechaVencimiento().isBefore(hoy)) {
                estado = "ROJO"; // VENCIDO
            } else if (p.getFechaVencimiento().isBefore(hoy.plusDays(90))) {
                if (!estado.equals("ROJO")) estado = "AMARILLO"; // PROXIMO A VENCER
            }
        }

        return ValorizacionDTO.builder()
                .id(p.getIdProducto())
                .codigo(p.getCodigo())
                .nombre(p.getNombreProducto())
                .categoria(p.getCategoria())
                .lote(p.getRegistroInvimaLote())
                .stock(stock)
                .stockMinimo(p.getStockMinimo())
                .costoUnitario(costo)
                .precioVenta(venta)
                .valorTotal(stock.multiply(costo))
                .fechaVencimiento(p.getFechaVencimiento())
                .estado(estado)
                .build();
    }

    private boolean matchesSearch(Producto p, String search) {
        if (search == null || search.isEmpty()) return true;
        String s = search.toLowerCase();
        return (p.getCodigo() != null && p.getCodigo().toLowerCase().contains(s)) ||
               (p.getNombreProducto() != null && p.getNombreProducto().toLowerCase().contains(s));
    }

    private boolean matchesCategoria(Producto p, String categoria) {
        if (categoria == null || categoria.isEmpty() || categoria.equals("TODOS")) return true;
        return p.getCategoria() != null && p.getCategoria().equalsIgnoreCase(categoria);
    }

    private boolean matchesEstadoStock(Producto p, String estado) {
        if (estado == null || estado.isEmpty() || estado.equals("TODOS")) return true;
        BigDecimal stock = p.getStockReal() != null ? p.getStockReal() : BigDecimal.ZERO;
        if (estado.equals("SIN_STOCK")) return stock.compareTo(BigDecimal.ZERO) <= 0;
        if (estado.equals("STOCK_BAJO")) return p.getStockMinimo() != null && stock.compareTo(p.getStockMinimo()) <= 0 && stock.compareTo(BigDecimal.ZERO) > 0;
        if (estado.equals("NORMAL")) return p.getStockMinimo() == null || stock.compareTo(p.getStockMinimo()) > 0;
        return true;
    }
}
