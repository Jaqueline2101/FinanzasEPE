package com.pe.finanzas.compania.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.access.prepost.PreAuthorize;

import com.pe.finanzas.compania.dto.CompaniaRegistroDTO;
import com.pe.finanzas.compania.dto.VinculoSeguroDTO;
import com.pe.finanzas.compania.entity.Compania;
import com.pe.finanzas.compania.entity.CompaniaPersonaVinculo;
import com.pe.finanzas.compania.service.CompaniaService;
import com.pe.finanzas.compania.repository.CompaniaRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/companias")
@RequiredArgsConstructor
public class CompaniaController {

    private final CompaniaService companiaService;
    private final CompaniaRepository companiaRepository;

    @PostMapping
    public ResponseEntity<Compania> registrarCompania(@RequestBody CompaniaRegistroDTO dto) {
        Compania nuevaCompania = companiaService.registrarCompania(dto);
        return new ResponseEntity<>(nuevaCompania, HttpStatus.CREATED);
    }

    @PostMapping("/{idCompania}/vincular")
    public ResponseEntity<CompaniaPersonaVinculo> vincularPaciente(
            @PathVariable Long idCompania,
            @RequestParam Long idPaciente,
            @RequestBody VinculoSeguroDTO dto) {

        CompaniaPersonaVinculo vinculo = companiaService.vincularPacienteASeguro(idCompania, idPaciente, dto);
        return new ResponseEntity<>(vinculo, HttpStatus.CREATED);
    }

    @GetMapping("/{idCompania}/vinculos")
    public ResponseEntity<List<CompaniaPersonaVinculo>> listarVinculos(@PathVariable Long idCompania) {
        return ResponseEntity.ok(companiaService.listarVinculos(idCompania));
    }

    @GetMapping
    public ResponseEntity<?> listarCompanias() {
        try {
            List<Compania> companias = companiaRepository.findAll();
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            // Register JavaTimeModule to handle LocalDateTime
            mapper.registerModule(new com.fasterxml.jackson.datatype.jsr310.JavaTimeModule());
            // Disable failure on empty beans just in case
            mapper.configure(com.fasterxml.jackson.databind.SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

            String json = mapper.writeValueAsString(companias);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json")
                    .body(json);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("{\"error\": \"Error serializando compañias: " + e.getMessage() + "\"}");
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<Compania> obtenerCompania(@PathVariable Long id) {
        return ResponseEntity.ok(companiaService.obtenerCompaniaPorId(id));
    }

    @GetMapping("/buscar-por-ruc/{ruc}")
    public ResponseEntity<?> buscarPorRuc(@PathVariable String ruc) {
        return ResponseEntity.ok(companiaService.obtenerPorRuc(ruc));
    }

    @GetMapping("/buscar-sugerencias")
    public ResponseEntity<java.util.List<com.pe.finanzas.compania.dto.BusinessSuggestionDTO>> buscarSugerencias(
            @RequestParam String term) {
        return ResponseEntity.ok(companiaService.buscarSugerencias(term));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Compania> actualizarCompania(@PathVariable Long id, @RequestBody CompaniaRegistroDTO dto) {
        return ResponseEntity.ok(companiaService.actualizarCompania(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarCompania(@PathVariable Long id) {
        companiaService.eliminarCompania(id);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
