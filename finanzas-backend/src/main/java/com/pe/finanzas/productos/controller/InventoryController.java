package com.pe.finanzas.productos.controller;

import com.pe.finanzas.productos.entity.Producto;
import com.pe.finanzas.productos.service.InventoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/inventario")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/sugerir-fefo/{idCatalogo}")
    public List<InventoryService.LoteSugerido> sugerirFEFO(
            @PathVariable Long idCatalogo, 
            @RequestParam BigDecimal cantidad) {
        return inventoryService.sugerirDespachoFEFO(idCatalogo, cantidad);
    }

    @GetMapping("/alertas-vencimiento")
    public List<Producto> getAlertasVencimiento(@RequestParam(required = false) Integer dias) {
        return inventoryService.obtenerAlertasVencimiento(dias);
    }
}
