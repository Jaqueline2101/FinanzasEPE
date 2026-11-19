package com.pe.finanzas.config.initializer;

import com.pe.finanzas.activos_fijos.entity.Activo;
import com.pe.finanzas.activos_fijos.repository.ActivoRepository;
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
import com.pe.finanzas.caja_general.entity.CajaGeneral;
import com.pe.finanzas.caja_general.repository.CajaGeneralRepository;
import com.pe.finanzas.catalogo.entity.Catalogo;
import com.pe.finanzas.catalogo.repository.CatalogoRepository;
import com.pe.finanzas.compra.entity.Compra;
import com.pe.finanzas.compra.entity.DetalleCompra;
import com.pe.finanzas.compra.repository.CompraRepository;
import com.pe.finanzas.compra.repository.DetalleCompraRepository;
import com.pe.finanzas.cuentas_corrientes.entity.CuentaCorriente;
import com.pe.finanzas.cuentas_corrientes.repository.CuentaCorrienteRepository;
import com.pe.finanzas.empresa.entity.Empresa;
import com.pe.finanzas.empresa.repository.EmpresaRepository;
import com.pe.finanzas.kardex.entity.Kardex;
import com.pe.finanzas.kardex.repository.KardexRepository;
import com.pe.finanzas.motor_precios.entity.CredPlan;
import com.pe.finanzas.motor_precios.repository.CredPlanRepository;
import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.repository.NivelRepository;
import com.pe.finanzas.nota_credito.entity.NotaCredito;
import com.pe.finanzas.nota_credito.repository.NotaCreditoRepository;
import com.pe.finanzas.orden_compra.entity.OrdenCompra;
import com.pe.finanzas.orden_compra.repository.OrdenCompraRepository;
import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.repository.PagoComprobanteRepository;
import com.pe.finanzas.paquetes.entity.Paquete;
import com.pe.finanzas.paquetes.repository.PaqueteRepository;
import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.repository.ProductoRepository;
import com.pe.finanzas.proveedores.entity.Proveedor;
import com.pe.finanzas.proveedores.repository.ProveedorRepository;
import com.pe.finanzas.puntos.entity.Punto;
import com.pe.finanzas.puntos.repository.PuntoRepository;
import com.pe.finanzas.reports.audit.entity.AuditLog;
import com.pe.finanzas.reports.audit.repository.AuditRepository;
import com.pe.finanzas.solicitud_anulacion.entity.SolicitudAnulacion;
import com.pe.finanzas.solicitud_anulacion.repository.SolicitudAnulacionRepository;
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
    private final OrdenCompraRepository ordenCompraRepository;
    private final CompraRepository compraRepository;
    private final DetalleCompraRepository detalleCompraRepository;
    private final VentaRegistroRepository ventaRegistroRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final SolicitudAnulacionRepository solicitudAnulacionRepository;
    private final NotaCreditoRepository notaCreditoRepository;
    private final AutorizacionDescuentoRepository autorizacionDescuentoRepository;
    private final PagoComprobanteRepository pagoComprobanteRepository;
    private final CajaChicaRepository cajaChicaRepository;
    private final CajaChicaMovimientoRepository cajaChicaMovimientoRepository;
    private final CajaGeneralRepository cajaGeneralRepository;
    private final ArqueoCajaRepository arqueoCajaRepository;
    private final MetodoPagoRepository metodoPagoRepository;
    private final KardexRepository kardexRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;
    private final AuditRepository auditRepository;
    private final PaqueteRepository paqueteRepository;
    private final EmpresaRepository empresaRepository;
    private final CredPlanRepository credPlanRepository;
    private final CuentaCorrienteRepository cuentaCorrienteRepository;
    private final ActivoRepository activoRepository;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 INICIANDO POBLADO MASIVO Y VERIFICACIÓN (MÍNIMO 20 REGISTROS POR AMBIENTE)...");

        try {
            // 1. Pacientes / Usuarios (Base para relaciones de cuenta corriente y cajeros)
            List<DatosPersonales> pacientes = seedPacientes();

            // 2. Sucursales (Mínimo 20)
            List<Sucursal> sucursales = seedSucursales();
            Sucursal sedePrincipal = sucursales.get(0);

            // 3. Puntos de Venta (Mínimo 20)
            List<Punto> puntos = seedPuntosVenta(sucursales);
            Punto puntoPrincipal = puntos.get(0);

            // 4. Series de Correlatividad
            seedSeriesCorrelativo(sucursales);

            // 5. Niveles Organizacionales y Centros de Costo (Mínimo 20)
            Map<String, Nivel> niveles = seedNiveles();

            // 6. Catálogo de Servicios Médicos y Farmacia (Mínimo 25)
            List<Catalogo> servicios = seedCatalogo(niveles);

            // 7. Inventario / Productos en Stock (Mínimo 25)
            List<Producto> productos = seedProductos(servicios, sucursales);

            // 8. Proveedores Médicos y Droguerías (Mínimo 20)
            List<Proveedor> proveedores = seedProveedores();

            // 9. Órdenes de Compra (Mínimo 20)
            seedOrdenesCompra(proveedores);

            // 10. Compras con Detalle (Mínimo 25)
            seedCompras(proveedores, servicios, sucursales);

            // 11. Ventas y Correlatividad (Mínimo 50, con más de 24 en Noviembre 2026)
            List<VentaRegistro> ventas = seedVentas(servicios, sucursales, puntos, pacientes);

            // 12. Solicitudes de Anulación de Comprobantes (Mínimo 20)
            seedSolicitudesAnulacion(ventas, sucursales);

            // 13. Notas de Crédito (Mínimo 20)
            seedNotasCredito(ventas, sucursales);

            // 14. Descuentos Autorizados (Mínimo 20)
            seedDescuentos(pacientes);

            // 15. Honorarios Médicos (Mínimo 20)
            seedHonorarios();

            // 16. Pagos de Personal / Planilla (Mínimo 20)
            seedPagosPersonal();

            // 17. Otros Pagos y Servicios (Mínimo 20)
            seedOtrosPagos();

            // 18. Caja Chica y Movimientos (Mínimo 20)
            seedCajaChica(sedePrincipal);

            // 19. Caja General Bancaria
            seedCajaGeneral(sedePrincipal);

            // 20. Arqueos de Caja Diarios (Mínimo 20)
            seedArqueos(sucursales, puntos);

            // 21. Cuentas Corrientes Bancarias (Mínimo 20)
            seedCuentasCorrientes();

            // 22. Kardex de Movimientos Valorizados (Mínimo 25)
            seedKardex(productos, servicios, sedePrincipal);

            // 23. Paquetes Preventivos y Campañas (Mínimo 20)
            seedPaquetes();

            // 24. Empresas Corporativas Clientes (Mínimo 20)
            seedEmpresas();

            // 25. Cuentas de Convenio y Aseguradoras (Mínimo 20)
            seedConvenios();

            // 26. Activos Fijos y Equipamiento Médico (Mínimo 20)
            seedActivos(servicios);

            // 27. Auditoría de Seguridad (Mínimo 25)
            seedAudit();

            log.info("🎉 ¡DATOS DE DEMOSTRACIÓN COMPLETADOS! CADA AMBIENTE TIENE AL MENOS 20 REGISTROS COHERENTES Y RELACIONADOS.");
        } catch (Exception e) {
            log.error("⚠️ Error durante el sembrado de datos de demostración: {}", e.getMessage(), e);
        }
    }

    // ==========================================
    // 1. PACIENTES / DATOS PERSONALES (Mínimo 25)
    // ==========================================
    private List<DatosPersonales> seedPacientes() {
        List<DatosPersonales> pacientes = datosPersonalesRepository.findAll();
        String[][] datos = {
                { "JUAN CARLOS", "PEREZ", "GOMEZ", "45892134", "jperez@gmail.com", "987123456" },
                { "MARIA ELENA", "ROJAS", "TORRES", "72145890", "mrojas@gmail.com", "991234567" },
                { "CARLOS ANDRES", "MENDOZA", "SILVA", "10293847", "cmendoza@hotmail.com", "984561230" },
                { "ANA CECILIA", "FLORES", "VEGA", "48192034", "aflores@gmail.com", "976543210" },
                { "ROBERTO PAOLO", "GUERRERO", "LOPEZ", "09283741", "rguerrero@outlook.com", "998877665" },
                { "CLAUDIA VALENTINA", "CASTILLO", "PEREZ", "73829104", "ccastillo@gmail.com", "965432187" },
                { "RICARDO DANIEL", "SALAZAR", "MEZA", "46192830", "rsalazar@yahoo.com", "971234987" },
                { "PATRICIA MONICA", "QUISPE", "HUAMAN", "75839201", "pquispe@gmail.com", "954321678" },
                { "VICTOR HUGO", "MORALES", "SANCHEZ", "08291823", "vmorales@hotmail.com", "983214567" },
                { "GLADYS MARGARITA", "GUTIERREZ", "DIAZ", "42819302", "ggutierrez@gmail.com", "978123654" },
                { "LUIS ALBERTO", "SANCHEZ", "REYES", "71293845", "lsanchez@gmail.com", "967891234" },
                { "CARMEN ROSA", "DIAZ", "VELASQUEZ", "40928371", "cdiaz@outlook.com", "954123789" },
                { "FERNANDO JOSE", "BUSTAMANTE", "RUIZ", "10928374", "fbustamante@gmail.com", "981234908" },
                { "ISABEL CRISTINA", "VARGAS", "PAZ", "76543210", "ivargas@gmail.com", "992345671" },
                { "MIGUEL ANGEL", "CORDOVA", "LOPEZ", "43210987", "mcordova@yahoo.com", "973456128" },
                { "TERESA DE JESUS", "ALVAREZ", "CAMPOS", "41526374", "talvarez@gmail.com", "985612347" },
                { "JORGE LUIS", "ESPINOZA", "RIVERA", "70918273", "jespinoza@gmail.com", "994561283" },
                { "LUCIA BEATRIZ", "NAVARRO", "QUISPE", "44123890", "lnavarro@gmail.com", "963258741" },
                { "CESAR AUGUSTO", "VALVERDE", "BRAVO", "09812734", "cvalverde@gmail.com", "951478236" },
                { "ROSA MARIA", "BENITEZ", "CASTRO", "71829304", "rbenitez@gmail.com", "987456321" },
                { "HECTOR MANUEL", "CHAVEZ", "SOTO", "43901827", "hchavez@gmail.com", "996325814" },
                { "MONICA ROCIO", "ORTEGA", "VILCHEZ", "74839201", "mortega@gmail.com", "982147365" },
                { "ALFREDO GABRIEL", "PAREDES", "LEON", "40192837", "aparedes@gmail.com", "974125896" },
                { "SILVIA ELIZABETH", "HERRERA", "PALOMINO", "73019284", "sherrera@gmail.com", "961234785" },
                { "DANIEL ENRIQUE", "ROMERO", "CALLIRGOS", "42901823", "dromero@gmail.com", "952369874" }
        };

        for (String[] d : datos) {
            String dni = d[3];
            if (pacientes.stream().noneMatch(p -> dni.equals(p.getNumdoc()))) {
                String nomCompleto = d[0] + " " + d[1] + " " + d[2];
                DatosPersonales p = new DatosPersonales();
                p.setNombre(d[0]);
                p.setApepat(d[1]);
                p.setApemat(d[2]);
                p.setVerNombre(nomCompleto);
                p.setNumdoc(dni);
                p.setTipodoc("1");
                p.setEmail(d[4]);
                p.setFono1(d[5]);
                p.setLogin("pac_" + dni);
                p.setPasswd("");
                p.setActive(true);
                p.setCreatedAt(LocalDateTime.now().minusMonths(6));
                pacientes.add(datosPersonalesRepository.save(p));
            }
        }
        return pacientes;
    }

    // ==========================================
    // 2. SUCURSALES (Mínimo 20)
    // ==========================================
    private List<Sucursal> seedSucursales() {
        List<Sucursal> sucursales = sucursalRepository.findAll();
        String[][] sedes = {
                { "SEDE PRINCIPAL - LIMA CENTRO", "Av. Arequipa 1420, Cercado de Lima" },
                { "SEDE SAN ISIDRO", "Av. Javier Prado Este 1520, San Isidro" },
                { "SEDE MIRAFLORES", "Av. José Pardo 601, Miraflores" },
                { "SEDE SURCO - CHACARILLA", "Av. Primavera 940, Santiago de Surco" },
                { "SEDE LOS OLIVOS", "Av. Carlos Izaguirre 812, Los Olivos" },
                { "SEDE SAN BORJA", "Av. San Borja Sur 450, San Borja" },
                { "SEDE MAGDALENA", "Jr. Bolognesi 320, Magdalena del Mar" },
                { "SEDE LA MOLINA", "Av. La Molina 1120, La Molina" },
                { "SEDE JESÚS MARÍA", "Av. General Garzón 1240, Jesús María" },
                { "SEDE SAN MIGUEL", "Av. La Marina 2150, San Miguel" },
                { "SEDE PUEBLO LIBRE", "Av. Sucre 780, Pueblo Libre" },
                { "SEDE ATE - VITARTE", "Av. Nicolás Ayllón 3410, Ate" },
                { "SEDE CALLAO", "Av. Sáenz Peña 680, Callao" },
                { "SEDE INDEPENDENCIA", "Av. Túpac Amaru 3850, Independencia" },
                { "SEDE CHORRILLOS", "Av. Defensores del Morro 1050, Chorrillos" },
                { "SEDE BELLAVISTA", "Av. Oscar R. Benavides 4500, Bellavista" },
                { "SEDE BARRANCO", "Av. Grau 240, Barranco" },
                { "SEDE BREÑA", "Av. Arica 630, Breña" },
                { "SEDE LINCE", "Av. Petit Thouars 1890, Lince" },
                { "SEDE SAN JUAN DE LURIGANCHO", "Av. Próceres de la Independencia 1640, SJL" }
        };

        for (String[] sede : sedes) {
            if (sucursales.stream().noneMatch(s -> s.getNombreSucursal() != null && s.getNombreSucursal().equalsIgnoreCase(sede[0]))) {
                Sucursal s = new Sucursal();
                s.setNombreSucursal(sede[0]);
                s.setDireccion(sede[1]);
                s.setEstado(1);
                sucursales.add(sucursalRepository.save(s));
            }
        }
        return sucursales;
    }

    // ==========================================
    // 3. PUNTOS DE VENTA (Mínimo 20)
    // ==========================================
    private List<Punto> seedPuntosVenta(List<Sucursal> sucursales) {
        List<Punto> puntos = puntoRepository.findAll();
        if (puntos.size() < 20) {
            for (int i = puntos.size() + 1; i <= 20; i++) {
                Sucursal suc = sucursales.get((i - 1) % sucursales.size());
                Punto p = Punto.builder()
                        .nombre(String.format("%02d-Caja %d - %s", i, (i % 3) + 1, suc.getNombreSucursal().replace("SEDE ", "")))
                        .idSucursal(suc.getIdSucursal() != null ? suc.getIdSucursal().intValue() : 1)
                        .tipo((i % 4 == 0) ? "FARMACIA" : "CAJA")
                        .build();
                puntos.add(puntoRepository.save(p));
            }
        }
        return puntos;
    }

    // ==========================================
    // 4. SERIES DE CORRELATIVIDAD
    // ==========================================
    private void seedSeriesCorrelativo(List<Sucursal> sucursales) {
        for (Sucursal s : sucursales) {
            Long idSuc = s.getIdSucursal();
            crearSerieSiNoExiste(idSuc, "B", "B001", 30L);
            crearSerieSiNoExiste(idSuc, "F", "F001", 20L);
            crearSerieSiNoExiste(idSuc, "T", "T001", 15L);
            crearSerieSiNoExiste(idSuc, "NC", "NC01", 10L);
        }
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

    // ==========================================
    // 5. NIVELES / CENTROS DE COSTO (Mínimo 20)
    // ==========================================
    private Map<String, Nivel> seedNiveles() {
        Map<String, Nivel> mapa = new HashMap<>();
        List<Nivel> existentes = nivelRepository.findAll();
        existentes.forEach(n -> mapa.put(n.getNombre().toUpperCase(), n));

        String[][] areas = {
                { "LABORATORIO CLÍNICO", "CC-LAB" },
                { "DIAGNÓSTICO POR IMÁGENES", "CC-IMG" },
                { "CONSULTAS MÉDICAS", "CC-MED" },
                { "FARMACIA CENTRAL", "CC-FAR" },
                { "CARDIOLOGÍA", "CC-CARD" },
                { "PEDIATRÍA Y NEONATOLOGÍA", "CC-PED" },
                { "GINECOLOGÍA Y OBSTETRICIA", "CC-GIN" },
                { "TRAUMATOLOGÍA Y ORTOPEDIA", "CC-TRAUM" },
                { "ODONTOLOGÍA INTEGRAL", "CC-ODONT" },
                { "OFTALMOLOGÍA", "CC-OFT" },
                { "DERMATOLOGÍA CLÍNICA", "CC-DERM" },
                { "GASTROENTEROLOGÍA", "CC-GASTRO" },
                { "NEUROLOGÍA", "CC-NEURO" },
                { "UROLOGÍA", "CC-UROL" },
                { "OTORRINOLARINGOLOGÍA", "CC-ORL" },
                { "ENDOCRINOLOGÍA", "CC-ENDO" },
                { "MEDICINA FÍSICA Y REHABILITACIÓN", "CC-REHAB" },
                { "NUTRICIÓN Y DIETÉTICA", "CC-NUTR" },
                { "PSICOLOGÍA CLÍNICA", "CC-PSIC" },
                { "CENTRO QUIRÚRGICO Y HOSPITALIZACIÓN", "CC-HOSP" }
        };

        for (int i = 0; i < areas.length; i++) {
            String nom = areas[i][0];
            String cc = areas[i][1];
            if (!mapa.containsKey(nom.toUpperCase())) {
                Nivel n = new Nivel();
                n.setNombre(nom);
                n.setNumNivel(String.format("%02d", mapa.size() + 1));
                n.setCentroCosto(cc);
                n.setEstado(1);
                n.setNivelJerarquia(1);
                Nivel saved = nivelRepository.save(n);
                mapa.put(nom.toUpperCase(), saved);
            }
        }
        return mapa;
    }

    // ==========================================
    // 6. CATÁLOGO MÉDICO Y SERVICIOS (Mínimo 25)
    // ==========================================
    private List<Catalogo> seedCatalogo(Map<String, Nivel> niveles) {
        List<Catalogo> lista = catalogoRepository.findAll();
        Nivel lab = niveles.getOrDefault("LABORATORIO CLÍNICO", niveles.values().iterator().next());
        Nivel img = niveles.getOrDefault("DIAGNÓSTICO POR IMÁGENES", lab);
        Nivel med = niveles.getOrDefault("CONSULTAS MÉDICAS", lab);
        Nivel far = niveles.getOrDefault("FARMACIA CENTRAL", lab);

        Object[][] items = {
                { "SERV001", "HEMOGRAMA COMPLETO AUTOMATIZADO", "45.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV002", "PERFIL LIPÍDICO COMPLETO", "85.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV003", "GLUCOSA EN SANGRE BASAL", "25.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV004", "EXAMEN COMPLETO DE ORINA", "30.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV005", "ECOGRAFÍA ABDOMINAL TOTAL", "160.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV006", "RADIOGRAFÍA DE TÓRAX DIGITAL", "95.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV007", "CONSULTA MEDICINA GENERAL", "80.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV008", "CONSULTA ESPECIALIZADA CARDIOLOGÍA", "150.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV009", "CONSULTA ESPECIALIZADA PEDIATRÍA", "120.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV010", "RESONANCIA MAGNÉTICA CEREBRAL", "550.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV011", "TOMOGRAFÍA AXIAL COMPUTARIZADA", "380.00", img, Catalogo.Tipo.SERVICIO, true },
                { "SERV012", "ELECTROCARDIOGRAMA DE REPOSO", "75.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV013", "ENDOSCOPIA DIGESTIVA ALTA", "420.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV014", "ESPIROMETRÍA CLÍNICA", "110.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV015", "AUDIOMETRÍA OCUPACIONAL", "65.00", med, Catalogo.Tipo.SERVICIO, true },
                { "SERV016", "HEMOGLOBINA GLICOSILADA HBA1C", "55.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV017", "PERFIL HEPÁTICO COMPLETO", "95.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV018", "PERFIL TIROIDEO (TSH, T3, T4 LIBRE)", "130.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV019", "DOSAJE DE VITAMINA D 25-OH", "140.00", lab, Catalogo.Tipo.SERVICIO, false },
                { "SERV020", "PAPANICOLAOU Y COLPOSCOPIA", "110.00", med, Catalogo.Tipo.SERVICIO, true },
                { "MED001", "PARACETAMOL 500 MG X 100 TAB", "15.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED002", "AMOXICILINA 500 MG X 50 CAP", "28.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED003", "IBUPROFENO 400 MG X 100 TAB", "18.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED004", "AZITROMICINA 500 MG X 3 TAB", "24.00", far, Catalogo.Tipo.PRODUCTO, false },
                { "MED005", "OMEPRAZOL 20 MG X 30 CAP", "22.00", far, Catalogo.Tipo.PRODUCTO, false }
        };

        for (Object[] row : items) {
            String cod = (String) row[0];
            if (lista.stream().noneMatch(c -> cod.equalsIgnoreCase(c.getCodigo()))) {
                String nom = (String) row[1];
                BigDecimal tarifa = new BigDecimal((String) row[2]);
                BigDecimal precioSinIgv = tarifa.divide(new BigDecimal("1.18"), 2, RoundingMode.HALF_UP);
                Nivel nivel = (Nivel) row[3];
                Catalogo.Tipo tipo = (Catalogo.Tipo) row[4];
                Boolean honorarios = (Boolean) row[5];

                Catalogo c = Catalogo.builder()
                        .codigo(cod)
                        .nombre(nom)
                        .nombreServicio(nom)
                        .unidad(BigDecimal.ONE)
                        .tipoUnidad("UND")
                        .tarifa(tarifa)
                        .precioSinIgv(precioSinIgv)
                        .precioConIgv(tarifa)
                        .nivel(nivel)
                        .tipo(tipo)
                        .tipoPrecio(Catalogo.TipoPrecio.PRECIO_FIJO)
                        .honorariosMedicos(honorarios)
                        .estado(1)
                        .build();
                lista.add(catalogoRepository.save(c));
            }
        }
        return lista;
    }

    // ==========================================
    // 7. PRODUCTOS E INVENTARIO (Mínimo 25)
    // ==========================================
    private List<Producto> seedProductos(List<Catalogo> servicios, List<Sucursal> sucursales) {
        List<Producto> productos = productoRepository.findAll();
        Long idSuc = sucursales.get(0).getIdSucursal();

        for (Catalogo cat : servicios) {
            if (productos.stream().noneMatch(p -> cat.getId().equals(p.getIdCatalogo()))) {
                BigDecimal stock = cat.getTipo() == Catalogo.Tipo.PRODUCTO ? new BigDecimal("350.00") : new BigDecimal("1000.00");
                Producto p = Producto.builder()
                        .idSucursal(idSuc)
                        .idCatalogo(cat.getId())
                        .stockReal(stock)
                        .stockMinimo(new BigDecimal("20.00"))
                        .stockMaximo(new BigDecimal("1000.00"))
                        .manejaUnidad(true)
                        .build();
                productos.add(productoRepository.save(p));
            }
        }
        return productos;
    }

    // ==========================================
    // 8. PROVEEDORES (Mínimo 20)
    // ==========================================
    private List<Proveedor> seedProveedores() {
        List<Proveedor> lista = proveedorRepository.findAll();
        String[][] provData = {
                { "20100012341", "MEDIFARMA S.A.", "Av. República de Panamá 2577, Lima", "(01) 411-2000", "ventas@medifarma.com.pe" },
                { "20100123456", "LABORATORIOS ROCHE PERÚ S.A.", "Calle Dionisio Derteano 144, San Isidro", "(01) 618-8000", "pedidos@roche.com" },
                { "20200234567", "B. BRAUN MEDICAL PERÚ S.A.", "Av. Separadora Industrial 2451, Ate", "(01) 625-7000", "contacto@bbraun.com.pe" },
                { "20300345678", "SIEMENS HEALTHCARE S.A.C.", "Av. Domingo Orué 971, Surquillo", "(01) 513-8700", "soporte@siemens-health.com" },
                { "20400456789", "ABBOTT LABORATORIES S.A.", "Av. Víctor Andrés Belaúnde 147, San Isidro", "(01) 617-1000", "atencion@abbott.com.pe" },
                { "20500567890", "FARMINDUSTRIA S.A.", "Av. Mariscal Miller 2151, Lince", "(01) 471-1200", "ventas@farmindustria.com.pe" },
                { "20600678901", "LABORATORIOS BAGÓ DEL PERÚ S.A.", "Av. Jorge Chávez 154, Miraflores", "(01) 610-8500", "contacto@bago.com.pe" },
                { "20700789012", "PFIZER S.A.", "Av. Javier Prado Este 4200, Surco", "(01) 615-5000", "pedidos@pfizer.com" },
                { "20800890123", "NOVARTIS BIOSERVICES PERÚ S.A.C.", "Av. Las Camelias 280, San Isidro", "(01) 211-3700", "servicios@novartis.com" },
                { "20900901234", "BAYER S.A.", "Av. Paseo de la República 3074, San Isidro", "(01) 211-3800", "contacto@bayer.com" },
                { "20111122334", "DROGUERÍA ALBIS S.A.C.", "Av. Industrial 1020, Ate", "(01) 326-4500", "ventas@albis.com.pe" },
                { "20222233445", "FARMACÉUTICA SANFER S.A.C.", "Jr. Chinchón 830, San Isidro", "(01) 442-8900", "contacto@sanfer.com.pe" },
                { "20333344556", "LABORATORIOS HERSIL S.A.", "Av. Los Frutales 220, Ate", "(01) 713-3333", "atencionalcliente@hersil.com.pe" },
                { "20444455667", "TECNOFARMA S.A.", "Av. El Derby 055, Surco", "(01) 700-3000", "ventas@tecnofarma.com.pe" },
                { "20555566778", "QUÍMICA SUIZA S.A.C.", "Av. República de Panamá 2577, La Victoria", "(01) 211-4000", "farmacia@qs.com.pe" },
                { "20666677889", "LABORATORIOS PORTUGAL S.R.L.", "Calle Jacinto Ibáñez 315, Arequipa", "(054) 232-424", "ventas@portugal.com.pe" },
                { "20777788990", "AC FARMA S.A.", "Calle Las Pléyades 234, Chorrillos", "(01) 748-0000", "atencion@acfarma.com.pe" },
                { "20888899001", "GENOMMA LAB PERÚ S.A.", "Av. Manuel Olguín 325, Surco", "(01) 612-4000", "contacto@genommalab.com" },
                { "20999900112", "CORPORACIÓN INFARMASA S.A.", "Av. San Luis 2250, San Borja", "(01) 617-8000", "pedidos@infarmasa.com.pe" },
                { "20101020203", "DISTRIMED PERÚ S.A.C.", "Av. Colonial 1450, Lima", "(01) 561-2345", "ventas@distrimed.com.pe" }
        };

        for (String[] row : provData) {
            String ruc = row[0];
            if (lista.stream().noneMatch(p -> ruc.equals(p.getNumDocIdent()))) {
                Proveedor p = Proveedor.builder()
                        .tipoDocIdent("RUC")
                        .numDocIdent(ruc)
                        .razonSocial(row[1])
                        .direccion(row[2])
                        .telefono(row[3])
                        .email(row[4])
                        .estado(1)
                        .fechaRegistro(LocalDateTime.now().minusMonths(6))
                        .build();
                lista.add(proveedorRepository.save(p));
            }
        }
        return lista;
    }

    // ==========================================
    // 9. ÓRDENES DE COMPRA (Mínimo 20)
    // ==========================================
    private void seedOrdenesCompra(List<Proveedor> proveedores) {
        long count = ordenCompraRepository.count();
        if (count >= 20) return;

        for (int i = (int) count + 1; i <= 20; i++) {
            Proveedor prov = proveedores.get((i - 1) % proveedores.size());
            LocalDate fecha = LocalDate.of(2026, ((i % 11) + 1), ((i % 25) + 1));
            BigDecimal subtotal = new BigDecimal(1200 + (i * 150)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(igv);

            OrdenCompra oc = OrdenCompra.builder()
                    .numeroOrden(String.format("OC-2026-%04d", i))
                    .fechaEmision(fecha)
                    .fechaEstimadaEntrega(fecha.plusDays(7))
                    .tipoCompra((i % 2 == 0) ? "Insumos y Reactivos" : "Medicamentos Hospitalarios")
                    .centroCosto((i % 2 == 0) ? "CC-LAB" : "CC-FAR")
                    .areaSolicitante((i % 2 == 0) ? "Laboratorio Clínico" : "Farmacia Central")
                    .responsableCompra("admin")
                    .moneda("PEN")
                    .estado((i % 3 == 0) ? "Pendiente" : "Aprobado")
                    .proveedor(prov)
                    .subtotal(subtotal)
                    .totalIgv(igv)
                    .totalGeneral(total)
                    .observaciones("Orden generada para abastecimiento del periodo")
                    .build();
            ordenCompraRepository.save(oc);
        }
    }

    // ==========================================
    // 10. COMPRAS Y DETALLE (Mínimo 25)
    // ==========================================
    private void seedCompras(List<Proveedor> proveedores, List<Catalogo> servicios, List<Sucursal> sucursales) {
        long count = compraRepository.count();
        if (count >= 25) return;

        Long idSuc = sucursales.get(0).getIdSucursal();
        Random random = new Random(42);

        for (int i = (int) count + 1; i <= 25; i++) {
            int mes = ((i % 11) + 1);
            int dia = Math.min(28, (i * 3) % 28 + 1);
            LocalDate fecha = LocalDate.of(2026, mes, dia);
            Proveedor prov = proveedores.get((i - 1) % proveedores.size());

            BigDecimal subtotal = new BigDecimal(1500 + (i * 200)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal igv = subtotal.multiply(new BigDecimal("0.18")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal total = subtotal.add(igv);

            Compra c = Compra.builder()
                    .proveedor(prov)
                    .idSucursal(idSuc)
                    .tipoComprobante("FACTURA")
                    .serie("F001")
                    .correlativo(String.format("%06d", i + 100))
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

            Catalogo cat = servicios.get(i % servicios.size());
            DetalleCompra det = DetalleCompra.builder()
                    .compra(c)
                    .catalogo(cat)
                    .productoNombre(cat.getNombreServicio())
                    .cantidad(new BigDecimal(50 + (i * 5)))
                    .factorConversion(1)
                    .descripcion("Adquisición periódica de reactivos y medicamentos")
                    .build();

            c.getDetalles().add(det);
            compraRepository.save(c);
        }
    }

    // ==========================================
    // 11. VENTAS Y FACTURACIÓN (Mínimo 50, con >20 en Noviembre 2026)
    // ==========================================
    private List<VentaRegistro> seedVentas(List<Catalogo> servicios, List<Sucursal> sucursales, List<Punto> puntos, List<DatosPersonales> pacientes) {
        List<VentaRegistro> ventas = ventaRegistroRepository.findAll();
        Long idSuc = sucursales.get(0).getIdSucursal();
        Long idPunto = puntos.get(0).getPunto();

        int correlativoBoleta = 1;
        int correlativoFactura = 1;
        int correlativoTicket = 1;

        // Si hay menos de 50 ventas en total, poblar ventas 2026 completas
        if (ventas.size() < 50) {
            // Histórico Enero - Octubre (20 ventas)
            for (int i = 1; i <= 20; i++) {
                int mes = ((i % 10) + 1);
                LocalDate fecha = LocalDate.of(2026, mes, Math.min(28, (i * 2) + 1));
                DatosPersonales pac = pacientes.get(i % pacientes.size());
                Catalogo cat = servicios.get(i % servicios.size());

                BigDecimal total = cat.getTarifa();
                BigDecimal baseImp = total.divide(new BigDecimal("1.18"), 4, RoundingMode.HALF_UP);
                BigDecimal igv = total.subtract(baseImp);

                String tipoDoc = (i % 2 == 0) ? "F" : "B";
                String serie = tipoDoc.equals("F") ? "F001" : "B001";
                String numdoc = String.format("%08d", tipoDoc.equals("F") ? correlativoFactura++ : correlativoBoleta++);

                VentaRegistro v = VentaRegistro.builder()
                        .idSucursal(idSuc)
                        .punto(idPunto)
                        .fecha(fecha)
                        .tipoDoc(tipoDoc)
                        .serie(serie)
                        .numdoc(numdoc)
                        .nombrePac(pac.getVerNombre())
                        .nroDni(pac.getNumdoc())
                        .tipoPac(tipoDoc.equals("F") ? "C" : "P")
                        .tipoAtencion((i % 3 == 0) ? "CONVENIO" : "PARTICULAR")
                        .baseImp(baseImp)
                        .igv(igv)
                        .total(total)
                        .valorVenta(baseImp)
                        .valorAfecto(baseImp)
                        .estado(1)
                        .modo("N")
                        .idPersonalUser("1")
                        .idCtacte(pac.getId())
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
                ventas.add(ventaRegistroRepository.save(v));
            }

            // Ventas Masivas en Noviembre 2026 (25 ventas)
            for (int dia = 1; dia <= 25; dia++) {
                LocalDate fecha = LocalDate.of(2026, 11, dia);
                DatosPersonales pac = pacientes.get((dia - 1) % pacientes.size());
                Catalogo cat = servicios.get(dia % servicios.size());

                BigDecimal total = cat.getTarifa();
                BigDecimal baseImp = total.divide(new BigDecimal("1.18"), 4, RoundingMode.HALF_UP);
                BigDecimal igv = total.subtract(baseImp);

                String tipoDoc;
                String serie;
                String numdoc;

                if (dia % 4 == 0) {
                    tipoDoc = "F";
                    serie = "F001";
                    numdoc = String.format("%08d", correlativoFactura++);
                } else if (dia % 6 == 0) {
                    tipoDoc = "T";
                    serie = "T001";
                    numdoc = String.format("%08d", correlativoTicket++);
                } else {
                    tipoDoc = "B";
                    serie = "B001";
                    numdoc = String.format("%08d", correlativoBoleta++);
                }

                int estado = (dia == 9 || dia == 19) ? 10 : 1; // 2 anulados

                VentaRegistro v = VentaRegistro.builder()
                        .idSucursal(idSuc)
                        .punto(idPunto)
                        .fecha(fecha)
                        .tipoDoc(tipoDoc)
                        .serie(serie)
                        .numdoc(numdoc)
                        .nombrePac(pac.getVerNombre())
                        .nroDni(pac.getNumdoc())
                        .tipoPac(tipoDoc.equals("F") ? "C" : "P")
                        .tipoAtencion((dia % 3 == 0) ? "CONVENIO" : "PARTICULAR")
                        .baseImp(baseImp)
                        .igv(igv)
                        .total(total)
                        .valorVenta(baseImp)
                        .valorAfecto(baseImp)
                        .estado(estado)
                        .modo("N")
                        .idPersonalUser("1")
                        .idCtacte(pac.getId())
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
                ventas.add(ventaRegistroRepository.save(v));
            }
        }
        return ventas;
    }

    // ==========================================
    // 12. SOLICITUDES DE ANULACIÓN (Mínimo 20)
    // ==========================================
    private void seedSolicitudesAnulacion(List<VentaRegistro> ventas, List<Sucursal> sucursales) {
        long count = solicitudAnulacionRepository.count();
        if (count >= 20 || ventas.isEmpty()) return;

        Long idSuc = sucursales.get(0).getIdSucursal();
        String[] motivos = {
                "Error en tipo de documento solicitado por el paciente",
                "Duplicidad de emisión por fallo de conexión en terminal",
                "Cambio de medio de pago de efectivo a tarjeta de crédito",
                "Paciente canceló la consulta por demora en el turno",
                "Error en datos de RUC ingresados en la factura",
                "Orden médica modificada por el especialista tratante",
                "Cobro indebido de examen no cubierto en el plan"
        };
        String[] estados = { "PENDIENTE", "APROBADO", "RECHAZADO" };

        for (int i = (int) count + 1; i <= 20; i++) {
            VentaRegistro v = ventas.get((i - 1) % ventas.size());
            SolicitudAnulacion sa = SolicitudAnulacion.builder()
                    .idVenta(v.getIdVenta())
                    .idSucursal(idSuc)
                    .comprobante(v.getSerie() + "-" + v.getNumdoc())
                    .fechaVenta(v.getFecha())
                    .montoVenta(v.getTotal())
                    .motivoSolicitud(motivos[(i - 1) % motivos.length])
                    .estado(estados[(i - 1) % estados.length])
                    .idSolicitante(1L)
                    .nombreSolicitante("Cajera Turno Mañana")
                    .idAutorizador(estados[(i - 1) % estados.length].equals("PENDIENTE") ? null : 1L)
                    .nombreAutorizador(estados[(i - 1) % estados.length].equals("PENDIENTE") ? null : "Administrador Sistema")
                    .fechaResolucion(estados[(i - 1) % estados.length].equals("PENDIENTE") ? null : LocalDateTime.now().minusHours(i))
                    .build();
            solicitudAnulacionRepository.save(sa);
        }
    }

    // ==========================================
    // 13. NOTAS DE CRÉDITO (Mínimo 20)
    // ==========================================
    private void seedNotasCredito(List<VentaRegistro> ventas, List<Sucursal> sucursales) {
        long count = notaCreditoRepository.count();
        if (count >= 20 || ventas.isEmpty()) return;

        Long idSuc = sucursales.get(0).getIdSucursal();

        for (int i = (int) count + 1; i <= 20; i++) {
            VentaRegistro v = ventas.get((i - 1) % ventas.size());
            NotaCredito nc = NotaCredito.builder()
                    .serieNc("NC01")
                    .numdocNc(String.format("%08d", i))
                    .idVentaRef(v.getIdVenta())
                    .comprobanteRef(v.getSerie() + "-" + v.getNumdoc())
                    .tipoComprobante("F".equals(v.getTipoDoc()) ? "FACTURA" : "BOLETA")
                    .motivoCodigo((i % 2 == 0) ? "01" : "06")
                    .motivoDescripcion((i % 2 == 0) ? "Anulación de la operación" : "Devolución total")
                    .detalle("Nota de crédito emitida por anulación conforme auditoría")
                    .montoNc(v.getTotal())
                    .montoOriginal(v.getTotal())
                    .paciente(v.getNombrePac())
                    .idSucursal(idSuc)
                    .idUsuario(1L)
                    .nombreUsuario("admin")
                    .fechaEmision(v.getFecha())
                    .estado("EMITIDA")
                    .build();
            notaCreditoRepository.save(nc);
        }
    }

    // ==========================================
    // 14. DESCUENTOS AUTORIZADOS (Mínimo 20)
    // ==========================================
    private void seedDescuentos(List<DatosPersonales> pacientes) {
        long count = autorizacionDescuentoRepository.count();
        if (count >= 20) return;

        String[] doctores = { "DR. JUAN RAMOS", "DRA. SOFIA VALDIVIA", "DR. ALBERTO FARFAN", "DRA. MONICA SUAREZ", "DR. CARLOS ALARCON" };
        String[] motivos = { "Convenio Adulto Mayor", "Campaña Salud Ocupacional", "Descuento Familiar", "Paciente en situación vulnerable", "Cortesía primera atención médica" };
        String[] estados = { "APROBADO", "PENDIENTE", "RECHAZADO" };

        for (int i = (int) count + 1; i <= 20; i++) {
            DatosPersonales pac = pacientes.get((i - 1) % pacientes.size());
            BigDecimal orig = new BigDecimal(80 + (i * 10)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal desc = new BigDecimal(15 + (i * 2)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal fin = orig.subtract(desc);

            AutorizacionDescuento ad = AutorizacionDescuento.builder()
                    .pacienteNombre(pac.getVerNombre())
                    .pacienteDni(pac.getNumdoc())
                    .doctorNombre(doctores[(i - 1) % doctores.length])
                    .montoOriginal(orig)
                    .montoDescuento(desc)
                    .montoFinal(fin)
                    .motivoSolicitud(motivos[(i - 1) % motivos.length])
                    .estado(estados[(i - 1) % estados.length])
                    .build();
            autorizacionDescuentoRepository.save(ad);
        }
    }

    // ==========================================
    // 15. HONORARIOS MÉDICOS (Mínimo 20)
    // ==========================================
    private void seedHonorarios() {
        long count = pagoComprobanteRepository.findAll().stream().filter(p -> "HONORARIOS".equals(p.getTipoPago())).count();
        if (count >= 20) return;

        String[][] medicos = {
                { "DR. JUAN CARLOS RAMOS DIAZ", "41829012", "Cardiología", "4800.00" },
                { "DRA. SOFÍA VALDIVIA ROMERO", "42918203", "Pediatría", "3900.00" },
                { "DR. ALBERTO FARFÁN CASTRO", "40192834", "Traumatología", "5200.00" },
                { "DRA. MONICA SUAREZ VEGA", "45019283", "Ginecología", "4200.00" },
                { "DR. EDUARDO SILVA PAREDES", "43019284", "Gastroenterología", "4600.00" },
                { "DRA. CARMEN BENAVIDES LUNA", "44102938", "Dermatología", "3800.00" },
                { "DR. MANUEL ROJAS VARGAS", "40918273", "Oftalmología", "4500.00" },
                { "DRA. PATRICIA ALVAREZ CHAVEZ", "42819203", "Neurología", "5100.00" },
                { "DR. GONZALO CASTILLO MORAN", "41928374", "Urología", "4700.00" },
                { "DRA. ROSAURA MENDOZA BRAVO", "43829104", "Endocrinología", "4100.00" },
                { "DR. JORGE MEZA PALOMINO", "45102938", "Otorrinolaringología", "4300.00" },
                { "DRA. GABRIELA RUIZ SOLIS", "40291823", "Medicina Física", "3600.00" },
                { "DR. CESAR CORDOVA NUNEZ", "44918203", "Neumología", "4400.00" },
                { "DRA. ELENA BUSTAMANTE PAZ", "41029384", "Psiquiatría", "4000.00" },
                { "DR. RAFAEL QUISPE CONDORI", "42819302", "Reumatología", "4250.00" },
                { "DRA. LUCIA ESPINOZA SOTO", "43918274", "Geriatría", "3950.00" },
                { "DR. MARCO ANTONIO VEGA", "40819283", "Cirugía General", "5400.00" },
                { "DRA. BEATRIZ CAMPOS RIVERA", "45192834", "Oncología", "5600.00" },
                { "DR. HUGO MORALES DELGADO", "42019283", "Medicina Interna", "4300.00" },
                { "DRA. DIANA HERRERA VILCHEZ", "43102938", "Infectología", "4150.00" }
        };

        LocalDate hoy = LocalDate.of(2026, 11, 15);

        for (int i = (int) count; i < medicos.length; i++) {
            String[] m = medicos[i];
            BigDecimal bruto = new BigDecimal(m[3]);
            BigDecimal ret = bruto.multiply(new BigDecimal("0.08")).setScale(2, RoundingMode.HALF_UP);
            BigDecimal neto = bruto.subtract(ret);

            PagoComprobante pc = PagoComprobante.builder()
                    .tipoPago("HONORARIOS")
                    .tipoBeneficiario("MEDICO")
                    .nombreRazonSocial(m[0])
                    .numeroDocumento(m[1])
                    .serieComprobante("E001")
                    .numeroRecibo(String.format("%06d", i + 101))
                    .areaServicio(m[2])
                    .montoBruto(bruto)
                    .retencion(ret)
                    .montoNeto(neto)
                    .fechaEmision(hoy.minusDays((i % 10) + 1))
                    .fechaPago(hoy)
                    .periodoMes("NOVIEMBRE")
                    .periodoAnio(2026)
                    .conceptoPago("Honorarios Médicos Consultas y Procedimientos Quirúrgicos")
                    .estadoPago("PAGADO")
                    .build();
            pagoComprobanteRepository.save(pc);
        }
    }

    // ==========================================
    // 16. PAGOS DE PERSONAL / PLANILLA (Mínimo 20)
    // ==========================================
    private void seedPagosPersonal() {
        long count = pagoComprobanteRepository.findAll().stream().filter(p -> "PLANILLA".equals(p.getTipoPago())).count();
        if (count >= 20) return;

        String[][] personal = {
                { "LIC. CARMEN ROSA DAVILA", "43920192", "Enfermería", "Enfermera Jefa", "3200.00" },
                { "TEC. MARIO BENAVIDES LOPEZ", "44819203", "Laboratorio", "Tecnólogo Médico Principal", "2600.00" },
                { "SRTA. LUCIA ESPINOZA CAMPOS", "71293845", "Recepción y Admisión", "Cajera Principal", "1900.00" },
                { "ING. JORGE ALARCON MEZA", "40928374", "Tecnologías de la Información", "Administrador de Sistemas", "3800.00" },
                { "LIC. TERESA VILCHEZ RIOS", "42819203", "Enfermería", "Licenciada en Enfermería", "2700.00" },
                { "TEC. WALTER ROJAS SALAS", "41029384", "Diagnóstico por Imágenes", "Técnico Radiólogo", "2500.00" },
                { "SRA. GLADYS SOTO CASTRO", "40192834", "Farmacia Central", "Técnica en Farmacia", "2100.00" },
                { "SR. OSCAR HUAMAN NUNEZ", "43829104", "Seguridad y Vigilancia", "Supervisor de Seguridad", "2000.00" },
                { "LIC. MONICA RIVERA PAZ", "45019283", "Recursos Humanos", "Coordinadora de RRHH", "3400.00" },
                { "CPC. ROBERTO VARGAS LUNA", "41928374", "Contabilidad y Finanzas", "Contador General", "4200.00" },
                { "SRTA. PAOLA BENITEZ BRAVO", "72819203", "Admisión Ambulatoria", "Recepcionista Turno Tarde", "1850.00" },
                { "SR. MIGUEL ANGEL MORALES", "42019283", "Mantenimiento General", "Técnico Electricista", "2200.00" },
                { "LIC. ANDREA PAREDES LEON", "44918273", "Laboratorio Clínico", "Bióloga Microbióloga", "3100.00" },
                { "TEC. CESAR BUSTAMANTE", "40819203", "Farmacia Central", "Asistente de Farmacia", "1950.00" },
                { "SRA. JULIA CASTILLO DIAZ", "43102938", "Servicios Generales", "Supervisora de Limpieza", "1800.00" },
                { "LIC. RICARDO QUISPE SOTO", "41526374", "Nutrición Hospitalaria", "Licenciado Nutricionista", "2800.00" },
                { "CPC. SILVIA MORAN CHAVEZ", "42819302", "Tesorería", "Encargada de Tesorería", "3300.00" },
                { "SRTA. VALENTINA SOLIS", "73829104", "Caja Ambulatoria", "Cajera Turno Mañana", "1850.00" },
                { "SR. HERNAN ORTEGA MEZA", "40291823", "Logística y Almacén", "Jefe de Almacén Central", "2900.00" },
                { "LIC. BEATRIZ NUNEZ VEGA", "45102938", "Trabajo Social", "Asistenta Social", "2600.00" }
        };

        LocalDate hoy = LocalDate.of(2026, 11, 15);

        for (int i = (int) count; i < personal.length; i++) {
            String[] p = personal[i];
            BigDecimal monto = new BigDecimal(p[4]);

            PagoComprobante pp = PagoComprobante.builder()
                    .tipoPago("PLANILLA")
                    .tipoBeneficiario("TRABAJADOR")
                    .nombreRazonSocial(p[0])
                    .numeroDocumento(p[1])
                    .areaTrabajador(p[2])
                    .cargoTrabajador(p[3])
                    .montoBruto(monto)
                    .montoNeto(monto)
                    .fechaEmision(hoy.minusDays((i % 5) + 1))
                    .fechaPago(hoy)
                    .periodoMes("NOVIEMBRE")
                    .periodoAnio(2026)
                    .conceptoPago("Remuneración Mensual Personal de Sede")
                    .estadoPago("PAGADO")
                    .build();
            pagoComprobanteRepository.save(pp);
        }
    }

    // ==========================================
    // 17. OTROS PAGOS (Mínimo 20)
    // ==========================================
    private void seedOtrosPagos() {
        long count = pagoComprobanteRepository.findAll().stream().filter(p -> "OTROS".equals(p.getTipoPago())).count();
        if (count >= 20) return;

        String[][] gastos = {
                { "ENEL DISTRIBUCIÓN PERÚ S.A.A.", "20269985900", "Servicio de Suministro Eléctrico Sede Central", "4200.00" },
                { "SEDAPAL S.A.", "20100152356", "Servicio de Agua Potable y Alcantarillado", "1850.00" },
                { "AMÉRICA MÓVIL PERÚ S.A.C. (CLARO)", "20467534026", "Internet Fibra Óptica Dedicado y Enlaces", "2400.00" },
                { "INMOBILIARIA PARDO S.A.C.", "20512345678", "Alquiler Mensual Local Sede Miraflores", "9500.00" },
                { "INVERSIONES JAVIER PRADO S.A.", "20523456789", "Alquiler Mensual Local Sede San Isidro", "11000.00" },
                { "SCHINDLER DEL PERÚ S.A.", "20100234567", "Mantenimiento Preventivo de Ascensores", "1600.00" },
                { "SECURITAS PERÚ S.A.", "20345678901", "Servicio de Seguridad y Resguardo 24/7", "8400.00" },
                { "BEFESA PERÚ S.A.", "20256789012", "Recojo y Disposición de Residuos Biocontaminados", "2100.00" },
                { "RÍMAC SEGUROS Y REASEGUROS", "20100041953", "Póliza Multirriesgo y Contra Incendio Sedes", "5300.00" },
                { "MICROSOFT PERÚ S.R.L.", "20251234567", "Licenciamiento Microsoft 365 y Azure Backup", "3100.00" },
                { "LIMPIEZA HOSPITALARIA INTEGRAL S.A.C.", "20456789012", "Servicio de Limpieza y Desinfección Clínica", "4900.00" },
                { "OXÍGENOS DEL PERÚ S.A. (LINDE)", "20100085614", "Recarga de Balones y Oxígeno Líquido Medicinal", "3700.00" },
                { "GAS NATURAL DE LIMA Y CALLAO (CÁLIDDA)", "20503758114", "Suministro de Gas Natural para Calentadores", "1250.00" },
                { "TELEFÓNICA DEL PERÚ S.A.A.", "20100017491", "Centralita IP Telefónica y Móviles Flota", "2150.00" },
                { "PROTECCIÓN CONTRA INCENDIOS S.A.C.", "20367890123", "Recarga y Certificación de Extintores Sedes", "980.00" },
                { "CLIMA MEDICAL S.A.C.", "20478901234", "Mantenimiento Aire Acondicionado Flujo Laminar", "2850.00" },
                { "ESTUDIO JURÍDICO VALDIVIA & ASOCIADOS", "20589012345", "Asesoría Legal Laboral y Regulatoria Susalud", "4500.00" },
                { "PRICEWATERHOUSECOOPERS S.C.R.L.", "20100128456", "Honorarios de Auditoría Externa Financiera", "8000.00" },
                { "COURIER MÉDICO EXPRÉS S.A.C.", "20690123456", "Transporte de Muestras Biológicas Refrigeradas", "1450.00" },
                { "REPROGRAFÍA DIGITAL PERÚ S.A.", "20501234987", "Impresión de Formatos Médicos y Boletas de Pago", "890.00" }
        };

        LocalDate hoy = LocalDate.of(2026, 11, 15);

        for (int i = (int) count; i < gastos.length; i++) {
            String[] g = gastos[i];
            BigDecimal monto = new BigDecimal(g[3]);

            PagoComprobante po = PagoComprobante.builder()
                    .tipoPago("OTROS")
                    .tipoBeneficiario("PROVEEDOR")
                    .nombreRazonSocial(g[0])
                    .numeroDocumento(g[1])
                    .serieComprobante("F001")
                    .numeroRecibo(String.format("%06d", i + 501))
                    .areaServicio("Administración General")
                    .montoBruto(monto)
                    .retencion(BigDecimal.ZERO)
                    .montoNeto(monto)
                    .fechaEmision(hoy.minusDays((i % 12) + 1))
                    .fechaPago(hoy)
                    .periodoMes("NOVIEMBRE")
                    .periodoAnio(2026)
                    .conceptoPago(g[2])
                    .estadoPago("PAGADO")
                    .build();
            pagoComprobanteRepository.save(po);
        }
    }

    // ==========================================
    // 18. CAJA CHICA Y MOVIMIENTOS (Mínimo 20)
    // ==========================================
    private void seedCajaChica(Sucursal sucursal) {
        Long idSuc = sucursal.getIdSucursal();
        CajaChica cc = cajaChicaRepository.findAll().stream().findFirst().orElseGet(() -> {
            CajaChica nueva = CajaChica.builder()
                    .nombre("CAJA CHICA SEDE PRINCIPAL")
                    .idSucursal(idSuc)
                    .saldoInicial(new BigDecimal("2500.00"))
                    .saldoActual(new BigDecimal("1850.00"))
                    .estado(CajaChica.EstadoCaja.ABIERTA)
                    .usuarioCreacion("admin")
                    .build();
            return cajaChicaRepository.save(nueva);
        });

        long count = cajaChicaMovimientoRepository.count();
        if (count >= 20) return;

        MetodoPago efectivo = metodoPagoRepository.findByDescripcionIgnoreCase("EFECTIVO")
                .orElseGet(() -> metodoPagoRepository.findAll().stream().findFirst().orElse(null));

        Object[][] movs = {
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "45.00", "Compra de papel bond y útiles de oficina", "B002-341" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "85.00", "Artículos de limpieza y desinfección", "F001-9872" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "150.00", "Movilidad y mensajería médica urgente", "REC-012" },
                { CajaChicaMovimiento.TipoMovimiento.INGRESO, "800.00", "Reembolso parcial fondo fijo cheque gerencia", "CHQ-8902" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "65.00", "Refrigerio para personal médico de guardia", "B003-891" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "120.00", "Compra de toner negro para impresora de recepción", "F002-1240" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "35.00", "Gastos de envío de muestras laboratorio Callao", "REC-034" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "95.00", "Reparación de cerradura consultorio 4", "B001-789" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "40.00", "Compra de pilas y baterías para tensiómetros", "B005-120" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "180.00", "Pago de delivery de medicamentos urgentes", "F004-981" },
                { CajaChicaMovimiento.TipoMovimiento.INGRESO, "500.00", "Aporte por reintegro de fondo de caja chica", "DEP-1209" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "55.00", "Compra de alcohol en gel y jabón líquido", "B002-990" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "75.00", "Duplicado de llaves de almacén y farmacia", "REC-056" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "110.00", "Compra de guantes quirúrgicos de emergencia", "B001-654" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "30.00", "Movilidad local toma de muestras a domicilio", "REC-078" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "88.00", "Cables HDMI y accesorios para monitor de triaje", "F003-452" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "42.00", "Agua mineral bidones para sala de espera", "B004-321" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "130.00", "Mantenimiento urgente chapa eléctrica de acceso", "F001-432" },
                { CajaChicaMovimiento.TipoMovimiento.EGRESO, "60.00", "Envío de correspondencia notarial certificada", "REC-099" },
                { CajaChicaMovimiento.TipoMovimiento.INGRESO, "600.00", "Reposición mensual de caja chica aprobada", "CHQ-9102" }
        };

        for (int i = (int) count; i < movs.length; i++) {
            Object[] m = movs[i];
            CajaChicaMovimiento mov = CajaChicaMovimiento.builder()
                    .cajaChica(cc)
                    .tipo((CajaChicaMovimiento.TipoMovimiento) m[0])
                    .monto(new BigDecimal((String) m[1]))
                    .descripcion((String) m[2])
                    .referencia((String) m[3])
                    .metodoPago(efectivo)
                    .fecha(LocalDateTime.now().minusDays(20 - i))
                    .build();
            cajaChicaMovimientoRepository.save(mov);
        }
    }

    // ==========================================
    // 19. CAJA GENERAL
    // ==========================================
    private void seedCajaGeneral(Sucursal sucursal) {
        if (cajaGeneralRepository.count() >= 1) return;

        Long idSuc = sucursal.getIdSucursal();
        CajaGeneral cg = CajaGeneral.builder()
                .nombre("CAJA GENERAL BANCARIA Y DE SEGURIDAD")
                .idSucursal(idSuc)
                .saldoActual(new BigDecimal("45000.00"))
                .usuarioCreacion("admin")
                .build();
        cajaGeneralRepository.save(cg);
    }

    // ==========================================
    // 20. ARQUEOS DE CAJA (Mínimo 20)
    // ==========================================
    private void seedArqueos(List<Sucursal> sucursales, List<Punto> puntos) {
        long count = arqueoCajaRepository.count();
        if (count >= 20) return;

        Long idSuc = sucursales.get(0).getIdSucursal();
        Long idPunto = puntos.get(0).getPunto();

        for (int i = (int) count + 1; i <= 20; i++) {
            BigDecimal saldo = new BigDecimal(2200 + (i * 120)).setScale(2, RoundingMode.HALF_UP);
            String turno = (i % 2 == 0) ? "Turno Tarde (14:00 - 21:00)" : "Turno Mañana (07:00 - 14:00)";
            ArqueoCaja a = ArqueoCaja.builder()
                    .fechaHora(LocalDateTime.now().minusDays(21 - i).withHour((i % 2 == 0) ? 21 : 14).withMinute(15))
                    .idUsuario(1L)
                    .idSucursal(idSuc)
                    .idPunto(idPunto)
                    .saldoEsperado(saldo)
                    .saldoFisico(saldo)
                    .diferencia(BigDecimal.ZERO)
                    .observaciones("Cierre de caja conforme " + turno + ". Cuadre al 100% verificado.")
                    .estado("CERRADO")
                    .build();
            arqueoCajaRepository.save(a);
        }
    }

    // ==========================================
    // 21. CUENTAS CORRIENTES BANCARIAS (Mínimo 20)
    // ==========================================
    private void seedCuentasCorrientes() {
        long count = cuentaCorrienteRepository.count();
        if (count >= 20) return;

        String[][] cuentas = {
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANCO DE CRÉDITO DEL PERÚ (BCP)", "Corriente", "193-22849201-0-12", "002-193-002284920101-12", "PEN", "BCP Soles Operaciones", "104101", "48500.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANCO DE CRÉDITO DEL PERÚ (BCP)", "Corriente", "193-99882211-1-45", "002-193-009988221114-45", "USD", "BCP Dólares Inversiones", "104102", "15200.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BBVA PERÚ", "Corriente", "0011-0182-0100034567", "011-182-000100034567-23", "PEN", "BBVA Recaudación Pacientes", "104103", "36400.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BBVA PERÚ", "Corriente", "0011-0182-0100098765", "011-182-000100098765-88", "USD", "BBVA Dólares Proveedores", "104104", "8900.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "INTERBANK", "Corriente", "200-3001234567", "003-200-003001234567-15", "PEN", "Interbank Cobranza Web", "104105", "29100.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "SCOTIABANK PERÚ", "Corriente", "000-4567890", "009-000-000045678901-44", "PEN", "Scotiabank Planillas", "104106", "18300.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANBIF", "Corriente", "001-0987654", "038-001-000098765401-12", "PEN", "BanBif Convenios Aseguradoras", "104107", "42000.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANCO PICHINCHA", "Ahorros", "010-3456789", "018-010-000034567890-33", "PEN", "Pichincha Fondo Reserva", "104108", "12500.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANCO DE LA NACIÓN", "Corriente", "00-000-123456", "018-000-000000123456-89", "PEN", "Detracciones SUNAT", "104109", "9800.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BCP SEDE SAN ISIDRO", "Corriente", "193-45678901-0-22", "002-193-004567890102-22", "PEN", "Caja San Isidro", "104110", "14200.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BCP SEDE MIRAFLORES", "Corriente", "193-56789012-0-33", "002-193-005678901203-33", "PEN", "Caja Miraflores", "104111", "16500.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BBVA SEDE SURCO", "Corriente", "0011-0245-0100011223", "011-245-000100011223-45", "PEN", "Caja Surco Chacarilla", "104112", "19800.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "INTERBANK SEDE OLIVOS", "Corriente", "200-4009876543", "003-200-004009876543-78", "PEN", "Caja Los Olivos", "104113", "13400.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "SCOTIABANK SAN BORJA", "Corriente", "000-8901234", "009-000-000089012341-99", "PEN", "Caja San Borja", "104114", "15100.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANBIF LA MOLINA", "Corriente", "001-5678901", "038-001-000056789011-55", "PEN", "Caja La Molina", "104115", "17200.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BCP CALLAO", "Corriente", "193-67890123-0-44", "002-193-006789012304-44", "PEN", "Caja Callao Bellavista", "104116", "11900.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "INTERBANK SAN MIGUEL", "Corriente", "200-5001239876", "003-200-005001239876-22", "PEN", "Caja San Miguel", "104117", "12800.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BBVA MAGDALENA", "Corriente", "0011-0356-0100055443", "011-356-000100055443-66", "PEN", "Caja Magdalena", "104118", "10500.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BCP JESÚS MARÍA", "Corriente", "193-78901234-0-55", "002-193-007890123405-55", "PEN", "Caja Jesús María", "104119", "13900.00" },
                { "CLÍNICA FINANZAS EPE S.A.C.", "BANCO GNB PERÚ", "Corriente", "002-12345678", "023-002-000123456789-11", "PEN", "GNB Fondo Garantía", "104120", "25000.00" }
        };

        for (int i = (int) count; i < cuentas.length; i++) {
            String[] c = cuentas[i];
            CuentaCorriente cc = CuentaCorriente.builder()
                    .nombreTitular(c[0])
                    .banco(c[1])
                    .tipoCuenta(c[2])
                    .numeroCuenta(c[3])
                    .cci(c[4])
                    .moneda(c[5])
                    .alias(c[6])
                    .cuentaContable(c[7])
                    .saldo(new BigDecimal(c[8]))
                    .estado("A")
                    .build();
            cuentaCorrienteRepository.save(cc);
        }
    }

    // ==========================================
    // 22. KARDEX DE INVENTARIO (Mínimo 25)
    // ==========================================
    private void seedKardex(List<Producto> productos, List<Catalogo> servicios, Sucursal sucursal) {
        long count = kardexRepository.count();
        if (count >= 25 || productos.isEmpty() || servicios.isEmpty()) return;

        Long idSuc = sucursal.getIdSucursal();

        for (int i = (int) count + 1; i <= 25; i++) {
            Producto p = productos.get((i - 1) % productos.size());
            Catalogo c = servicios.stream()
                    .filter(cat -> cat.getId().equals(p.getIdCatalogo()))
                    .findFirst()
                    .orElse(servicios.get(0));

            boolean esIngreso = (i % 3 != 0);
            BigDecimal cant = new BigDecimal((i % 5 + 1) * 20);
            BigDecimal costoUnit = c.getPrecioSinIgv();
            BigDecimal costoTot = costoUnit.multiply(cant);

            Kardex k = Kardex.builder()
                    .idArticuloKardex(UUID.randomUUID().toString())
                    .idAlmArticulo(p.getIdProducto())
                    .idCatalogo(c.getId())
                    .idSucursal(idSuc)
                    .idUsuario(1L)
                    .operacion(esIngreso ? "COMPRA_INGRESO" : "VENTA_SALIDA")
                    .signo(esIngreso ? "+" : "-")
                    .cantidad(cant)
                    .costoUnitario(costoUnit)
                    .costoTotal(costoTot)
                    .saldoCantidad(p.getStockReal())
                    .saldoCostoUnitario(costoUnit)
                    .saldoCostoTotal(costoUnit.multiply(p.getStockReal()))
                    .fecha(LocalDateTime.now().minusDays(26 - i))
                    .detalle(esIngreso ? "Ingreso por factura de compra F001-" + (100 + i) : "Salida por atención médica y dispensación")
                    .numDoc(esIngreso ? "FAC-001-" + String.format("%06d", 100 + i) : "BOL-001-" + String.format("%06d", 200 + i))
                    .build();
            kardexRepository.save(k);
        }
    }

    // ==========================================
    // 23. PAQUETES PREVENTIVOS (Mínimo 20)
    // ==========================================
    private void seedPaquetes() {
        long count = paqueteRepository.count();
        if (count >= 20) return;

        String[][] paqs = {
                { "PAQ001", "CHEQUEO CLÍNICO INTEGRAL PREVENTIVO", "Consulta Medicina General + Hemograma + Glucosa + Perfil Lipídico", "180.00" },
                { "PAQ002", "PERFIL EJECUTIVO CARDIOVASCULAR", "Consulta Especializada Cardiología + Perfil Lipídico Completo + EKG", "240.00" },
                { "PAQ003", "PAQUETE CONTROL MATERNO - PRENATAL", "Ecografía Obstétrica + Hemograma Completo + Examen de Orina", "210.00" },
                { "PAQ004", "PAQUETE PEDIÁTRICO SALUD TOTAL", "Consulta Pediatría + Hemograma + Examen Completo de Orina + Parásitos", "160.00" },
                { "PAQ005", "PERFIL ONCOLÓGICO PREVENTIVO MUJER", "Consulta Ginecología + Papanicolaou + Ecografía Mamaria", "280.00" },
                { "PAQ006", "PERFIL UROLÓGICO PREVENTIVO VARÓN", "Consulta Urología + Antígeno Prostático PSA + Ecografía Renal", "260.00" },
                { "PAQ007", "CHEQUEO SALUD OCUPACIONAL BÁSICO", "Audiometría + Espirometría + Rayos X de Tórax + EKG", "220.00" },
                { "PAQ008", "PERFIL TIROIDEO INTEGRAL", "Dosaje TSH + T3 + T4 Libre + Ecografía de Tiroides", "250.00" },
                { "PAQ009", "CHEQUEO GERIÁTRICO DORADO", "Consulta Geriatría + Perfil Hepático + Perfil Lipídico + Glucosa", "230.00" },
                { "PAQ010", "PAQUETE CONTROL DIABETES PLUS", "Glucosa en Sangre + Hemoglobina Glicosilada + Microalbuminuria", "140.00" },
                { "PAQ011", "PERFIL HEPÁTICO Y DIGESTIVO", "Perfil Hepático Completo + Ecografía Abdominal Superior", "210.00" },
                { "PAQ012", "CHEQUEO DENTAL INTEGRAL", "Odontograma + Profilaxis Ultrasónica + Fluorización + Curación", "120.00" },
                { "PAQ013", "PERFIL DERMATOLÓGICO Y LUNARES", "Consulta Dermatología + Mapeo de Lunares + Crioterapia Lesión", "190.00" },
                { "PAQ014", "PAQUETE POST-COVID RESPIRATORIO", "Tomografía de Tórax + Espirometría + Consulta Neumología", "450.00" },
                { "PAQ015", "PERFIL REUMATOLÓGICO Y ARTICULAR", "Factor Reumatoideo + Ácido Úrico + PCR Cuantitativo", "175.00" },
                { "PAQ016", "CHEQUEO OFTALMOLÓGICO COMPLETO", "Medida de la Vista Computarizada + Fondo de Ojo + Tonometría", "90.00" },
                { "PAQ017", "PAQUETE NUTRICIONAL Y METABÓLICO", "Bioimpedancia Corporal + Consulta Nutrición + Perfil Lipídico", "150.00" },
                { "PAQ018", "PERFIL ANEMIA Y NUTRICIÓN INFANTIL", "Hemoglobina + Ferritina Sérica + Hierro Sérico + Consulta", "115.00" },
                { "PAQ019", "PAQUETE CONTROL HIPERTENSIÓN", "Consulta Cardiología + MAPA Holter Presión 24h + EKG", "320.00" },
                { "PAQ020", "CHEQUEO PRE-OPERATORIO COMPLETO", "Riesgo Quirúrgico + EKG + Hemograma + Coagulograma + Rayos X", "290.00" }
        };

        for (int i = (int) count; i < paqs.length; i++) {
            String[] p = paqs[i];
            Paquete paq = Paquete.builder()
                    .codigo(p[0])
                    .nombre(p[1])
                    .descripcion(p[2])
                    .precioPaquete(new BigDecimal(p[3]))
                    .categoria("PAQUETE_REGULAR")
                    .estado("1")
                    .fechaInicio(LocalDate.of(2026, 1, 1))
                    .fechaFin(LocalDate.of(2026, 12, 31))
                    .build();
            paqueteRepository.save(paq);
        }
    }

    // ==========================================
    // 24. EMPRESAS CLIENTES CORPORATIVAS (Mínimo 20)
    // ==========================================
    private void seedEmpresas() {
        long count = empresaRepository.count();
        if (count >= 20) return;

        String[][] empresas = {
                { "COMPAÑÍA MINERA ANTAMINA S.A.", "20389230724", "Av. El Derby 055, Surco", "(01) 211-8000", "ANTAMINA" },
                { "FERREYROS S.A.", "20100028698", "Jr. Cristóbal de Peralta 820, Surco", "(01) 626-4000", "FERREYROS" },
                { "ALICORP S.A.A.", "20100055237", "Av. Argentina 4793, Carmen de la Legua, Callao", "(01) 315-0800", "ALICORP" },
                { "AENZA S.A.A. (GRAÑA Y MONTERO)", "20100100108", "Av. Petit Thouars 4957, Miraflores", "(01) 213-6565", "AENZA" },
                { "LECHE GLORIA S.A.", "20100190797", "Av. República de Panamá 2461, La Victoria", "(01) 470-7170", "GLORIA" },
                { "BANCO INTERNACIONAL DEL PERÚ S.A.A. (INTERBANK)", "20100053455", "Av. Carlos Villarán 140, La Victoria", "(01) 219-2000", "INTERBANK" },
                { "BANCO DE CRÉDITO DEL PERÚ (BCP)", "20100047218", "Calle Centenario 156, La Molina", "(01) 313-2000", "BCP" },
                { "SUPERMERCADOS PERUANOS S.A. (PLAZA VEA)", "20195835370", "Calle Morelli 181, San Borja", "(01) 618-8000", "SPSA" },
                { "TIENDAS POR DEPARTAMENTO RIPLEY S.A.C.", "20337006125", "Av. Paseo de la República 3110, San Isidro", "(01) 610-4000", "RIPLEY" },
                { "SAGA FALABELLA S.A.", "20100128056", "Av. Paseo de la República 3220, San Isidro", "(01) 615-6000", "FALABELLA" },
                { "COSAPI S.A.", "20100039207", "Av. República de Colombia 791, San Isidro", "(01) 211-3500", "COSAPI" },
                { "COMPAÑÍA DE MINAS BUENAVENTURA S.A.A.", "20100079683", "Las Begonias 415, San Isidro", "(01) 419-2500", "BUENAVENTURA" },
                { "SOUTHERN PERU COPPER CORPORATION", "20100147557", "Av. Caminos del Inca 171, Surco", "(01) 512-0440", "SOUTHERN" },
                { "SODIMAC PERÚ S.A.", "20389230700", "Av. Javier Prado Este 1059, La Victoria", "(01) 615-6002", "SODIMAC" },
                { "CEMENTOS PACASMAYO S.A.A.", "20419387658", "Calle La Colonia 180, Surco", "(01) 317-6000", "PACASMAYO" },
                { "ENTEL PERÚ S.A.", "20106897914", "Av. República de Colombia 791, San Isidro", "(01) 611-1000", "ENTEL" },
                { "TELEFÓNICA DEL PERÚ S.A.A.", "20100017491", "Av. Enrique Canaval y Moreyra 240, San Isidro", "(01) 210-1000", "TELEFONICA" },
                { "KIMBERLY-CLARK PERÚ S.R.L.", "20297316691", "Av. Paseo de la República 3587, San Isidro", "(01) 610-7000", "KIMBERLY" },
                { "UNIÓN DE CERVECERÍAS PERUANAS BACKUS Y JOHNSTON", "20100113610", "Av. Nicolás Ayllón 3986, Ate", "(01) 311-3000", "BACKUS" },
                { "PETRÓLEOS DEL PERÚ - PETROPERÚ S.A.", "20100162122", "Av. Enrique Canaval Moreyra 150, San Isidro", "(01) 614-5000", "PETROPERU" }
        };

        for (int i = (int) count; i < empresas.length; i++) {
            String[] e = empresas[i];
            Empresa emp = Empresa.builder()
                    .nombre(e[0])
                    .codigo("EMP" + String.format("%03d", i + 1))
                    .abrev(e[4])
                    .direccion(e[2])
                    .telefono(e[3])
                    .representante("Ing. Representante Corporativo")
                    .estado(1)
                    .fechaCre(LocalDate.of(2025, 1, 15))
                    .ctacte("CTA-" + e[1].substring(4))
                    .build();
            empresaRepository.save(emp);
        }
    }

    // ==========================================
    // 25. PLANES DE CONVENIO / ASEGURADORAS (Mínimo 20)
    // ==========================================
    private void seedConvenios() {
        long count = credPlanRepository.count();
        if (count >= 20) return;

        String[][] planes = {
                { "CONV-001", "RÍMAC EPS - PLAN INTEGRAL SALUD", "POL-RIMAC-89021" },
                { "CONV-002", "PACÍFICO EPS - PLAN COBERTURA TOTAL", "POL-PACIF-10293" },
                { "CONV-003", "MAPFRE PERÚ - PLAN FAMILIAR DORADO", "POL-MAPFRE-44512" },
                { "CONV-004", "SANITAS PERÚ EPS - PLAN CLÁSICO PLUS", "POL-SANITAS-88123" },
                { "CONV-005", "LA POSITIVA SEGUROS - PLAN SALUD TOTAL", "POL-POSITIV-77210" },
                { "CONV-006", "BANMÉDICA PERÚ - PLAN CORPORATIVO", "POL-BANMED-33412" },
                { "CONV-007", "CONVENIO CORPORATIVO MINERA ANTAMINA", "POL-CORP-MIN-01" },
                { "CONV-008", "CONVENIO COLABORADORES FERREYROS", "POL-CORP-FER-02" },
                { "CONV-009", "PLAN COBERTURA EMPLEADOS ALICORP", "POL-CORP-ALI-03" },
                { "CONV-010", "PLAN BENEFICIO FAMILIAR GLORIA", "POL-CORP-GLO-04" },
                { "CONV-011", "CONVENIO COLECTIVO INTERBANK SALUD", "POL-CORP-INT-05" },
                { "CONV-012", "PLAN SEGURO MÉDICO BCP COLABORADOR", "POL-CORP-BCP-06" },
                { "CONV-013", "CONVENIO SUPERMERCADOS PERUANOS", "POL-CORP-SPS-07" },
                { "CONV-014", "PLAN SALUD TOTAL RIPLEY PERÚ", "POL-CORP-RIP-08" },
                { "CONV-015", "PLAN COBERTURA SAGA FALABELLA", "POL-CORP-FAL-09" },
                { "CONV-016", "CONVENIO MÉDICO COSAPI INGENIERÍA", "POL-CORP-COS-10" },
                { "CONV-017", "PLAN SALUD OCUPACIONAL BUENAVENTURA", "POL-CORP-BUE-11" },
                { "CONV-018", "CONVENIO MINERÍA SOUTHERN COPPER", "POL-CORP-SOU-12" },
                { "CONV-019", "PLAN BENEFICIO CLÍNICO ENTEL", "POL-CORP-ENT-13" },
                { "CONV-020", "PLAN SALUD CORPORATIVO BACKUS", "POL-CORP-BAC-14" }
        };

        for (int i = (int) count; i < planes.length; i++) {
            String[] p = planes[i];
            CredPlan plan = CredPlan.builder()
                    .codigo(p[0])
                    .nombre(p[1])
                    .poliza(p[2])
                    .estado("1")
                    .fechaInicio(LocalDate.of(2026, 1, 1))
                    .fechaFin(LocalDate.of(2026, 12, 31))
                    .idPersonalUser(1L)
                    .build();
            credPlanRepository.save(plan);
        }
    }

    // ==========================================
    // 26. ACTIVOS FIJOS (Mínimo 20)
    // ==========================================
    private void seedActivos(List<Catalogo> servicios) {
        long count = activoRepository.count();
        if (count >= 20 || servicios.isEmpty()) return;

        Catalogo cat = servicios.get(0);

        String[][] equipos = {
                { "ACT-MED-00142", "SN-GE-VOLUSON-E8", "ECÓGRAFO DOPPLER 4D GENERAL ELECTRIC", "85000.00", "SALA DE ECOGRAFÍA - CONSULTORIO 3" },
                { "ACT-MED-00143", "SN-SIEMENS-MULTIX-PRO", "EQUIPO DE RAYOS X DIGITAL SIEMENS", "120000.00", "SALA DE RAYOS X DIGITAL - PISO 1" },
                { "ACT-MED-00144", "SN-MINDRAY-BC5390", "ANALIZADOR HEMATOLÓGICO AUTOMATIZADO", "45000.00", "LABORATORIO CLÍNICO - HEMATOLOGÍA" },
                { "ACT-MED-00145", "SN-ROCHE-COBAS-C311", "ANALIZADOR BIOQUÍMICO ROCHE COBAS", "72000.00", "LABORATORIO CLÍNICO - BIOQUÍMICA" },
                { "ACT-MED-00146", "SN-PHILIPS-AFFINITI-70", "ECÓGRAFO CARDIOVASCULAR PHILIPS", "95000.00", "CONSULTORIO DE CARDIOLOGÍA 2" },
                { "ACT-MED-00147", "SN-GE-MAC2000", "ELECTROCARDIÓGRAFO DIGITAL 12 CANALES", "15000.00", "CARDIOLOGÍA - TRIAJE" },
                { "ACT-MED-00148", "SN-OLYMPUS-EVIS-EXERA", "TORRE DE ENDOSCOPIA DIGESTIVA OLYMPUS", "110000.00", "SALA DE GASTROENTEROLOGÍA" },
                { "ACT-MED-00149", "SN-TUTTNAUER-3870E", "AUTOCLAVE HOSPITALARIO DE ESTERILIZACIÓN", "28000.00", "CENTRAL DE ESTERILIZACIÓN" },
                { "ACT-MED-00150", "SN-DRAGER-FABIUS-PLUS", "MÁQUINA DE ANESTESIA DRÄGER", "88000.00", "CENTRO QUIRÚRGICO - QUIRÓFANO 1" },
                { "ACT-MED-00151", "SN-PHILIPS-INTELLIVUE", "MONITOR MULTIPARÁMETRO DE SIGNOS VITALES", "18000.00", "RECUPERACIÓN QUIRÚRGICA" },
                { "ACT-MED-00152", "SN-ZOLL-R-SERIES", "DESFIBRILADOR BIFÁSICO CON MARCAPASOS", "24000.00", "SALA DE SHOCK Y TRAUMA" },
                { "ACT-MED-00153", "SN-CARL-ZEISS-PRIMO", "MICROSCOPIO ÓPTICO BINOCULAR ZEISS", "12500.00", "LABORATORIO - MICROBIOLOGÍA" },
                { "ACT-MED-00154", "SN-KAVO-ESTETICA-E30", "UNIDAD DENTAL COMPLETA SILLÓN KAVO", "32000.00", "CONSULTORIO ODONTOLÓGICO 1" },
                { "ACT-MED-00155", "SN-TOPCON-TRC-NW400", "RETINÓGRAFO DIGITAL NO MIDRIÁTICO", "58000.00", "CONSULTORIO DE OFTALMOLOGÍA" },
                { "ACT-MED-00156", "SN-HETTICH-ROTINA-380", "CENTRÍFUGA DE MESA REFRIGERADA HETTICH", "14500.00", "LABORATORIO - BANCO DE SANGRE" },
                { "ACT-MED-00157", "SN-MAQUET-BETASTAR", "MESA DE OPERACIONES QUIRÚRGICA MAQUET", "65000.00", "CENTRO QUIRÚRGICO - QUIRÓFANO 2" },
                { "ACT-MED-00158", "SN-DRAEGER-BABYLOG", "VENTILADOR MECÁNICO NEONATAL DRÄGER", "79000.00", "UCI NEONATAL" },
                { "ACT-MED-00159", "SN-HILLROM-PROGRESSA", "CAMA HOSPITALARIA ELÉCTRICA HILL-ROM", "16000.00", "HOSPITALIZACIÓN PISO 2" },
                { "ACT-MED-00160", "SN-WELCH-ALLYN-CONNEX", "SISTEMA INTEGRADO DE TRIAJE VITAL", "11000.00", "TRIAJE AMBULATORIO" },
                { "ACT-MED-00161", "SN-STRYKER-SYSTEM-8", "EQUIPO DE CIRUGÍA TRAUMATOLÓGICA", "48000.00", "TRAUMATOLOGÍA Y QUIRÓFANO" }
        };

        for (int i = (int) count; i < equipos.length; i++) {
            String[] eq = equipos[i];
            Activo a = Activo.builder()
                    .catalogo(cat)
                    .codigoPatrimonial(eq[0])
                    .numeroSerie(eq[1])
                    .estadoFisico("BUENO")
                    .situacion("OPERATIVO")
                    .ubicacionDetallada(eq[4])
                    .valorCompra(new BigDecimal(eq[3]))
                    .fechaCompra(LocalDate.of(2025, ((i % 11) + 1), 10))
                    .idResponsable(1L)
                    .vidaUtilAnios(7)
                    .observaciones("Equipo operativo con mantenimiento preventivo vigente")
                    .build();
            activoRepository.save(a);
        }
    }

    // ==========================================
    // 27. AUDITORÍA DE SEGURIDAD (Mínimo 25)
    // ==========================================
    private void seedAudit() {
        long count = auditRepository.count();
        if (count >= 25) return;

        Object[][] logs = {
                { "AUTH", "LOGIN", "Inicio de sesión exitoso desde consola web", "/api/auth/login", "POST" },
                { "VENTAS", "EMITIR_COMPROBANTE", "Emisión de Boleta B001-00000001 por S/. 180.00", "/api/ventas", "POST" },
                { "COMPRAS", "REGISTRAR_ORDEN", "Registro de Factura de Compra F001-000101", "/api/compras", "POST" },
                { "CAJA_CHICA", "EGRESO_CAJA", "Egreso de Caja Chica S/. 85.00 para útiles y papelería", "/api/caja-chica/movimientos", "POST" },
                { "DESCUENTOS", "AUTORIZAR", "Aprobación de descuento especial convenio 20%", "/api/descuentos/autorizar", "POST" },
                { "VENTAS", "EMITIR_FACTURA", "Emisión de Factura F001-00000005 por S/. 450.00", "/api/ventas", "POST" },
                { "VENTAS", "ANULAR_COMPROBANTE", "Solicitud de anulación aprobada para Boleta B001-00000009", "/api/ventas/anular", "POST" },
                { "NOTAS_CREDITO", "EMITIR_NC", "Emisión de Nota de Crédito NC01-00000001", "/api/notas-credito", "POST" },
                { "CAJA_GENERAL", "TRANSFERENCIA", "Transferencia bancaria de Caja General a Cta BCP", "/api/caja-general/transferir", "POST" },
                { "ARQUEO_CAJA", "CIERRE_TURNO", "Arqueo de cierre de caja turno mañana conforme", "/api/arqueo-caja/cerrar", "POST" },
                { "PAGOS", "PAGO_HONORARIOS", "Liquidación de recibo por honorarios Dr. Juan Ramos", "/api/pagos/honorarios", "POST" },
                { "PAGOS", "PAGO_PLANILLA", "Abono de nómina quincenal personal de enfermería", "/api/pagos/personal", "POST" },
                { "PAGOS", "PAGO_SERVICIOS", "Pago de servicio eléctrico Enel Sede Central", "/api/pagos/otros", "POST" },
                { "KARDEX", "INGRESO_ALMACEN", "Ingreso al Kardex por orden de compra reactivos", "/api/kardex/ingreso", "POST" },
                { "CATALOGO", "CREAR_SERVICIO", "Creación de nuevo servicio en catálogo médico", "/api/catalogo", "POST" },
                { "PRODUCTOS", "AJUSTE_STOCK", "Actualización de stock físico en almacén central", "/api/productos/stock", "PUT" },
                { "PROVEEDORES", "REGISTRAR", "Registro de nuevo proveedor farmacéutico Droguería", "/api/proveedores", "POST" },
                { "SUCURSALES", "MODIFICAR", "Actualización de dirección en Sede San Isidro", "/api/sucursales/2", "PUT" },
                { "PAQUETES", "CREAR_PAQUETE", "Creación de combo Chequeo Preventivo Integral", "/api/paquetes", "POST" },
                { "EMPRESAS", "REGISTRAR", "Afiliación de nueva empresa cliente corporativa", "/api/empresas", "POST" },
                { "CONVENIOS", "VINCULAR_PLAN", "Vinculación de plan de cobertura con Rímac EPS", "/api/convenios/plan", "POST" },
                { "USUARIOS", "CAMBIO_PASSWORD", "Actualización de contraseña de usuario administrador", "/api/users/password", "PUT" },
                { "PUNTOS_VENTA", "ASIGNAR_SERIE", "Asignación de serie B001 a terminal de cobranza 3", "/api/puntos/series", "POST" },
                { "ACTIVOS", "REGISTRAR_ACTIVO", "Alta patrimonial de ecógrafo Doppler 4D", "/api/activos", "POST" },
                { "AUTH", "LOGOUT", "Cierre de sesión de usuario conforme políticas", "/api/auth/logout", "POST" }
        };

        for (int i = (int) count; i < logs.length; i++) {
            Object[] l = logs[i];
            AuditLog a = AuditLog.builder()
                    .userId(1L)
                    .username("admin")
                    .module((String) l[0])
                    .action((String) l[1])
                    .details((String) l[2])
                    .ipAddress("192.168.1." + (100 + (i % 20)))
                    .status("SUCCESS")
                    .url((String) l[3])
                    .method((String) l[4])
                    .timestamp(LocalDateTime.now().minusHours(26 - i))
                    .build();
            auditRepository.save(a);
        }
    }
}
