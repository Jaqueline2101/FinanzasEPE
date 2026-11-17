package com.pe.finanzas.catalogo.controller;

import com.pe.finanzas.catalogo.entity.TipoTemperatura;
import com.pe.finanzas.catalogo.repository.TipoTemperaturaRepository;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/tipo-temperatura")
@RequiredArgsConstructor
public class TipoTemperaturaController {

    private final TipoTemperaturaRepository tipoTemperaturaRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<TipoTemperatura>>> getAll() {
        List<TipoTemperatura> lista = tipoTemperaturaRepository.findAll();
        return ResponseEntity.ok(ApiResponse.<List<TipoTemperatura>>builder()
                .success(true)
                .message("Lista de temperaturas obtenida")
                .data(lista)
                .build());
    }
}
