package com.pe.finanzas.reimprimir.service;

import com.pe.finanzas.pagorec_honorarios.entity.PagoComprobante;
import com.pe.finanzas.pagorec_honorarios.repository.PagoComprobanteRepository;
import com.pe.finanzas.reimprimir.dto.DocumentoReimpresionDTO;
import com.pe.finanzas.reimprimir.dto.ReimpresionFilterDTO;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReimpresionServiceImpl implements ReimpresionService {

    private final VentaRegistroRepository ventaRepository;
    private final PagoComprobanteRepository comprobanteRepository;

    @Override
    public List<DocumentoReimpresionDTO> buscarDocumentos(ReimpresionFilterDTO filtros) {
        log.info("Iniciando búsqueda de documentos. Filtros: {}", filtros);
        List<DocumentoReimpresionDTO> resultados = new ArrayList<>();

        String modulo = filtros.getModuloOrigen();
        boolean buscarTodo = modulo == null || modulo.isEmpty() || modulo.equalsIgnoreCase("ALL") || modulo.equalsIgnoreCase("TODOS");
        
        boolean buscarVentas = buscarTodo || modulo.equalsIgnoreCase("VENTAS");
        boolean buscarHonorarios = buscarTodo || modulo.equalsIgnoreCase("HONORARIOS");
        boolean buscarPersonal = buscarTodo || modulo.equalsIgnoreCase("PAGO_PERSONAL");

        // Si las fechas son nulas, usamos un rango por defecto (ej: hoy) o permitimos búsqueda total
        // Para evitar problemas de performance en tablas grandes, si son nulas pondremos hoy por defecto si no hay query
        if (filtros.getFechaDesde() == null && filtros.getFechaHasta() == null && (filtros.getQuery() == null || filtros.getQuery().isEmpty())) {
            filtros.setFechaDesde(java.time.LocalDate.now());
            filtros.setFechaHasta(java.time.LocalDate.now());
        }

        if (buscarVentas) {
            log.info("Buscando VENTAS entre {} y {}", filtros.getFechaDesde(), filtros.getFechaHasta());
            List<VentaRegistro> ventas;
            if (filtros.getFechaDesde() != null && filtros.getFechaHasta() != null) {
                ventas = ventaRepository.findByFechaBetween(filtros.getFechaDesde(), filtros.getFechaHasta());
            } else {
                ventas = ventaRepository.findAll(); // Ojo: esto puede ser pesado, pero matchesVenta filtrará
            }
            
            log.info("Ventas encontradas en BD: {} (Sin procesar filtros adicionales)", ventas.size());
            resultados.addAll(ventas.stream()
                    .filter(v -> matchesVenta(v, filtros))
                    .map(this::mapToDTO)
                    .collect(Collectors.toList()));
        }

        if (buscarHonorarios || buscarPersonal) {
            log.info("Buscando COMPROBANTES entre {} y {}", filtros.getFechaDesde(), filtros.getFechaHasta());
            List<PagoComprobante> comprobantes;
            if (filtros.getFechaDesde() != null && filtros.getFechaHasta() != null) {
                comprobantes = comprobanteRepository.findByFechaEmisionBetween(filtros.getFechaDesde(), filtros.getFechaHasta());
            } else {
                comprobantes = comprobanteRepository.findAll();
            }
            
            log.info("Comprobantes encontrados en BD: {} (Sin procesar filtros adicionales)", comprobantes.size());
            resultados.addAll(comprobantes.stream()
                    .filter(c -> matchesComprobante(c, filtros, buscarHonorarios, buscarPersonal))
                    .map(this::mapToDTO)
                    .collect(Collectors.toList()));
        }

        log.info("Resultados finales después de filtros: {}", resultados.size());
        return resultados;
    }

    private boolean matchesVenta(VentaRegistro v, ReimpresionFilterDTO f) {
        // Búsqueda específica (AND)
        if (f.getSerie() != null && !f.getSerie().isEmpty()) {
            if (v.getSerie() == null || !v.getSerie().toLowerCase().contains(f.getSerie().toLowerCase()))
                return false;
        }
        if (f.getNumero() != null && !f.getNumero().isEmpty()) {
            if (v.getNumdoc() == null || !v.getNumdoc().contains(f.getNumero()))
                return false;
        }
        if (f.getNumDocIdentidad() != null && !f.getNumDocIdentidad().isEmpty()) {
            if (v.getNroDni() == null || !v.getNroDni().equals(f.getNumDocIdentidad()))
                return false;
        }
        if (f.getTipoDocumento() != null && !f.getTipoDocumento().isEmpty()) {
            String tipo = f.getTipoDocumento().toUpperCase();
            String vTipo = v.getTipoDoc() != null ? v.getTipoDoc().toUpperCase() : "";
            
            // Mapeo flexible para tipos de documento
            if (tipo.contains("BOLETA") && !vTipo.equals("B") && !vTipo.contains("BOLETA")) return false;
            if (tipo.contains("FACTURA") && !vTipo.equals("F") && !vTipo.contains("FACTURA")) return false;
            if (tipo.contains("TICKET") && !vTipo.equals("T") && !vTipo.contains("TICKET")) return false;
            
            if (!tipo.contains("BOLETA") && !tipo.contains("FACTURA") && !tipo.contains("TICKET")) {
                if (!vTipo.contains(tipo)) return false;
            }
        }

        // Búsqueda general (OR)
        if (f.getQuery() != null && !f.getQuery().isEmpty()) {
            String qRaw = f.getQuery().toLowerCase();
            String[] parts = qRaw.split("[^a-z0-9]+");

            String allText = (v.getSerie() != null ? v.getSerie() : "") + " " +
                    (v.getNumdoc() != null ? v.getNumdoc() : "") + " " +
                    (v.getNroDni() != null ? v.getNroDni() : "") + " " +
                    (v.getNombrePac() != null ? v.getNombrePac() : "") + " " +
                    (v.getTipoDoc() != null ? v.getTipoDoc() : "");
            allText = allText.toLowerCase();

            for (String part : parts) {
                if (!part.isEmpty() && !allText.contains(part)) {
                    return false;
                }
            }
        }

        return true;
    }

    private boolean matchesComprobante(PagoComprobante c, ReimpresionFilterDTO f, boolean checkHono,
            boolean checkPers) {

        String tipoBenef = c.getTipoBeneficiario() != null ? c.getTipoBeneficiario() : "";

        // Filtrar por tipo de beneficiario según el módulo solicitado
        if (!checkHono && (tipoBenef.equalsIgnoreCase("MEDICO") || tipoBenef.equalsIgnoreCase("PROVEEDOR")
                || tipoBenef.equalsIgnoreCase("MÉDICO")))
            return false;
        if (!checkPers && tipoBenef.equalsIgnoreCase("TRABAJADOR"))
            return false;

        // Filtros adicionales si se especificaron ambos pero queremos discriminar
        if (f.getModuloOrigen() != null && !f.getModuloOrigen().isEmpty()) {
            String mod = f.getModuloOrigen().toUpperCase();
            if (mod.equals("HONORARIOS") && tipoBenef.equalsIgnoreCase("TRABAJADOR"))
                return false;
            if (mod.equals("PAGO_PERSONAL") && !tipoBenef.equalsIgnoreCase("TRABAJADOR"))
                return false;
        }

        // Búsqueda específica (AND)
        if (f.getSerie() != null && !f.getSerie().isEmpty()) {
            if (c.getSerieComprobante() == null || !c.getSerieComprobante().toLowerCase().contains(f.getSerie().toLowerCase()))
                return false;
        }
        if (f.getNumero() != null && !f.getNumero().isEmpty()) {
            if (c.getNumeroRecibo() == null || !c.getNumeroRecibo().contains(f.getNumero()))
                return false;
        }
        if (f.getNumDocIdentidad() != null && !f.getNumDocIdentidad().isEmpty()) {
            if (c.getNumeroDocumento() == null || !c.getNumeroDocumento().equals(f.getNumDocIdentidad()))
                return false;
        }

        // Búsqueda general (OR)
        if (f.getQuery() != null && !f.getQuery().isEmpty()) {
            String qRaw = f.getQuery().toLowerCase();
            String[] parts = qRaw.split("[^a-z0-9]+");

            String allText = (c.getSerieComprobante() != null ? c.getSerieComprobante() : "") + " " +
                    (c.getNumeroRecibo() != null ? c.getNumeroRecibo() : "") + " " +
                    (c.getNumeroDocumento() != null ? c.getNumeroDocumento() : "") + " " +
                    (c.getNombreRazonSocial() != null ? c.getNombreRazonSocial() : "") + " " +
                    (c.getTipoPago() != null ? c.getTipoPago() : "");
            allText = allText.toLowerCase();

            for (String part : parts) {
                if (!part.isEmpty() && !allText.contains(part)) {
                    return false;
                }
            }
        }

        return true;
    }

    private DocumentoReimpresionDTO mapToDTO(VentaRegistro v) {
        String tipoDocFull = v.getTipoDoc();
        if ("B".equalsIgnoreCase(tipoDocFull)) tipoDocFull = "BOLETA";
        else if ("F".equalsIgnoreCase(tipoDocFull)) tipoDocFull = "FACTURA";
        else if ("T".equalsIgnoreCase(tipoDocFull)) tipoDocFull = "TICKET";
        else if (tipoDocFull == null) tipoDocFull = "VENTA";

        // Mapeo de estado corregido (Integer comparison)
        String estadoLabel = "Emitido";
        if (v.getEstado() != null) {
            if (v.getEstado() == 10) estadoLabel = "Anulado";
            else if (v.getEstado() == 1 || v.getEstado() == 16) estadoLabel = "Emitido";
            else estadoLabel = "Estado " + v.getEstado();
        }

        return DocumentoReimpresionDTO.builder()
                .id(v.getIdVenta())
                .fecha(v.getFecha())
                .tipoDocumento(tipoDocFull)
                .serie(v.getSerie() != null ? v.getSerie() : "---")
                .numero(v.getNumdoc() != null ? v.getNumdoc() : "---")
                .clienteBeneficiario(
                        v.getNombrePac() != null ? v.getNombrePac() : (v.getRazon() != null ? v.getRazon() : "VARIOUS"))
                .total(v.getTotal())
                .moneda("PEN")
                .estado(estadoLabel)
                .usuario(v.getUserUpd())
                .moduloOrigen("VENTAS")
                .build();
    }

    private DocumentoReimpresionDTO mapToDTO(PagoComprobante c) {
        String tipoBenef = c.getTipoBeneficiario() != null ? c.getTipoBeneficiario() : "";
        String modulo = tipoBenef.equalsIgnoreCase("TRABAJADOR") ? "PAGO_PERSONAL" : "HONORARIOS";
        
        String estadoLabel = c.getEstadoPago() != null ? c.getEstadoPago() : "PENDIENTE";
        if ("1".equals(estadoLabel)) estadoLabel = "Emitido";
        else if ("0".equals(estadoLabel)) estadoLabel = "Anulado";

        return DocumentoReimpresionDTO.builder()
                .id(c.getIdPago())
                .fecha(c.getFechaEmision())
                .tipoDocumento(c.getTipoPago() != null ? c.getTipoPago() : "PAGO")
                .serie(c.getSerieComprobante() != null ? c.getSerieComprobante() : "---")
                .numero(c.getNumeroRecibo() != null ? c.getNumeroRecibo() : "---")
                .clienteBeneficiario(c.getNombreRazonSocial() != null ? c.getNombreRazonSocial() : "---")
                .total(c.getMontoNeto())
                .moneda(c.getMoneda() != null ? c.getMoneda() : "PEN")
                .estado(estadoLabel)
                .usuario(c.getUsuarioRegistro())
                .moduloOrigen(modulo)
                .build();
    }
}
