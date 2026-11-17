package com.pe.finanzas.correlatividad.service;

import com.pe.finanzas.correlatividad.dto.CorrelatividadDetalleRequestDTO;
import com.pe.finanzas.correlatividad.dto.CorrelatividadItemDTO;
import com.pe.finanzas.correlatividad.dto.CorrelatividadRequestDTO;
import com.pe.finanzas.correlatividad.dto.CorrelatividadResponseDTO;
import com.pe.finanzas.venta_registro.dto.VentaRegistroDto;
import com.pe.finanzas.venta_registro.entity.VentaRegistro;
import com.pe.finanzas.venta_registro.repository.VentaRegistroRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CorrelatividadService {

    private final VentaRegistroRepository ventaRegistroRepository;

    public CorrelatividadResponseDTO obtenerResumen(CorrelatividadRequestDTO request) {
        List<Object[]> results = ventaRegistroRepository.findCorrelatividadResumen(
                request.getIdSucursal(),
                request.getAnio(),
                request.getMes());

        List<CorrelatividadItemDTO> items = new ArrayList<>();
        BigDecimal totalBaseImp = BigDecimal.ZERO;
        BigDecimal totalIgv = BigDecimal.ZERO;
        BigDecimal totalGeneral = BigDecimal.ZERO;

        for (Object[] row : results) {
            Integer mes = (Integer) row[0];
            String tipoDoc = (String) row[1];
            String serie = (String) row[2];
            Long cantidad = (Long) row[3];
            String desde = (String) row[4];
            String hasta = (String) row[5];
            BigDecimal baseImp = (BigDecimal) row[6];
            BigDecimal igv = (BigDecimal) row[7];
            BigDecimal total = (BigDecimal) row[8];

            CorrelatividadItemDTO item = CorrelatividadItemDTO.builder()
                    .mes(mes)
                    .tipoDoc(tipoDoc)
                    .serie(serie)
                    .cantidad(cantidad)
                    .desde(desde)
                    .hasta(hasta)
                    .baseImp(baseImp != null ? baseImp : BigDecimal.ZERO)
                    .igv(igv != null ? igv : BigDecimal.ZERO)
                    .total(total != null ? total : BigDecimal.ZERO)
                    .build();

            items.add(item);

            totalBaseImp = totalBaseImp.add(item.getBaseImp());
            totalIgv = totalIgv.add(item.getIgv());
            totalGeneral = totalGeneral.add(item.getTotal());
        }

        return CorrelatividadResponseDTO.builder()
                .items(items)
                .totalBaseImp(totalBaseImp)
                .totalIgv(totalIgv)
                .totalGeneral(totalGeneral)
                .build();
    }
    public List<VentaRegistroDto> obtenerDetalle(CorrelatividadDetalleRequestDTO request) {
        List<VentaRegistro> results = ventaRegistroRepository.findCorrelatividadDetalle(
                request.getIdSucursal(),
                request.getAnio(),
                request.getMes(),
                request.getTipoDoc(),
                request.getSerie());

        return results.stream().map(this::convertirADto).collect(Collectors.toList());
    }

    private VentaRegistroDto convertirADto(VentaRegistro entity) {
        return VentaRegistroDto.builder()
                .idVenta(entity.getIdVenta())
                .fecha(entity.getFecha())
                .tipoDoc(entity.getTipoDoc())
                .serie(entity.getSerie())
                .numdoc(entity.getNumdoc())
                .nombrePac(entity.getNombrePac())
                .baseImp(entity.getBaseImp())
                .igv(entity.getIgv())
                .total(entity.getTotal())
                .estado(entity.getEstado())
                .build();
    }
}
