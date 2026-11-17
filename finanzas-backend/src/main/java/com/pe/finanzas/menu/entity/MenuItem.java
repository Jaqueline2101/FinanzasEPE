package com.pe.finanzas.menu.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Entity
@Table(name = "menu_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_acceso")
    private com.pe.finanzas.accesos.entity.AccesoMain acceso;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "parent_id")
    private MenuItem parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orden ASC")
    @Builder.Default
    private List<MenuItem> children = new ArrayList<>();

    @Column(nullable = false, length = 100)
    private String titulo;

    @Column(length = 255)
    private String ruta;

    @Column(columnDefinition = "TEXT")
    private String icono;

    @Column(nullable = false)
    @Builder.Default
    private Integer orden = 0;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoMenuItem tipo = TipoMenuItem.ITEM;

    // ========================================
    // SISTEMA HÍBRIDO DE CONTROL DE ACCESO
    // ========================================
    @Column(name = "tipo_control", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private TipoControl tipoControl = TipoControl.MODULO;

    @Column(name = "modulo_requerido", length = 100)
    private String moduloRequerido;

    @Column(name = "permisos_requeridos", columnDefinition = "TEXT")
    private String permisosRequeridos;

    @Column(nullable = false)
    @Builder.Default
    private Boolean activo = true;

    // ========================================
    // ENUMS
    // ========================================

    public enum TipoMenuItem {
        GRUPO, // Sección principal (ej: CONFIGURACIONES)
        SUBMENU, // Subsección (ej: SEGURIDAD)
        ITEM // Item final con ruta
    }

    public enum TipoControl {
        PUBLICO, // No requiere verificación
        MODULO, // Requiere cualquier permiso del módulo
        PERMISOS, // Requiere AL MENOS UNO de los permisos
        PERMISOS_TODOS // Requiere TODOS los permisos
    }

    // ========================================
    // MÉTODOS DE UTILIDAD
    // ========================================

    public boolean requiereAcceso() {
        return tipoControl != null && tipoControl != TipoControl.PUBLICO;
    }

    public List<String> getPermisosRequeridosList() {
        if (permisosRequeridos == null || permisosRequeridos.trim().isEmpty()) {
            return new ArrayList<>();
        }

        try {
            // Parsear JSON simple: ["PERMISO_1", "PERMISO_2"]
            String cleaned = permisosRequeridos
                    .replace("[", "")
                    .replace("]", "")
                    .replace("\"", "")
                    .trim();

            if (cleaned.isEmpty()) {
                return new ArrayList<>();
            }

            return Arrays.asList(cleaned.split("\\s*,\\s*"));
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    public void setPermisosRequeridosList(List<String> permisos) {
        if (permisos == null || permisos.isEmpty()) {
            this.permisosRequeridos = null;
            return;
        }

        // Crear JSON simple
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < permisos.size(); i++) {
            if (i > 0)
                json.append(", ");
            json.append("\"").append(permisos.get(i)).append("\"");
        }
        json.append("]");

        this.permisosRequeridos = json.toString();
    }

    public void addChild(MenuItem child) {
        children.add(child);
        child.setParent(this);
    }

    public void removeChild(MenuItem child) {
        children.remove(child);
        child.setParent(null);
    }

    // ========================================
    // MÉTODOS DE DESCRIPCIÓN
    // ========================================

    public String getDescripcionControl() {
        if (tipoControl == null || tipoControl == TipoControl.PUBLICO) {
            return "Público - Sin restricciones";
        }

        switch (tipoControl) {
            case MODULO:
                return "Módulo: " + (moduloRequerido != null ? moduloRequerido : "No especificado");

            case PERMISOS:
                return "Permisos (al menos uno): " + getPermisosRequeridosList();

            case PERMISOS_TODOS:
                return "Permisos (todos): " + getPermisosRequeridosList();

            default:
                return "Desconocido";
        }
    }
}
