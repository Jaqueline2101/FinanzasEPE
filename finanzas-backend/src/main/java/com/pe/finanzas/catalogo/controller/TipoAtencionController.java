package com.pe.finanzas.catalogo.controller;

import com.pe.finanzas.catalogo.entity.TipoAtencion;
import com.pe.finanzas.catalogo.repository.TipoAtencionRepository;
import com.pe.finanzas.niveles.dto.RespuestaGeneralDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-atencion")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class TipoAtencionController {

    private final TipoAtencionRepository tipoAtencionRepository;

    @GetMapping("/activos")
    public ResponseEntity<RespuestaGeneralDto<List<TipoAtencion>>> listarActivos() {
        try {
            List<TipoAtencion> tipos = tipoAtencionRepository.findAllActivos();
            return ResponseEntity.ok(RespuestaGeneralDto.exitoso("Tipos de atención activos obtenidos", tipos));
        } catch (Exception e) {
            log.error("Error al listar tipos de atención activos", e);
            return ResponseEntity.internalServerError()
                    .body(RespuestaGeneralDto.error("Error al obtener tipos de atención activos"));
        }
    }

    @GetMapping
    public ResponseEntity<RespuestaGeneralDto<List<TipoAtencion>>> listar() {
        try {
            List<TipoAtencion> tipos = tipoAtencionRepository.findAll();
            return ResponseEntity.ok(RespuestaGeneralDto.exitoso("Tipos de atención obtenidos", tipos));
        } catch (Exception e) {
            log.error("Error al listar tipos de atención", e);
            return ResponseEntity.internalServerError()
                    .body(RespuestaGeneralDto.error("Error al obtener tipos de atención"));
        }
    }

    @PostMapping
    public ResponseEntity<RespuestaGeneralDto<TipoAtencion>> guardar(@RequestBody TipoAtencion tipoAtencion) {
        try {
            if (tipoAtencionRepository.existsByCodigo(tipoAtencion.getCodigo())) {
                return ResponseEntity.badRequest()
                        .body(RespuestaGeneralDto.error("El código ya existe"));
            }
            TipoAtencion nuevo = tipoAtencionRepository.save(tipoAtencion);
            return ResponseEntity.ok(RespuestaGeneralDto.exitoso("Tipo de atención creado", nuevo));
        } catch (Exception e) {
            log.error("Error al guardar tipo de atención", e);
            return ResponseEntity.internalServerError()
                    .body(RespuestaGeneralDto.error("Error al guardar tipo de atención"));
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RespuestaGeneralDto<TipoAtencion>> actualizar(@PathVariable Long id,
            @RequestBody TipoAtencion tipoAtencion) {
        try {
            return tipoAtencionRepository.findById(id)
                    .map(existente -> {
                        existente.setNombre(tipoAtencion.getNombre());
                        existente.setCodigo(tipoAtencion.getCodigo());
                        existente.setOrden(tipoAtencion.getOrden());
                        existente.setEstado(tipoAtencion.getEstado());
                        TipoAtencion actualizado = tipoAtencionRepository.save(existente);
                        return ResponseEntity
                                .ok(RespuestaGeneralDto.exitoso("Tipo de atención actualizado", actualizado));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al actualizar tipo de atención", e);
            return ResponseEntity.internalServerError()
                    .body(RespuestaGeneralDto.error("Error al actualizar tipo de atención"));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<RespuestaGeneralDto<Void>> eliminar(@PathVariable Long id) {
        try {
            return tipoAtencionRepository.findById(id)
                    .map(tipo -> {
                        tipo.setEstado(0);
                        tipoAtencionRepository.save(tipo);
                        return ResponseEntity
                                .ok(RespuestaGeneralDto.<Void>exitoso("Tipo de atención inactivado", null));
                    })
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            log.error("Error al eliminar tipo de atención", e);
            return ResponseEntity.internalServerError()
                    .body(RespuestaGeneralDto.error("Error al eliminar tipo de atención"));
        }
    }
}
