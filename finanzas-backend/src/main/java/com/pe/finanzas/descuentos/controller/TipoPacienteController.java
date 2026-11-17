package com.pe.finanzas.descuentos.controller;

import com.pe.finanzas.descuentos.dto.DescuentoMasivoDto;
import com.pe.finanzas.descuentos.dto.TipoPacienteDto;
import com.pe.finanzas.descuentos.dto.TipoPacienteRequest;
import com.pe.finanzas.descuentos.dto.TipoPacienteServicioDto;
import com.pe.finanzas.descuentos.entity.TipoPaciente;
import com.pe.finanzas.descuentos.entity.TipoPacienteServicio;
import com.pe.finanzas.descuentos.repository.TipoPacienteRepository;
import com.pe.finanzas.descuentos.repository.TipoPacienteServicioRepository;
import com.pe.finanzas.exception.ResourceNotFoundException;
import com.pe.finanzas.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tipos-paciente")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class TipoPacienteController {

        private final TipoPacienteRepository tipoPacienteRepository;
        private final TipoPacienteServicioRepository tipoPacienteServicioRepository;
        private final com.pe.finanzas.descuentos.service.AplicacionDescuentoService aplicacionDescuentoService;

        @PostMapping
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_CREAR')")
        public ResponseEntity<ApiResponse<TipoPacienteDto>> crearTipoPaciente(
                        @Valid @RequestBody TipoPacienteRequest request) {

                if (tipoPacienteRepository.existsByCodigo(request.getCodigo())) {
                        throw new IllegalArgumentException(
                                        "Ya existe un tipo de paciente con el código: " + request.getCodigo());
                }

                TipoPaciente tipoPaciente = TipoPaciente.builder()
                                .codigo(request.getCodigo())
                                .nombre(request.getNombre())
                                .dsc(request.getDsc() != null ? request.getDsc() : BigDecimal.ZERO)
                                .cob(request.getCob() != null ? request.getCob() : BigDecimal.ZERO)
                                .frq(Boolean.TRUE.equals(request.getEsFrecuente()) ? "1" : "0")
                                .estado(request.getEstado() != null ? request.getEstado() : 1)
                                .codmon("S")
                                .build();

                TipoPaciente guardado = tipoPacienteRepository.save(tipoPaciente);

                if (request.getServicios() != null && !request.getServicios().isEmpty()) {
                        guardarDescuentosServicio(guardado, request.getServicios());
                }

                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.<TipoPacienteDto>builder()
                                                .success(true)
                                                .message("Tipo de paciente creado exitosamente")
                                                .data(convertirADto(guardado))
                                                .build());
        }

        @PutMapping("/{id}")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<TipoPacienteDto>> actualizarTipoPaciente(
                        @PathVariable Long id,
                        @Valid @RequestBody TipoPacienteRequest request) {

                TipoPaciente tipoPaciente = tipoPacienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de paciente no encontrado con ID: " + id));

                tipoPaciente.setNombre(request.getNombre());
                tipoPaciente.setDsc(request.getDsc() != null ? request.getDsc() : BigDecimal.ZERO);
                tipoPaciente.setCob(request.getCob() != null ? request.getCob() : BigDecimal.ZERO);
                tipoPaciente.setFrq(Boolean.TRUE.equals(request.getEsFrecuente()) ? "1" : "0");

                if (request.getEstado() != null) {
                        tipoPaciente.setEstado(request.getEstado());
                }

                TipoPaciente actualizado = tipoPacienteRepository.save(tipoPaciente);

                if (request.getServicios() != null) {

                        tipoPacienteServicioRepository.deleteByTipoPacienteIdTipoPaciente(id);

                        if (!request.getServicios().isEmpty()) {
                                guardarDescuentosServicio(actualizado, request.getServicios());
                        }
                }

                return ResponseEntity.ok(ApiResponse.<TipoPacienteDto>builder()
                                .success(true)
                                .message("Tipo de paciente actualizado exitosamente")
                                .data(convertirADto(actualizado))
                                .build());
        }

        @GetMapping("/{id}")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_LEER')")
        public ResponseEntity<ApiResponse<TipoPacienteDto>> obtenerTipoPaciente(@PathVariable Long id) {

                TipoPaciente tipoPaciente = tipoPacienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de paciente no encontrado con ID: " + id));

                return ResponseEntity.ok(ApiResponse.<TipoPacienteDto>builder()
                                .success(true)
                                .message("Tipo de paciente obtenido exitosamente")
                                .data(convertirADto(tipoPaciente))
                                .build());
        }

        @GetMapping
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_LEER')")
        public ResponseEntity<ApiResponse<List<TipoPacienteDto>>> listarTiposPaciente() {

                List<TipoPacienteDto> tipos = tipoPacienteRepository.findAll().stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.<List<TipoPacienteDto>>builder()
                                .success(true)
                                .message("Tipos de paciente obtenidos exitosamente")
                                .data(tipos)
                                .build());
        }

        @GetMapping("/activos")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_LEER')")
        public ResponseEntity<ApiResponse<List<TipoPacienteDto>>> listarTiposPacienteActivos() {

                List<TipoPacienteDto> tipos = tipoPacienteRepository.findByEstado(1).stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.<List<TipoPacienteDto>>builder()
                                .success(true)
                                .message("Tipos de paciente activos obtenidos exitosamente")
                                .data(tipos)
                                .build());
        }

        @GetMapping("/con-descuento")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_LEER')")
        public ResponseEntity<ApiResponse<List<TipoPacienteDto>>> listarConDescuento() {

                List<TipoPacienteDto> tipos = tipoPacienteRepository.findActivosConDescuento().stream()
                                .map(this::convertirADto)
                                .collect(Collectors.toList());

                return ResponseEntity.ok(ApiResponse.<List<TipoPacienteDto>>builder()
                                .success(true)
                                .message("Tipos de paciente con descuento obtenidos exitosamente")
                                .data(tipos)
                                .build());
        }

        @PatchMapping("/{id}/estado")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_ACTUALIZAR')")
        public ResponseEntity<ApiResponse<Void>> cambiarEstado(
                        @PathVariable Long id,
                        @RequestParam Integer estado) {

                TipoPaciente tipoPaciente = tipoPacienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de paciente no encontrado con ID: " + id));

                tipoPaciente.setEstado(estado);
                tipoPacienteRepository.save(tipoPaciente);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Estado actualizado exitosamente")
                                .build());
        }

        @DeleteMapping("/{id}")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_ELIMINAR')")
        public ResponseEntity<ApiResponse<Void>> eliminarTipoPaciente(@PathVariable Long id) {

                if (!tipoPacienteRepository.existsById(id)) {
                        throw new ResourceNotFoundException("Tipo de paciente no encontrado con ID: " + id);
                }

                tipoPacienteRepository.deleteById(id);

                return ResponseEntity.ok(ApiResponse.<Void>builder()
                                .success(true)
                                .message("Tipo de paciente eliminado exitosamente")
                                .build());
        }

        private TipoPacienteDto convertirADto(TipoPaciente entity) {
                BigDecimal beneficioTotal = (entity.getDsc() != null ? entity.getDsc() : BigDecimal.ZERO)
                                .add(entity.getCob() != null ? entity.getCob() : BigDecimal.ZERO);

                List<TipoPacienteServicioDto> servicios = tipoPacienteServicioRepository
                                .findByTipoPacienteId(entity.getIdTipoPaciente())
                                .stream()
                                .map(this::convertirServicioADto)
                                .collect(Collectors.toList());

                return TipoPacienteDto.builder()
                                .idTipoPaciente(entity.getIdTipoPaciente())
                                .codigo(entity.getCodigo())
                                .nombre(entity.getNombre())
                                .dsc(entity.getDsc())
                                .cob(entity.getCob())
                                .frq(entity.getFrq() != null ? Integer.valueOf(entity.getFrq()) : null)
                                .estado(entity.getEstado())
                                .activo(entity.isActivo())
                                .tieneDescuento(entity.tieneDescuento())
                                .tieneCobertura(entity.tieneCobertura())
                                .esFrecuente(entity.esFrecuente())
                                .beneficioTotal(beneficioTotal)
                                .servicios(servicios)
                                .build();
        }

        private void guardarDescuentosServicio(TipoPaciente tipoPaciente,
                        List<TipoPacienteRequest.ServicioDescuentoRequest> servicios) {
                for (TipoPacienteRequest.ServicioDescuentoRequest servicioReq : servicios) {
                        TipoPacienteServicio servicio = TipoPacienteServicio.builder()
                                        .tipoPaciente(tipoPaciente)
                                        .tipoServicio(servicioReq.getTipoServicio())
                                        .descuento(servicioReq.getDescuento() != null ? servicioReq.getDescuento()
                                                        : BigDecimal.ZERO)
                                        .cobertura(servicioReq.getCobertura() != null ? servicioReq.getCobertura()
                                                        : BigDecimal.ZERO)
                                        .estado(servicioReq.getEstado() != null
                                                        ? String.valueOf(servicioReq.getEstado())
                                                        : "1")
                                        .build();
                        tipoPacienteServicioRepository.save(servicio);
                }
        }

        @PostMapping("/{id}/descuentos/masivo")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_CREAR')")
        public ResponseEntity<ApiResponse<TipoPacienteDto>> aplicarDescuentoMasivo(
                        @PathVariable Long id,
                        @Valid @RequestBody DescuentoMasivoDto request) {

                TipoPaciente tipoPaciente = tipoPacienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de paciente no encontrado con ID: " + id));

                for (String servicioId : request.getServiciosIds()) {
                        tipoPacienteServicioRepository
                                        .findByTipoPacienteIdAndTipoServicio(id, servicioId)
                                        .ifPresent(tipoPacienteServicioRepository::delete);
                }

                List<TipoPacienteServicio> nuevosDescuentos = request.getServiciosIds().stream()
                                .map(servicioId -> TipoPacienteServicio.builder()
                                                .tipoPaciente(tipoPaciente)
                                                .tipoServicio(servicioId)
                                                .descuento(request.getPorcentajeDescuento())
                                                .cobertura(request.getCobertura() != null ? request.getCobertura()
                                                                : BigDecimal.ZERO)
                                                .estado("1")
                                                .build())
                                .collect(Collectors.toList());

                tipoPacienteServicioRepository.saveAll(nuevosDescuentos);

                TipoPacienteDto dto = convertirADto(tipoPaciente);

                return ResponseEntity.ok(ApiResponse.<TipoPacienteDto>builder()
                                .success(true)
                                .message(String.format("Descuento de %.2f%% aplicado a %d servicio(s)",
                                                request.getPorcentajeDescuento(), request.getServiciosIds().size()))
                                .data(dto)
                                .build());
        }

        private TipoPacienteServicioDto convertirServicioADto(TipoPacienteServicio entity) {
                BigDecimal beneficioTotal = (entity.getDescuento() != null ? entity.getDescuento() : BigDecimal.ZERO)
                                .add(entity.getCobertura() != null ? entity.getCobertura() : BigDecimal.ZERO);

                return TipoPacienteServicioDto.builder()
                                .idTipoPacienteServicio(entity.getIdTipoPacienteServicio())
                                .tipoServicio(entity.getTipoServicio())
                                .descuento(entity.getDescuento())
                                .cobertura(entity.getCobertura())
                                .beneficioTotal(beneficioTotal)
                                .estado(entity.getEstado() != null ? Integer.valueOf(entity.getEstado()) : null)
                                .build();
        }

        @GetMapping("/{idTipoPaciente}/servicios-con-descuento")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_LEER')")
        public ResponseEntity<ApiResponse<List<com.pe.finanzas.descuentos.dto.ServicioConDescuentoDTO>>> obtenerServiciosConDescuento(
                        @PathVariable Long idTipoPaciente) {

                List<com.pe.finanzas.descuentos.dto.ServicioConDescuentoDTO> servicios = aplicacionDescuentoService
                                .obtenerServiciosConDescuentosEspeciales(idTipoPaciente);

                return ResponseEntity
                                .ok(ApiResponse.<List<com.pe.finanzas.descuentos.dto.ServicioConDescuentoDTO>>builder()
                                                .success(true)
                                                .message("Servicios con descuentos especiales obtenidos exitosamente")
                                                .data(servicios)
                                                .build());
        }

        @PostMapping("/{id}/descuentos/batch")
        @PreAuthorize("hasAuthority('TIPO_PACIENTE_CREAR')")
        public ResponseEntity<ApiResponse<TipoPacienteDto>> aplicarDescuentosBatch(
                        @PathVariable Long id,
                        @Valid @RequestBody List<TipoPacienteRequest.ServicioDescuentoRequest> descuentos) {

                TipoPaciente tipoPaciente = tipoPacienteRepository.findById(id)
                                .orElseThrow(() -> new ResourceNotFoundException(
                                                "Tipo de paciente no encontrado con ID: " + id));

                for (TipoPacienteRequest.ServicioDescuentoRequest req : descuentos) {

                        TipoPacienteServicio descuento = tipoPacienteServicioRepository
                                        .findByTipoPacienteIdAndTipoServicio(id, req.getTipoServicio())
                                        .orElse(TipoPacienteServicio.builder()
                                                        .tipoPaciente(tipoPaciente)
                                                        .tipoServicio(req.getTipoServicio())
                                                        .build());

                        descuento.setDescuento(req.getDescuento() != null ? req.getDescuento() : BigDecimal.ZERO);
                        descuento.setCobertura(req.getCobertura() != null ? req.getCobertura() : BigDecimal.ZERO);
                        descuento.setEstado("1"); // Asegurar que esté activo

                        tipoPacienteServicioRepository.save(descuento);
                }

                TipoPacienteDto dto = convertirADto(tipoPaciente);

                return ResponseEntity.ok(ApiResponse.<TipoPacienteDto>builder()
                                .success(true)
                                .message(String.format("Se actualizaron descuentos para %d servicio(s)",
                                                descuentos.size()))
                                .data(dto)
                                .build());
        }
}
