package com.pe.finanzas.reimprimir.controller;

import com.pe.finanzas.reimprimir.dto.DocumentoReimpresionDTO;
import com.pe.finanzas.reimprimir.dto.ReimpresionFilterDTO;
import com.pe.finanzas.reimprimir.service.ReimpresionService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reimpresiones")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class ReimpresionController {

    private final ReimpresionService reimpresionService;

    @PostMapping("/buscar")
    @PreAuthorize("hasAuthority('REIMPRESION_LEER')")
    public ResponseEntity<ApiResponse<List<DocumentoReimpresionDTO>>> buscarDocumentos(
            @RequestBody ReimpresionFilterDTO filtros) {

        List<DocumentoReimpresionDTO> documentos = reimpresionService.buscarDocumentos(filtros);

        return ResponseEntity.ok(ApiResponse.<List<DocumentoReimpresionDTO>>builder()
                .success(true)
                .message("Documentos encontrados exitosamente")
                .data(documentos)
                .build());
    }
}
