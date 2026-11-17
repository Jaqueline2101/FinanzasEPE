package com.pe.finanzas.menu.service;

import com.pe.finanzas.menu.dto.MenuItemDTO;
import com.pe.finanzas.menu.entity.MenuItem;
import com.pe.finanzas.menu.repository.MenuItemRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class MenuService {

    private final MenuItemRepository menuItemRepository;
    private final DatosPersonalesRepository datosPersonalesRepository;

    @Transactional(readOnly = true)
    public List<MenuItemDTO> obtenerMenuParaUsuario(Long usuarioId, String modulo) {
        log.debug("🔍 Obteniendo menú para usuario ID: {} con módulo: {}", usuarioId, modulo);

        // Fallback default y mapeo de módulo legado
        if (modulo == null || modulo.trim().isEmpty() || "Finanzas".equalsIgnoreCase(modulo)) {
            if ("Finanzas".equalsIgnoreCase(modulo)) {
                log.info("🔄 Redirigiendo módulo legado 'Finanzas' a 'Sistema'");
            }
            modulo = "Sistema";
        }
        
        log.info("📂 Cargando menú para Módulo: {} (Usuario ID: {})", modulo, usuarioId);

        // Obtener usuario con sus permisos
        DatosPersonales usuario = datosPersonalesRepository.findByIdWithRoles(usuarioId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Obtener permisos efectivos del usuario
        Set<String> permisosUsuario = usuario.getEffectivePermissionNames();
        log.debug("👤 Usuario {} tiene {} permisos", usuarioId, permisosUsuario.size());

        // Extraer módulos de los permisos
        Set<String> modulosUsuario = extraerModulos(permisosUsuario);
        log.debug("📦 Usuario {} tiene acceso a {} módulos: {}",
                usuarioId, modulosUsuario.size(), modulosUsuario);

        // Lógica de "Super Admin": Verificamos tanto roles clásicos como roles por sucursal
        boolean esAdmin = usuario.getRoles().stream()
                .anyMatch(rol -> isAdministrativeRole(rol.getName()));
        
        if (!esAdmin && usuario.getUserBranchRoles() != null) {
            esAdmin = usuario.getUserBranchRoles().stream()
                    .anyMatch(ubr -> isAdministrativeRole(ubr.getRole().getName()));
        }

        if (esAdmin) {
            log.info("👑 Usuario {} reconocido como administrador, habilitando Sidebar completo", usuarioId);
        }

        // Obtener items raíz filtrados por el AccesoMain (Módulo)
        List<MenuItem> itemsRaiz = menuItemRepository.findRootItemsByAcceso(modulo);
        
        log.debug("📁 Items raíz encontrados para el módulo '{}': {}", modulo, itemsRaiz.size());
        
        log.debug("📁 Items raíz encontrados: {}", itemsRaiz.size());

        // Construir árbol de menú filtrado
        List<MenuItemDTO> menu = new ArrayList<>();
        for (MenuItem item : itemsRaiz) {
            MenuItemDTO dto = construirMenuItemDTO(item, permisosUsuario, modulosUsuario, esAdmin);
            if (dto != null && (dto.getChildren() == null || !dto.getChildren().isEmpty() || dto.getRuta() != null)) {
                menu.add(dto);
            }
        }

        log.debug("✅ Menú construido con {} grupos principales", menu.size());
        return menu;
    }

    private Set<String> extraerModulos(Set<String> permisos) {
        return permisos.stream()
                .map(permiso -> {
                    if (permiso.contains("_")) {
                        return permiso.split("_")[0];
                    }
                    return permiso;
                })
                .collect(Collectors.toSet());
    }

    private MenuItemDTO construirMenuItemDTO(MenuItem item,
            Set<String> permisosUsuario,
            Set<String> modulosUsuario,
            boolean esAdmin) {

        // Verificar acceso según el tipo de control
        if (!tieneAccesoAlItem(item, permisosUsuario, modulosUsuario, esAdmin)) {
            log.trace("❌ Usuario no tiene acceso al item: {} ({})",
                    item.getTitulo(), item.getDescripcionControl());
            return null;
        }

        // Crear DTO base
        MenuItemDTO dto = MenuItemDTO.builder()
                .id(item.getId())
                .titulo(item.getTitulo())
                .ruta(item.getRuta())
                .icono(item.getIcono())
                .orden(item.getOrden())
                .tipo(item.getTipo().name())
                .children(new ArrayList<>())
                .build();

        // Procesar hijos recursivamente
        if (item.getChildren() != null && !item.getChildren().isEmpty()) {
            for (MenuItem child : item.getChildren()) {
                MenuItemDTO childDto = construirMenuItemDTO(child, permisosUsuario, modulosUsuario, esAdmin);
                if (childDto != null) {
                    dto.getChildren().add(childDto);
                }
            }

            // Si es GRUPO o SUBMENU sin hijos visibles, no mostrarlo (A menos que sea Admin)
            if (dto.getChildren().isEmpty() && !esAdmin &&
                    (item.getTipo() == MenuItem.TipoMenuItem.GRUPO ||
                            item.getTipo() == MenuItem.TipoMenuItem.SUBMENU)) {
                log.trace("⚠️  Item {} no tiene hijos visibles, se omite", item.getTitulo());
                return null;
            }
        }

        log.trace("✅ Item incluido: {} ({})", item.getTitulo(), item.getDescripcionControl());
        return dto;
    }

    private boolean tieneAccesoAlItem(MenuItem item,
            Set<String> permisosUsuario,
            Set<String> modulosUsuario,
            boolean esAdmin) {

        // Admin tiene acceso a todo
        if (esAdmin) {
            return true;
        }

        // Si el item no requiere control, es público
        if (!item.requiereAcceso()) {
            log.trace("🔓 Item público: {}", item.getTitulo());
            return true;
        }

        // Verificar según el tipo de control
        switch (item.getTipoControl()) {
            case PUBLICO:
                return true;

            case MODULO:
                return verificarAccesoModulo(item, modulosUsuario);

            case PERMISOS:
                return verificarAccesoPermisosAny(item, permisosUsuario);

            case PERMISOS_TODOS:
                return verificarAccesoPermisosTodos(item, permisosUsuario);

            default:
                log.warn("⚠️  Tipo de control desconocido para item: {}", item.getTitulo());
                return false;
        }
    }

    /**
     * Verifica acceso por MÓDULO (cualquier permiso del módulo)
     */
    private boolean verificarAccesoModulo(MenuItem item, Set<String> modulosUsuario) {
        String moduloRequerido = item.getModuloRequerido();

        if (moduloRequerido == null || moduloRequerido.isEmpty()) {
            log.warn("⚠️  Item {} tiene tipo MODULO pero no especifica módulo", item.getTitulo());
            return false;
        }

        boolean tieneAcceso = modulosUsuario.contains(moduloRequerido);

        if (!tieneAcceso) {
            log.trace("🔒 No tiene módulo {} para {}", moduloRequerido, item.getTitulo());
        } else {
            log.trace("🔓 Tiene módulo {} para {}", moduloRequerido, item.getTitulo());
        }

        return tieneAcceso;
    }

    /**
     * Verifica acceso por PERMISOS (necesita AL MENOS UNO)
     */
    private boolean verificarAccesoPermisosAny(MenuItem item, Set<String> permisosUsuario) {
        List<String> permisosRequeridos = item.getPermisosRequeridosList();

        if (permisosRequeridos.isEmpty()) {
            log.warn("⚠️  Item {} tiene tipo PERMISOS pero no especifica permisos", item.getTitulo());
            return false;
        }

        boolean tieneAcceso = permisosRequeridos.stream()
                .anyMatch(permisosUsuario::contains);

        if (!tieneAcceso) {
            log.trace("🔒 No tiene ninguno de {} para {}", permisosRequeridos, item.getTitulo());
        } else {
            log.trace("🔓 Tiene al menos uno de {} para {}", permisosRequeridos, item.getTitulo());
        }

        return tieneAcceso;
    }

    /**
     * Verifica acceso por PERMISOS (necesita TODOS)
     */
    private boolean verificarAccesoPermisosTodos(MenuItem item, Set<String> permisosUsuario) {
        List<String> permisosRequeridos = item.getPermisosRequeridosList();

        if (permisosRequeridos.isEmpty()) {
            log.warn("⚠️  Item {} tiene tipo PERMISOS_TODOS pero no especifica permisos", item.getTitulo());
            return false;
        }

        boolean tieneAcceso = permisosRequeridos.stream()
                .allMatch(permisosUsuario::contains);

        if (!tieneAcceso) {
            List<String> faltantes = permisosRequeridos.stream()
                    .filter(p -> !permisosUsuario.contains(p))
                    .collect(Collectors.toList());
            log.trace("🔒 Le faltan {} de {} para {}", faltantes, permisosRequeridos, item.getTitulo());
        } else {
            log.trace("🔓 Tiene todos {} para {}", permisosRequeridos, item.getTitulo());
        }

        return tieneAcceso;
    }

    /**
     * Obtiene todos los items del menú (sin filtrar)
     */
    @Transactional(readOnly = true)
    public List<MenuItem> obtenerTodosLosItems() {
        return menuItemRepository.findAll();
    }

    /**
     * Obtiene los items raíz del menú
     */
    @Transactional(readOnly = true)
    public List<MenuItem> obtenerItemsRaiz() {
        return menuItemRepository.findRootItems();
    }

    private boolean isAdministrativeRole(String roleName) {
        return "SUPER_ADMIN".equalsIgnoreCase(roleName) || 
               "DTI".equalsIgnoreCase(roleName) || 
               "ADMINISTRADOR".equalsIgnoreCase(roleName);
    }
}
