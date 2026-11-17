package com.pe.finanzas.menu.controller;

import com.pe.finanzas.exception.BadRequestException;
import com.pe.finanzas.menu.dto.MenuItemDTO;
import com.pe.finanzas.menu.service.MenuService;
import com.pe.finanzas.shared.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/menu")
@CrossOrigin(origins = "*")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;
    @GetMapping("/{usuarioId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<List<MenuItemDTO>>> obtenerMenu(
            @PathVariable Long usuarioId,
            @RequestParam(required = false) String modulo) {

        if (usuarioId == null || usuarioId <= 0) {
            throw new BadRequestException("El ID del usuario debe ser un número positivo");
        }

        System.out.println("🔍 [DIAGNOSTICO SENIOR] Recibiendo petición de menú:");
        System.out.println("   > Usuario ID: " + usuarioId);
        System.out.println("   > Módulo solicitado: " + (modulo != null ? modulo : "null (se usará default: Sistema)"));
        
        List<MenuItemDTO> menu = menuService.obtenerMenuParaUsuario(usuarioId, modulo);
        
        System.out.println("   > Cantidad de grupos de menú encontrados: " + (menu != null ? menu.size() : 0));
        if (menu != null && menu.isEmpty()) {
            System.out.println("   ⚠️ [ALERTA] El menú regresó vacío. Verificando roles del usuario...");
        }


        return ResponseEntity.ok(ApiResponse.<List<MenuItemDTO>>builder()
                .success(true)
                .message("Menú obtenido exitosamente")
                .data(menu)
                .build());
    }
}
