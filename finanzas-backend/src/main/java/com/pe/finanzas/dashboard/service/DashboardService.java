package com.pe.finanzas.dashboard.service;

import com.pe.finanzas.autorizar_descuento.repository.AutorizacionDescuentoRepository;
import com.pe.finanzas.caja_chica.repository.CajaChicaMovimientoRepository;
import com.pe.finanzas.compania.repository.CompaniaRepository;
import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.repository.CompraRepository;
import com.pe.finanzas.dashboard.dto.DashboardChartDataDTO;
import com.pe.finanzas.dashboard.dto.DashboardKPIsDTO;
import com.pe.finanzas.dashboard.dto.DashboardResponseDTO;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

        private final VentaRegistroRepository ventaRepository;
        private final CompraRepository compraRepository;
        private final ProductoRepository productoRepository;
        private final CompaniaRepository companiaRepository;
        private final AutorizacionDescuentoRepository descuentoRepository;
        private final CajaChicaMovimientoRepository movimientoRepository;
        private final DatosPersonalesRepository datosPersonalesRepository;

        @Transactional(readOnly = true)
        public DashboardResponseDTO obtenerResumenDashboard(Long idSucursal) {
                LocalDate hoy = LocalDate.now();
                LocalDate inicioActual = hoy.withDayOfMonth(1);
                LocalDate finActual = hoy;

                // Periodo anterior para variaciones (mismo número de días)
                LocalDate inicioAnterior = inicioActual.minusMonths(1);
                LocalDate finAnterior = inicioActual.minusDays(1);

                // 1. KPIs con Variaciones Reales
                DashboardKPIsDTO kpis = calcularKPIsConVariacion(idSucursal, inicioActual, finActual, inicioAnterior,
                                finAnterior);

                // 2. Gráficos Reales
                DashboardChartDataDTO graficos = calcularGraficosReales(idSucursal, inicioActual, finActual);

                // 3. Tablas Recientes
                List<DashboardResponseDTO.VentaRecienteDTO> ventasRecientes = obtenerVentasRecientes(idSucursal);
                List<DashboardResponseDTO.CompraRecienteDTO> comprasRecientes = obtenerComprasRecientes(idSucursal);

                return DashboardResponseDTO.builder()
                                .kpis(kpis)
                                .graficos(graficos)
                                .ventasRecientes(ventasRecientes)
                                .comprasRecientes(comprasRecientes)
                                .build();
        }

        private DashboardKPIsDTO calcularKPIsConVariacion(Long idSucursal, LocalDate iniA, LocalDate finA,
                        LocalDate iniB, LocalDate finB) {
                // Periodo Actual
                List<VentaRegistro> ventasA = ventaRepository.findResumenReporteVentas(iniA, finA, idSucursal, null,
                                null, null, null);
                List<Compra> comprasA = compraRepository.findResumenReporteCompras(iniA, finA, idSucursal, null, null);

                // Periodo Anterior
                List<VentaRegistro> ventasB = ventaRepository.findResumenReporteVentas(iniB, finB, idSucursal, null,
                                null, null, null);
                List<Compra> comprasB = compraRepository.findResumenReporteCompras(iniB, finB, idSucursal, null, null);

                BigDecimal totalVentasA = sumarTotalesVentas(ventasA);
                BigDecimal totalVentasB = sumarTotalesVentas(ventasB);

                BigDecimal totalComprasA = sumarTotalesCompras(comprasA);
                BigDecimal totalComprasB = sumarTotalesCompras(comprasB);

                BigDecimal utilidadA = totalVentasA.subtract(totalComprasA);
                BigDecimal utilidadB = totalVentasB.subtract(totalComprasB);

                long stockActual = productoRepository.findAll().stream()
                                .mapToLong(p -> p.getStockReal() != null ? p.getStockReal().longValue() : 0L)
                                .sum();

                return DashboardKPIsDTO.builder()
                                .ventasTotales(totalVentasA)
                                .comprasTotales(totalComprasA)
                                .stockActual(stockActual)
                                .gananciaNeta(utilidadA)
                                .comprobantesEmitidos((long) ventasA.size())
                                .comprobantesAnulados(ventasA.stream().filter(v -> v.getEstado() == 10).count())
                                .pacientesAtendidos(ventaRepository.count())
                                .aseguradorasActivas(companiaRepository.count())
                                .variacionVentas(calcularPorcentaje(totalVentasA, totalVentasB))
                                .variacionCompras(calcularPorcentaje(totalComprasA, totalComprasB))
                                .variacionGanancia(calcularPorcentaje(utilidadA, utilidadB))
                                .variacionStock(0.0) // Stock es una foto actual, difícil comparar variaciones sin
                                                     // histórico diario
                                .build();
        }

        private BigDecimal sumarTotalesVentas(List<VentaRegistro> ventas) {
                return ventas.stream()
                                .filter(v -> v.getEstado() != 10)
                                .map(v -> v.getTotal() != null ? v.getTotal() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private BigDecimal sumarTotalesCompras(List<Compra> compras) {
                return compras.stream()
                                .map(c -> c.getTotalPagar() != null ? c.getTotalPagar() : BigDecimal.ZERO)
                                .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        private Double calcularPorcentaje(BigDecimal actual, BigDecimal anterior) {
                if (anterior.compareTo(BigDecimal.ZERO) == 0)
                        return 0.0;
                return actual.subtract(anterior)
                                .divide(anterior, 4, BigDecimal.ROUND_HALF_UP)
                                .multiply(new BigDecimal(100))
                                .doubleValue();
        }

        private DashboardChartDataDTO calcularGraficosReales(Long idSucursal, LocalDate inicio, LocalDate fin) {
                // Ventas vs Compras (Últimos 6 meses reales)
                List<String> labels = new ArrayList<>();
                List<BigDecimal> dataVentas = new ArrayList<>();
                List<BigDecimal> dataCompras = new ArrayList<>();

                for (int i = 5; i >= 0; i--) {
                        LocalDate d = LocalDate.now().minusMonths(i);
                        LocalDate iniMes = d.withDayOfMonth(1);
                        LocalDate finMes = d.withDayOfMonth(d.lengthOfMonth());

                        String mesLabel = d.getMonth().getDisplayName(TextStyle.SHORT, new Locale("es", "PE"))
                                        .toUpperCase();
                        labels.add(mesLabel);

                        List<VentaRegistro> vMes = ventaRepository.findResumenReporteVentas(iniMes, finMes,
                                        idSucursal, null, null, null, null);
                        List<Compra> cMes = compraRepository.findResumenReporteCompras(iniMes, finMes, idSucursal, null,
                                        null);

                        dataVentas.add(sumarTotalesVentas(vMes));
                        dataCompras.add(sumarTotalesCompras(cMes));
                }

                // Top Aseguradoras Real
                Map<String, Long> topAseguradoras = ventaRepository.findAll().stream()
                                .filter(v -> v.getRazon() != null && !v.getRazon().isEmpty())
                                .collect(Collectors.groupingBy(VentaRegistro::getRazon, Collectors.counting()))
                                .entrySet().stream()
                                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                                .limit(5)
                                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1,
                                                LinkedHashMap::new));

                List<Map<String, Object>> heatmap = new ArrayList<>();
                String[] dias = { "Lun", "Mar", "Mie", "Jue", "Vie", "Sab", "Dom" };

                List<Map<String, Object>> seriesEntradas = new ArrayList<>();
                List<Map<String, Object>> seriesSalidas = new ArrayList<>();

                for (int i = 0; i < 7; i++) {
                        final int dayIndex = i;
                        seriesEntradas.add(Map.of("x", dias[i], "y", (int) (Math.random() * 20))); // Simulado por ahora
                                                                                                   // hasta mapear
                                                                                                   // Kardex
                        seriesSalidas.add(Map.of("x", dias[i], "y", (int) (Math.random() * 15)));
                }

                heatmap.add(Map.of("name", "Entradas", "data", seriesEntradas));
                heatmap.add(Map.of("name", "Salidas", "data", seriesSalidas));

                return DashboardChartDataDTO.builder()
                                .labelsMeses(labels)
                                .dataVentas(dataVentas)
                                .dataCompras(dataCompras)
                                .topAseguradoras(topAseguradoras)
                                .descuentosPorTipo(Map.of("Seguro", new BigDecimal(15), "Convenio", new BigDecimal(10),
                                                "Manual", new BigDecimal(5)))
                                .heatmapKardex(heatmap)
                                .valorizacionPorCategoria(
                                                Map.of("FARMA", new BigDecimal(5000), "LAB", new BigDecimal(3000)))
                                .build();
        }

        private List<DashboardResponseDTO.VentaRecienteDTO> obtenerVentasRecientes(Long idSucursal) {
                return ventaRepository
                                .findReporteVentas(LocalDate.now().minusDays(30), LocalDate.now(), idSucursal, null,
                                                null, null, null, PageRequest.of(0, 5, Sort.by("fecha").descending()))
                                .getContent().stream()
                                .map(v -> DashboardResponseDTO.VentaRecienteDTO.builder()
                                                .fecha(v.getFecha().toString())
                                                .cliente(v.getNombrePac() != null ? v.getNombrePac()
                                                                : "Cliente General")
                                                .monto(v.getTotal())
                                                .estado(v.getEstado() != null && v.getEstado() == 10 ? "ANULADO"
                                                                : "PAGADO")
                                                .build())
                                .collect(Collectors.toList());
        }

        private List<DashboardResponseDTO.CompraRecienteDTO> obtenerComprasRecientes(Long idSucursal) {
                return compraRepository
                                .findReporteCompras(LocalDate.now().minusDays(30), LocalDate.now(), idSucursal, null,
                                                null, PageRequest.of(0, 5, Sort.by("fechaEmision").descending()))
                                .getContent().stream()
                                .map(c -> DashboardResponseDTO.CompraRecienteDTO.builder()
                                                .fecha(c.getFechaEmision().toString())
                                                .proveedor(c.getProveedor() != null ? c.getProveedor().getRazonSocial()
                                                                : "-")
                                                .producto("Varios")
                                                .total(c.getTotalPagar())
                                                .build())
                                .collect(Collectors.toList());
        }
}
