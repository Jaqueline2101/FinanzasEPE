package com.pe.finanzas.compra.controller;

import com.pe.finanzas.compra.entity.CronogramaPago;
import com.pe.finanzas.compra.service.CompraService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@RestController
@RequestMapping("/api/cronograma-pagos")
@RequiredArgsConstructor
public class CronogramaPagoController {

    private final CompraService compraService;

    @GetMapping
    public ResponseEntity<Page<CronogramaPago>> listarDeudas(@RequestParam Map<String, Object> params,
            Pageable pageable) {
        return ResponseEntity.ok(compraService.listarDeudas(params, pageable));
    }

    @PostMapping("/{id}/pagar")
    public ResponseEntity<CronogramaPago> pagarCuota(
            @PathVariable Long id,
            @RequestParam("numeroOperacion") String numeroOperacion,
            @RequestParam(value = "voucher", required = false) MultipartFile voucher) {

        return ResponseEntity.ok(compraService.registrarPagoCuota(id, numeroOperacion, voucher));
    }

    @PostMapping("/pagar-masivo")
    public ResponseEntity<java.util.List<CronogramaPago>> pagarMasivo(
            @RequestParam("ids") java.util.List<Long> ids,
            @RequestParam("numeroOperacion") String numeroOperacion,
            @RequestParam(value = "voucher", required = false) MultipartFile voucher) {

        return ResponseEntity.ok(compraService.registrarPagosMasivos(ids, numeroOperacion, voucher));
    }
}
