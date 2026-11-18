package com.pe.finanzas.config.initializer;

import com.pe.finanzas.arqueo_caja.entity.ArqueoCaja;
import com.pe.finanzas.arqueo_caja.repository.ArqueoCajaRepository;
import com.pe.finanzas.atributos.entity.MetodoPago;
import com.pe.finanzas.atributos.repository.MetodoPagoRepository;
import com.pe.finanzas.autorizar_descuento.entity.AutorizacionDescuento;
import com.pe.finanzas.autorizar_descuento.repository.AutorizacionDescuentoRepository;
import com.pe.finanzas.caja_chica.entity.CajaChica;
import com.pe.finanzas.caja_chica.entity.CajaChicaMovimiento;
import com.pe.finanzas.caja_chica.repository.CajaChicaMovimientoRepository;
import com.pe.finanzas.caja_chica.repository.CajaChicaRepository;
import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.entity.DetalleCompra;
import com.pe.finanzas.compra.repository.CompraRepository;
import com.pe.finanzas.compra.repository.DetalleCompraRepository;
import com.pe.finanzas.kardex.entity.Kardex;
import com.pe.finanzas.kardex.repository.KardexRepository;
import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.repository.NivelRepository;
import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.repository.PagoComprobanteRepository;
import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import com.pe.finanzas.puntos.entity.Punto;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.reports.audit.entity.AuditLog;
import com.pe.finanzas.reports.audit.repository.AuditRepository;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.venta_registro.entity.SerieCorrelativo;
import com.pe.finanzas.venta_registro.entity.VentaDetalle;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.SerieCorrelativoRepository;
import com.pe.finanzas.venta_registro.repository.VentaDetalleRepository;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

@Component
@Order(20)
@Slf4j
@RequiredArgsConstructor
public class DemoDataInitializer implements CommandLineRunner {

    private final SucursalRepository sucursalRepository;
    private final PuntoRepository puntoRepository;
    private final SerieCorrelativoRepository serieCorrelativoRepository;
    private final NivelRepository nivelRepository;
    private final CatalogoRepository catalogoRepository;
    private final ProductoRepository productoRepository;
    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final VentaRegistroRepository ventaRegistroRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final AutorizacionDescuentoRepository autorizacionDescuentoRepository;
    private final PagoComprobanteRepository pagoComprobanteRepository;
    private final CajaChicaRepository cajaChicaRepository;
    private final CajaChicaMovimientoRepository cajaChicaMovimientoRepository;
    private final ArqueoCajaRepository arqueoCajaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final KardexRepository kardexRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final AuditRepository auditRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("🚀 INICIANDO VERIFICACIÓN Y POBLADO DE DATOS DE DEMOSTRACIÓN EN TODOS LOS MÓDULOS...");

        try {
            // 1. Sucursales
            Sucursal sedePrincipal = seedSucursales();

            // 2. Puntos de Venta
            Punto punto1 = seedPuntosVenta(sedePrincipal);

            // 3. Series de Correlatividad
            seedSeriesCorrelativo(sedePrincipal);

            // 4. Niveles Organizacionales
            Map<String, Nivel> niveles = seedNiveles();

            // 5. Catálogo de Servicios y Exámenes Médicos
            List<Catalogo> servicios = seedCatalogo(niveles);

            // 6. Inventario / Productos en Stock
            List<Producto> productos = seedProductos(servicios, sedePrincipal);

            // 7. Proveedores
            List<Proveedor> proveedores = seedProveedores();

            // 8. Compras y Detalle de Compras (Histórico 2026 + Noviembre 2026)
            seedCompras(proveedores, servicios, sedePrincipal);

            // 9. Ventas y Detalle de Ventas (Histórico 2026 + Noviembre 2026 para
            // Correlatividad y Reportes)
            seedVentas(servicios, sedePrincipal, punto1);

            // 10. Descuentos Autorizados
            seedDescuentos();

            // 11. Pagos de Honorarios Médicos y Nómina
            seedPagos();

            // 12. Caja Chica y Movimientos
            seedCajaChica(sedePrincipal);

            // 13. Arqueos de Caja
            seedArqueos(sedePrincipal, punto1);

            // 14. Kardex de Movimientos
            seedKardex(productos, servicios, sedePrincipal);

            // 15. Auditoría
            seedAudit();

            log.info("🎉 ¡DATOS DE DEMOSTRACIÓN CARGADOS EXITOSAMENTE EN TODOS LOS CAMPOS!");
        } catch (Exception e) {
            log.error("⚠️ Error durante el sembrado de datos de demostración: {}", e.getMessage(), e);
        }
    }

    private Sucursal seedSucursales() {
        return sucursalRepository.findAll().stream()
                .filter(s -> s.getNombreSucursal() != null && s.getNombreSucursal().toUpperCase().contains("PRINCIPAL"))
                .findFirst()
                .orElseGet(() -> {
                    Sucursal s = new Sucursal();
                    s.setNombreSucursal("SEDE PRINCIPAL");
                    s.setDireccion("Av. Arequipa 1420, Lima");
                    s.setEstado(1);
                    return sucursalRepository.save(s);
                });
    }

    private Punto seedPuntosVenta(Sucursal sucursal) {
        return puntoRepository.findById(1L).orElseGet(() -> {
            Punto p = Punto.builder()
                    .nombre("01-Caja 1 - 207")
                    .idSucursal(sucursal.getIdSucursal() != null ? sucursal.getIdSucursal().intValue() : 1)
                    .tipo("CAJA")
                    .build();
            return puntoRepository.save(p);
        });
    }

    private void seedSeriesCorrelativo(Sucursal sucursal) {
        Long idSucursal = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        crearSerieSiNoExiste(idSucursal, "B", "B001", 30L);
        crearSerieSiNoExiste(idSucursal, "F", "F001", 15L);
        crearSerieSiNoExiste(idSucursal, "T", "T001", 10L);
        crearSerieSiNoExiste(idSucursal, "NC", "NC01", 2L);
    }

    private void crearSerieSiNoExiste(Long idSucursal, String tipoDoc, String serie, Long ultimoNumero) {
        if (serieCorrelativoRepository.findByIdSucursalAndTipoDocAndSerie(idSucursal, tipoDoc, serie).isEmpty()) {
            SerieCorrelativo sc = SerieCorrelativo.builder()
                    .idSucursal(idSucursal)
                    .tipoDoc(tipoDoc)
                    .serie(serie)
                    .ultimoNumero(ultimoNumero)
                    .longitudNumero(8)
                    .build();
            serieCorrelativoRepository.save(sc);
        }
    }

    private Map<String, Nivel> seedNiveles() {
        Map<String, Nivel> mapa = new HashMap<>();
        String[] nombres = { "LABORATORIO CLÍNICO", "DIAGNÓSTICO POR IMÁGENES", "CONSULTAS MÉDICAS", "FARMACIA" };
        String[] centrosCosto = { "CC-LAB", "CC-IMG", "CC-MED", "CC-FAR" };

        for (int i = 0; i < nombres.length; i++) {
            String nom = nombres[i];
            String cc = centrosCosto[i];
            Nivel n = nivelRepository.findAll().stream()
                    .filter(item -> nom.equalsIgnoreCase(item.getNombre()))
                    .findFirst()
                    .orElseGet(() -> {
                        Nivel nuevo = new Nivel();
                        nuevo.setNombre(nom);
                        nuevo.setNumNivel(String.format("%02d", mapa.size() + 1));
                        nuevo.setCentroCosto(cc);
                        nuevo.setEstado(1);
                        nuevo.setNivelJerarquia(1);
                        return nivelRepository.save(nuevo);
                    });
            mapa.put(nom, n);
        }
        return mapa;
    }

    private List<Catalogo> seedCatalogo(Map<String, Nivel> niveles) {
        List<Catalogo> lista = catalogoRepository.findAll();
        if (!lista.isEmpty()) {
            return lista;
        }

        Nivel lab = niveles.getOrDefault("LABORATORIO CLÍNICO", niveles.values().iterator().next());
        Nivel img = niveles.getOrDefault("DIAGNÓSTICO POR IMÁGENES", lab);
        Nivel med = niveles.getOrDefault("CONSULTAS MÉDICAS", lab);
        Nivel far = niveles.getOrDefault("FARMACIA", lab);

        Object[][] itemsData = {
                { "SERV001", "HEMOGRAMA COMPLETO AUTOMATIZADO", "45.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV002", "PERFIL LIPÍDICO COMPLETO", "85.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV003", "GLUCOSA EN SANGRE BASAL", "25.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV004", "EXAMEN COMPLETO DE ORINA", "30.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV005", "ECOGRAFÍA ABDOMINAL TOTAL", "160.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV006", "RADIOGRAFÍA DE TÓRAX DIGITAL", "95.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV007", "CONSULTA MEDICINA GENERAL", "80.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV008", "CONSULTA ESPECIALIZADA CARDIOLOGÍA", "150.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV009", "CONSULTA ESPECIALIZADA PEDIATRÍA", "120.00", med, Catalogo.Tipo.SERVICIO, true },
                { "MED001", "PARACETAMOL 500 MG X 100 TAB", "15.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED002", "AMOXICILINA 500 MG X 50 CAP", "28.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED003", "IBUPROFENO 400 MG X 100 TAB", "18.00", far, Catalogo.Tipo.PRODUCTO, false }
        };

        List<Catalogo> creados = new ArrayList<>();
        for (Object[] row : itemsData) {
            String codigo = (String) row[0];
            String nombre = (String) row[1];
            BigDecimal tarifa = new BigDecimal((String) row[2]);
            BigDecimal precioSinIgv = tarifa.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
            Nivel nivel = (Nivel) row[3];
            Catalogo.Tipo tipo = (Catalogo.Tipo) row[4];
            Boolean honorarios = (Boolean) row[5];

            Catalogo c = Catalogo.builder()
                    .codigo(codigo)
                    .nombre(nombre)
                    .nombreServicio(nombre)
                    .tarifa(tarifa)
                    .precioSinIgv(precioSinIgv)
                    .precioConIgv(tarifa)
                    .precioCompra(precioSinIgv.multiply(new BigDecimal("0.60")).setScale(2, RoundingMode.HALF_UP))
                    .unidad(BigDecimal.ONE)
                    .tipoUnidad("UND")
                    .honorariosMedicos(honorarios)
                    .nivel(nivel)
                    .tipo(tipo)
                    .tipoPrecio(Catalogo.TipoPrecio.PRECIO_FIJO)
                    .moneda("PEN")
                    .estado(1)
                    .build();

            creados.add(catalogoRepository.save(c));
        }
        return creados;
    }

    private List<Producto> seedProductos(List<Catalogo> servicios, Sucursal sucursal) {
        List<Producto> lista = productoRepository.findAll();
        if (!lista.isEmpty()) {
            return lista;
        }

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        List<Producto> creados = new ArrayList<>();

        for (Catalogo cat : servicios) {
            BigDecimal stock = cat.getTipo() == Catalogo.Tipo.PRODUCTO
                    ? new BigDecimal("350.00")
                    : new BigDecimal("1000.00");

            Producto p = Producto.builder()
                    .idSucursal(idSuc)
                    .idCatalogo(cat.getId())
                    .stockReal(stock)
                    .stockMinimo(new BigDecimal("20.00"))
                    .stockMaximo(new BigDecimal("1000.00"))
                    .manejaUnidad(true)
                    .build();
            creados.add(productoRepository.save(p));
        }
        return creados;
    }

    private List<Proveedor> seedProveedores() {
        List<Proveedor> lista = proveedorRepository.findAll();
        if (!lista.isEmpty()) {
            return lista;
        }

        String[][] provData = {
                { "20100012341", "MEDIFARMA S.A.", "Av. República de Panamá 2577, Lima", "(01) 411-2000",
                        "ventas@medifarma.com.pe" },
                { "20100123456", "LABORATORIOS ROCHE PERÚ S.A.", "Calle Dionisio Derteano 144, San Isidro",
                        "(01) 618-8000", "pedidos@roche.com" },
                { "20200234567", "B. BRAUN MEDICAL PERÚ S.A.", "Av. Separadora Industrial 2451, Ate", "(01) 625-7000",
                        "contacto@bbraun.com.pe" },
                { "20300345678", "SIEMENS HEALTHCARE S.A.C.", "Av. Domingo Orué 971, Surquillo", "(01) 513-8700",
                        "soporte@siemens-health.com" },
                { "20400456789", "ABBOTT LABORATORIES S.A.", "Av. Víctor Andrés Belaúnde 147, San Isidro",
                        "(01) 617-1000", "atencion@abbott.com.pe" }
        };

        List<Proveedor> creados = new ArrayList<>();
        for (String[] row : provData) {
            Proveedor p = Proveedor.builder()
                    .tipoDocIdent("RUC")
                    .numDocIdent(row[0])
                    .razonSocial(row[1])
                    .nombreComercial(row[1])
                    .direccion(row[2])
                    .telefono(row[3])
                    .email(row[4])
                    .estado(1)
                    .fechaRegistro(LocalDateTime.now().minusMonths(6))
                    .build();
            creados.add(proveedorRepository.save(p));
        }
        return creados;
    }

    private void seedCompras(List<Proveedor> proveedores, List<Catalogo> servicios, Sucursal sucursal) {
        if (compraRepository.count() >= 10)
            return;

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        Random random = new Random(42);

        // Generar compras distribuidas mes a mes en 2026
        for (int mes = 1; mes <= 11; mes++) {
            int numCompras = (mes == 11) ? 4 : 1; // 4 compras en noviembre 2026

            for (int k = 1; k <= numCompras; k++) {
                int dia = Math.min(28, (k * 6) + 1);
                LocalDate fecha = LocalDate.of(2026, mes, dia);
                Proveedor prov = proveedores.get(random.nextInt(proveedores.size()));

                BigDecimal subtotal = new BigDecimal(1500 + random.nextInt(4000) * 2).setScale(2, RoundingMode.HALF_UP);
                BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
                BigDecimal total = subtotal.add(igv);

                Compra c = Compra.builder()
                        .proveedor(prov)
                        .idSucursal(idSuc)
                        .tipoComprobante("FACTURA")
                        .serie("F001")
                        .correlativo(String.format("%06d", (mes * 10) + k))
                        .fechaEmision(fecha)
                        .fechaVencimiento(fecha.plusDays(30))
                        .condicionPago("CONTADO")
                        .moneda("PEN")
                        .subtotal(subtotal)
                        .igv(igv)
                        .total(total)
                        .totalPagar(total)
                        .medioPago("TRANSFERENCIA")
                        .porcentajeIgv(new BigDecimal("18.00"))
                        .estado(1)
                        .detalles(new ArrayList<>())
                        .build();

                Catalogo cat = servicios.get(random.nextInt(servicios.size()));
                DetalleCompra det = DetalleCompra.builder()
                        .compra(c)
                        .catalogo(cat)
                        .productoNombre(cat.getNombreServicio())
                        .cantidad(new BigDecimal(50 + random.nextInt(100)))
                        .factorConversion(1)
                        .descripcion("Adquisición periódica de reactivos y medicamentos")
                        .build();

                c.getDetalles().add(det);
                compraRepository.save(c);
            }
        }
    }

    private void seedVentas(List<Catalogo> servicios, Sucursal sucursal, Punto punto) {
        if (servicios == null || servicios.isEmpty())
            return;

        boolean hasVentasNov2026 = ventaRegistroRepository.findAll().stream()
                .anyMatch(v -> v.getFecha() != null && v.getFecha().getYear() == 2026
                        && v.getFecha().getMonthValue() == 11);
        if (hasVentasNov2026) {
            log.info("✅ Ventas de Noviembre 2026 ya están presentes en la base de datos.");
            return;
        }

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        Long idPunto = punto.getPunto();

        String[] pacientes = {
                "JUAN CARLOS PEREZ GOMEZ", "MARIA ELENA ROJAS TORRES", "CARLOS ANDRES MENDOZA SILVA",
                "ANA CECILIA FLORES VEGA", "ROBERTO PAOLO GUERRERO LOPEZ", "CLAUDIA VALENTINA CASTILLO",
                "RICARDO DANIEL SALAZAR", "PATRICIA MONICA QUISPE HUAMAN", "VICTOR HUGO MORALES SANCHEZ",
                "GLADYS MARGARITA GUTIERREZ", "LUIS ALBERTO SANCHEZ REYES", "CARMEN ROSA DIAZ VELASQUEZ",
                "FERNANDO JOSE BUSTAMANTE", "ISABEL CRISTINA VARGAS PAZ", "MIGUEL ANGEL CORDOVA LOPEZ"
        };
        String[] dnis = {
                "45892134", "72145890", "10293847", "48192034", "09283741",
                "73829104", "46192830", "75839201", "08291823", "42819302",
                "71293845", "40928371", "10928374", "76543210", "43210987"
        };
        String[] tiposAtencion = { "PARTICULAR", "CONVENIO", "SEGURO" };

        int correlativoBoleta = 1;
        int correlativoFactura = 1;
        int correlativoTicket = 1;

        // 1. Ventas históricas para meses anteriores (Enero a Octubre 2026) -> 2 por
        // mes
        for (int mes = 1; mes <= 10; mes++) {
            for (int i = 1; i <= 2; i++) {
                LocalDate fecha = LocalDate.of(2026, mes, i * 12);
                int pacIdx = (mes * 2 + i) % pacientes.length;

                Catalogo cat = servicios.get((mes + i) % servicios.size());
                BigDecimal total = cat.getTarifa();
                BigDecimal baseImp = total.divide(new BigDecimal("1.18"), 4, RoundingMode.HALF_UP);
                BigDecimal igv = total.subtract(baseImp);

                String tipoDoc = (i % 2 == 0) ? "F" : "B";
                String serie = (tipoDoc.equals("F")) ? "F001" : "B001";
                String numdoc = String.format("%08d",
                        (tipoDoc.equals("F")) ? correlativoFactura++ : correlativoBoleta++);

                VentaRegistro v = VentaRegistro.builder()
                        .idSucursal(idSuc)
                        .punto(idPunto)
                        .fecha(fecha)
                        .tipoDoc(tipoDoc)
                        .serie(serie)
                        .numdoc(numdoc)
                        .nombrePac(pacientes[pacIdx])
                        .nroDni(dnis[pacIdx])
                        .tipoPac(tipoDoc.equals("F") ? "C" : "P")
                        .tipoAtencion(tiposAtencion[(mes + i) % 3])
                        .baseImp(baseImp)
                        .igv(igv)
                        .total(total)
                        .valorVenta(baseImp)
                        .valorAfecto(baseImp)
                        .estado(1)
                        .modo("N")
                        .detalles(new ArrayList<>())
                        .build();

                VentaDetalle det = VentaDetalle.builder()
                        .ventaRegistro(v)
                        .idArticulo(cat.getId())
                        .glosa(cat.getNombreServicio())
                        .cantidad(BigDecimal.ONE)
                        .valorAf(baseImp)
                        .build();

                v.getDetalles().add(det);
                ventaRegistroRepository.save(v);
            }
        }

        // 2. Ventas masivas en el mes actual (NOVIEMBRE 2026) -> 24 ventas para
        // correlatividad y métricas reales
        for (int dia = 2; dia <= 17; dia++) {
            int ventasDelDia = (dia % 2 == 0) ? 2 : 1;

            for (int k = 0; k < ventasDelDia; k++) {
                LocalDate fecha = LocalDate.of(2026, 11, dia);
                int pacIdx = (dia * 2 + k) % pacientes.length;
                Catalogo cat = servicios.get((dia + k) % servicios.size());

                BigDecimal total = cat.getTarifa();
                BigDecimal baseImp = total.divide(new BigDecimal("1.18"), 4, RoundingMode.HALF_UP);
                BigDecimal igv = total.subtract(baseImp);

                String tipoDoc;
                String serie;
                String numdoc;

                if (k == 0 && dia % 3 == 0) {
                    tipoDoc = "F";
                    serie = "F001";
                    numdoc = String.format("%08d", correlativoFactura++);
                } else if (dia % 5 == 0) {
                    tipoDoc = "T";
                    serie = "T001";
                    numdoc = String.format("%08d", correlativoTicket++);
                } else {
                    tipoDoc = "B";
                    serie = "B001";
                    numdoc = String.format("%08d", correlativoBoleta++);
                }

                // Estado 10 (Anulado) para 2 comprobantes de muestra
                int estado = (dia == 9 || dia == 14) ? 10 : 1;

                VentaRegistro v = VentaRegistro.builder()
                        .idSucursal(idSuc)
                        .punto(idPunto)
                        .fecha(fecha)
                        .tipoDoc(tipoDoc)
                        .serie(serie)
                        .numdoc(numdoc)
                        .nombrePac(pacientes[pacIdx])
                        .nroDni(dnis[pacIdx])
                        .tipoPac(tipoDoc.equals("F") ? "C" : "P")
                        .tipoAtencion(tiposAtencion[(dia + k) % 3])
                        .baseImp(baseImp)
                        .igv(igv)
                        .total(total)
                        .valorVenta(baseImp)
                        .valorAfecto(baseImp)
                        .estado(estado)
                        .modo("N")
                        .detalles(new ArrayList<>())
                        .build();

                VentaDetalle det = VentaDetalle.builder()
                        .ventaRegistro(v)
                        .idArticulo(cat.getId())
                        .glosa(cat.getNombreServicio())
                        .cantidad(BigDecimal.ONE)
                        .valorAf(baseImp)
                        .build();

                v.getDetalles().add(det);
                ventaRegistroRepository.save(v);
            }
        }
    }

    private void seedDescuentos() {
        if (autorizacionDescuentoRepository.count() >= 5)
            return;

        Object[][] descData = {
                { "MARIA ELENA ROJAS TORRES", "72145890", "DR. JUAN RAMOS", "160.00", "30.00", "130.00",
                        "Convenio Adulto Mayor", "APROBADO" },
                { "CARLOS ANDRES MENDOZA", "10293847", "DRA. SOFIA VALDIVIA", "150.00", "25.00", "125.00",
                        "Campaña Salud Ocupacional", "APROBADO" },
                { "ROBERTO PAOLO GUERRERO", "09283741", "DR. ALBERTO FARFAN", "120.00", "20.00", "100.00",
                        "Descuento por Paquete Familiar", "APROBADO" },
                { "ANA CECILIA FLORES", "48192034", "DR. JUAN RAMOS", "85.00", "15.00", "70.00", "Paciente vulnerable",
                        "PENDIENTE" },
                { "VICTOR HUGO MORALES", "08291823", "DRA. SOFIA VALDIVIA", "95.00", "15.00", "80.00",
                        "Cortesía primera consulta", "RECHAZADO" }
        };

        for (Object[] row : descData) {
            AutorizacionDescuento ad = AutorizacionDescuento.builder()
                    .pacienteNombre((String) row[0])
                    .pacienteDni((String) row[1])
                    .doctorNombre((String) row[2])
                    .montoOriginal(new BigDecimal((String) row[3]))
                    .montoDescuento(new BigDecimal((String) row[4]))
                    .montoFinal(new BigDecimal((String) row[5]))
                    .motivoSolicitud((String) row[6])
                    .estado((String) row[7])
                    .build();
            autorizacionDescuentoRepository.save(ad);
        }
    }

    private void seedPagos() {
        if (pagoComprobanteRepository.count() >= 8)
            return;

        LocalDate hoy = LocalDate.of(2026, 11, 15);

        // 1. Honorarios Médicos
        Object[][] honData = {
                { "DR. JUAN CARLOS RAMOS DIAZ", "41829012", "E001", "000145", "4500.00", "360.00", "4140.00",
                        "Cardiología" },
                { "DRA. SOFÍA VALDIVIA ROMERO", "42918203", "E001", "000146", "3800.00", "304.00", "3496.00",
                        "Pediatría" },
                { "DR. ALBERTO FARFÁN CASTRO", "40192834", "E001", "000147", "5200.00", "416.00", "4784.00",
                        "Traumatología" },
                { "DRA. MONICA SUAREZ VEGA", "45019283", "E001", "000148", "4100.00", "328.00", "3772.00",
                        "Ginecología" }
        };

        for (Object[] row : honData) {
            PagoComprobante pc = PagoComprobante.builder()
                    .tipoPago("HONORARIOS")
                    .tipoBeneficiario("MEDICO")
                    .nombreRazonSocial((String) row[0])
                    .numeroDocumento((String) row[1])
                    .serieComprobante((String) row[2])
                    .numeroRecibo((String) row[3])
                    .areaServicio((String) row[7])
                    .montoBruto(new BigDecimal((String) row[4]))
                    .retencion(new BigDecimal((String) row[5]))
                    .montoNeto(new BigDecimal((String) row[6]))
                    .fechaEmision(hoy.minusDays(5))
                    .fechaPago(hoy)
                    .periodoMes("NOVIEMBRE")
                    .periodoAnio(2026)
                    .conceptoPago("Honorarios Médicos Consulta y Procedimientos")
                    .estadoPago("PAGADO")
                    .build();
            pagoComprobanteRepository.save(pc);
        }

        // 2. Pagos de Personal (Nómina)
        Object[][] perData = {
                { "LIC. CARMEN ROSA DAVILA", "43920192", "Enfermería", "Enfermera Jefa", "2800.00" },
                { "TEC. MARIO BENAVIDES LOPEZ", "44819203", "Laboratorio", "Tecnólogo Médico", "2400.00" },
                { "SRTA. LUCIA ESPINOZA CAMPOS", "71293845", "Recepción", "Cajera y Recepción", "1800.00" },
                { "ING. JORGE ALARCON MEZA", "40928374", "Sistemas", "Administrador de Sistemas", "3500.00" }
        };

        for (Object[] row : perData) {
            PagoComprobante pp = PagoComprobante.builder()
                    .tipoPago("PLANILLA")
                    .tipoBeneficiario("TRABAJADOR")
                    .nombreRazonSocial((String) row[0])
                    .numeroDocumento((String) row[1])
                    .areaTrabajador((String) row[2])
                    .cargoTrabajador((String) row[3])
                    .montoBruto(new BigDecimal((String) row[4]))
                    .montoNeto(new BigDecimal((String) row[4]))
                    .fechaEmision(hoy.minusDays(2))
                    .fechaPago(hoy)
                    .periodoMes("NOVIEMBRE")
                    .periodoAnio(2026)
                    .conceptoPago("Remuneración Mensual Personal de Sede")
                    .estadoPago("PAGADO")
                    .build();
            pagoComprobanteRepository.save(pp);
        }
    }

    private void seedCajaChica(Sucursal sucursal) {
        if (cajaChicaRepository.count() >= 1)
            return;

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        CajaChica cc = CajaChica.builder()
                .nombre("CAJA CHICA SEDE PRINCIPAL")
                .idSucursal(idSuc)
                .saldoInicial(new BigDecimal("1500.00"))
                .saldoActual(new BigDecimal("1220.00"))
                .estado(CajaChica.EstadoCaja.ABIERTA)
                .usuarioCreacion("admin")
                .build();
        cc = cajaChicaRepository.save(cc);

        MetodoPago efectivo = metodoPagoRepository.findByDescripcionIgnoreCase("EFECTIVO")
                .orElseGet(() -> metodoPagoRepository.findAll().stream().findFirst().orElse(null));

        if (efectivo != null) {
            Object[][] movs = {
                    { CajaChicaMovimiento.TipoMovimiento.EGRESO, "45.00", "Compra de papel bond y útiles de oficina",
                            "B002-341" },
                    { CajaChicaMovimiento.TipoMovimiento.EGRESO, "85.00", "Artículos de limpieza y desinfección",
                            "F001-9872" },
                    { CajaChicaMovimiento.TipoMovimiento.EGRESO, "150.00", "Movilidad y mensajería médica urgente",
                            "REC-012" },
                    { CajaChicaMovimiento.TipoMovimiento.INGRESO, "500.00", "Reembolso parcial fondo fijo", "CHQ-8902" }
            };

            for (Object[] m : movs) {
                CajaChicaMovimiento mov = CajaChicaMovimiento.builder()
                        .cajaChica(cc)
                        .tipo((CajaChicaMovimiento.TipoMovimiento) m[0])
                        .monto(new BigDecimal((String) m[1]))
                        .descripcion((String) m[2])
                        .referencia((String) m[3])
                        .metodoPago(efectivo)
                        .fecha(LocalDateTime.now().minusDays(3))
                        .build();
                cajaChicaMovimientoRepository.save(mov);
            }
        }
    }

    private void seedArqueos(Sucursal sucursal, Punto punto) {
        if (arqueoCajaRepository.count() >= 2)
            return;

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;
        Long idPunto = punto.getPunto();

        ArqueoCaja a1 = ArqueoCaja.builder()
                .fechaHora(LocalDateTime.now().minusDays(1))
                .idUsuario(1L)
                .idSucursal(idSuc)
                .idPunto(idPunto)
                .saldoEsperado(new BigDecimal("3420.00"))
                .saldoFisico(new BigDecimal("3420.00"))
                .diferencia(BigDecimal.ZERO)
                .observaciones("Arqueo de cierre conforme turno tarde. Sin descuadre.")
                .estado("CERRADO")
                .build();
        arqueoCajaRepository.save(a1);

        ArqueoCaja a2 = ArqueoCaja.builder()
                .fechaHora(LocalDateTime.now().minusDays(2))
                .idUsuario(1L)
                .idSucursal(idSuc)
                .idPunto(idPunto)
                .saldoEsperado(new BigDecimal("2890.00"))
                .saldoFisico(new BigDecimal("2890.00"))
                .diferencia(BigDecimal.ZERO)
                .observaciones("Arqueo conforme turno mañana.")
                .estado("CERRADO")
                .build();
        arqueoCajaRepository.save(a2);
    }

    private void seedKardex(List<Producto> productos, List<Catalogo> servicios, Sucursal sucursal) {
        if (kardexRepository.count() >= 5 || productos.isEmpty() || servicios.isEmpty())
            return;

        Long idSuc = sucursal.getIdSucursal() != null ? sucursal.getIdSucursal() : 1L;

        for (int i = 0; i < Math.min(productos.size(), 4); i++) {
            Producto p = productos.get(i);
            Catalogo c = servicios.stream()
                    .filter(cat -> cat.getId().equals(p.getIdCatalogo()))
                    .findFirst()
                    .orElse(servicios.get(0));

            Kardex k = Kardex.builder()
                    .idArticuloKardex(UUID.randomUUID().toString())
                    .idAlmArticulo(p.getIdProducto())
                    .idCatalogo(c.getId())
                    .idSucursal(idSuc)
                    .idUsuario(1L)
                    .operacion("COMPRA_INGRESO")
                    .signo("+")
                    .cantidad(new BigDecimal("100.00"))
                    .costoUnitario(c.getPrecioSinIgv())
                    .costoTotal(c.getPrecioSinIgv().multiply(new BigDecimal("100.00")))
                    .saldoCantidad(p.getStockReal())
                    .saldoCostoUnitario(c.getPrecioSinIgv())
                    .saldoCostoTotal(c.getPrecioSinIgv().multiply(p.getStockReal()))
                    .fecha(LocalDateTime.now().minusDays(5))
                    .detalle("Ingreso por orden de compra inicial")
                    .numDoc("FAC-001-000124")
                    .build();

            kardexRepository.save(k);
        }
    }

    private void seedAudit() {
        if (auditRepository.count() >= 5)
            return;

        Object[][] logs = {
                { 1L, "admin", "AUTH", "LOGIN", "Inicio de sesión exitoso desde consola web", "192.168.1.100",
                        "SUCCESS", "/api/auth/login", "POST" },
                { 1L, "admin", "VENTAS", "EMITIR_COMPROBANTE", "Emisión de Boleta B001-00000024 por S/. 120.00",
                        "192.168.1.100", "SUCCESS", "/api/ventas", "POST" },
                { 1L, "admin", "COMPRAS", "REGISTRAR_ORDEN", "Registro de Factura de Compra F001-000110",
                        "192.168.1.100", "SUCCESS", "/api/compras", "POST" },
                { 1L, "admin", "CAJA_CHICA", "EGRESO_CAJA", "Egreso de Caja Chica S/. 85.00 para útiles",
                        "192.168.1.100", "SUCCESS", "/api/caja-chica/movimientos", "POST" },
                { 1L, "admin", "DESCUENTOS", "AUTORIZAR", "Aprobación de descuento especial convenio 20%",
                        "192.168.1.100", "SUCCESS", "/api/descuentos/autorizar", "POST" }
        };

        for (Object[] row : logs) {
            AuditLog a = AuditLog.builder()
                    .userId((Long) row[0])
                    .username((String) row[1])
                    .module((String) row[2])
                    .action((String) row[3])
                    .details((String) row[4])
                    .ipAddress((String) row[5])
                    .status((String) row[6])
                    .url((String) row[7])
                    .method((String) row[8])
                    .timestamp(LocalDateTime.now().minusHours(2))
                    .build();
            auditRepository.save(a);
        }
    }
}
