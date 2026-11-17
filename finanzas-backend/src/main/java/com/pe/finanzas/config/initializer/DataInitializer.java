package com.pe.finanzas.config.initializer;

import com.pe.finanzas.accesos.entity.AccesoMain;
import com.pe.finanzas.accesos.repository.AccesoMainRepository;
import com.pe.finanzas.permissions.entity.Permission;
import com.pe.finanzas.permissions.repository.PermissionRepository;
import com.pe.finanzas.menu.entity.MenuItem;
import com.pe.finanzas.menu.repository.MenuItemRepository;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.roles.repository.RoleRepository;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.sucursales.repository.SucursalRepository;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.entity.UserBranchRole;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.users.repository.UserBranchRoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;
import java.util.ArrayList;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final PermissionRepository permRepo;
    private final RoleRepository roleRepo;
    private final DatosPersonalesRepository userRepo;
    private final SucursalRepository sucursalRepo;
    private final AccesoMainRepository accesoRepo;
    private final UserBranchRoleRepository ubrRepo;
    private final MenuItemRepository menuRepo;
    private final PasswordEncoder encoder;
    private final ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) throws Exception {
        log.info("🚀 Starting Clean Professional System Seeding (JSON-Driven)...");

        fixSchemaIfNeeded();

        // Cargar datos desde JSON
        JsonNode root;
        try (InputStream is = new ClassPathResource("data/sidebar/seed-data.json").getInputStream()) {
            root = objectMapper.readTree(is);
        } catch (Exception e) {
            log.error("❌ CRITICAL ERROR: Could not load seed-data.json", e);
            return;
        }

        // 1. Seguridad Esencial (Bootstrap)
        AccesoMain sysAccess = seedSystemAccess();
        seedPermissionsFromJson(sysAccess, root.get("permissions"));
        seedRoles(sysAccess);
        Sucursal mainBranch = seedMainBranch();
        seedSuperAdmin(mainBranch);

        // 2. Sidebar Completo (Recursivo desde JSON)
        log.info("🌿 Sincronizando Sidebar desde JSON...");
        seedMenusFromJson(sysAccess, root.get("menus"), null);

        // 3. Documentos y Pacientes (Legacy Seeding)
        seedDocumentTypesFromJson(root.get("documentTypes"));
        seedPatientTypesFromJson(root.get("patientTypes"));

        log.info("✅ SYSTEM TOTALLY SYNCED FROM JSON!");
    }

    private void seedPermissionsFromJson(AccesoMain access, JsonNode permsNode) {
        if (permsNode == null) return;
        permsNode.forEach(p -> {
            String name = p.get(0).asText();
            String desc = p.get(1).asText();
            Permission perm = permRepo.findFirstByName(name)
                    .orElseGet(() -> Permission.builder().name(name).code(name).build());
            perm.setDescription(desc);
            perm.setModule(access.getNombre());
            perm.setAcceso(access);
            perm.setActive(true);
            permRepo.save(perm);
        });
    }

    private void seedMenusFromJson(AccesoMain access, JsonNode menusNode, MenuItem parent) {
        if (menusNode == null) return;
        for (JsonNode m : menusNode) {
            String title = m.get("titulo").asText();
            String icon = m.has("icono") ? m.get("icono").asText() : null;
            int order = m.get("orden").asInt();
            MenuItem.TipoMenuItem tipo = MenuItem.TipoMenuItem.valueOf(m.get("tipo").asText());
            MenuItem.TipoControl control = MenuItem.TipoControl.valueOf(m.get("control").asText());
            String ruta = m.has("ruta") ? m.get("ruta").asText() : null;
            List<String> perms = new ArrayList<>();
            if (m.has("permisos")) {
                m.get("permisos").forEach(p -> perms.add(p.asText()));
            }

            MenuItem item = upsertMenuItem(title, icon, order, access, parent, tipo, control, perms.isEmpty() ? null : perms, ruta);
            
            if (m.has("children")) {
                seedMenusFromJson(access, m.get("children"), item);
            }
        }
    }

    private void seedDocumentTypesFromJson(JsonNode docTypesNode) {
        if (docTypesNode == null) return;
        for (JsonNode doc : docTypesNode) {
            String sql = "INSERT INTO tipo_documento (codigo, tipo_doc, nombre, descripcion, requiere_ruc, activo, factor) " +
                    "VALUES (?, ?, ?, ?, ?, true, 1) " +
                    "ON CONFLICT (codigo) DO UPDATE SET tipo_doc = EXCLUDED.tipo_doc, nombre = EXCLUDED.nombre";
            jdbcTemplate.update(sql, doc.get(0).asText(), doc.get(0).asText(), doc.get(1).asText(), doc.get(2).asText(), Boolean.parseBoolean(doc.get(3).asText()));
        }
    }

    private void seedPatientTypesFromJson(JsonNode patientTypesNode) {
        if (patientTypesNode == null) return;
        for (JsonNode type : patientTypesNode) {
            String sql = "INSERT INTO tipo_paciente (codigo, nombre, estado, dsc, cob) " +
                    "VALUES (?, ?, ?, 0, 0) " +
                    "ON CONFLICT (codigo) DO UPDATE SET nombre = EXCLUDED.nombre, estado = EXCLUDED.estado";
            jdbcTemplate.update(sql, type.get(0).asText(), type.get(1).asText(), Integer.parseInt(type.get(2).asText()));
        }
    }

    private void fixSchemaIfNeeded() {
        try {
            jdbcTemplate.execute("ALTER TABLE menu_items ALTER COLUMN icono TYPE TEXT");
            jdbcTemplate.execute("ALTER TABLE menu_items ALTER COLUMN titulo TYPE VARCHAR(100)");
            jdbcTemplate.execute("ALTER TABLE menu_items ALTER COLUMN ruta TYPE VARCHAR(255)");
        } catch (Exception e) {}
    }

    private AccesoMain seedSystemAccess() {
        return accesoRepo.findFirstByNombreIgnoreCase("SISTEMA").orElseGet(() -> {
            AccesoMain acc = AccesoMain.builder()
                    .nombre("SISTEMA")
                    .escala("NORMAL")
                    .tema("DEFAULT")
                    .build();
            return accesoRepo.save(acc);
        });
    }

    private void seedRoles(AccesoMain access) {
        List<String> roleNames = List.of("SUPER_ADMIN", "ADMINISTRADOR", "DTI", "CAJERO", "SUPERVISOR", "DIRECTOR", "CONTABILIDAD", "PACIENTE", "USUARIO");
        roleNames.forEach(name -> {
            if (roleRepo.findFirstByName(name).isEmpty()) {
                roleRepo.save(Role.builder().name(name).description(name.replace("_", " ")).acceso(access).active(true).alcance("GLOBAL").build());
            }
        });

        Set<Permission> allPerms = new HashSet<>(permRepo.findAll());
        assignPermsToRole("SUPER_ADMIN", allPerms);
        assignPermsToRole("DTI", allPerms);
        assignPermsToRole("ADMINISTRADOR", allPerms);

        assignPermsToRole("CAJERO", getPermsByPrefixes(List.of("VENTA_", "CAJA_", "REIMPRESION_", "PUNTO_LEER", "PAQUETE_", "USUARIO_", "CONVENIO_")));
        assignPermsToRole("CONTABILIDAD", getPermsByPrefixes(List.of("COMPRA_", "REPORTE_", "PLAN_", "CAJA_", "SUCURSAL_", "EMPRESA_", "CONVENIO_")));
        assignPermsToRole("DIRECTOR", getPermsByPrefixes(List.of("RRHH_", "EMPLEADO_", "PAGO_", "REPORTE_", "PAQUETE_", "CATALOGO_", "COMPANIA_")));
        assignPermsToRole("SUPERVISOR", getPermsByPrefixes(List.of("CAJA_", "VENTA_", "REPORTE_", "PUNTO_")));
    }

    private Set<Permission> getPermsByPrefixes(List<String> prefixes) {
        Set<Permission> filtered = new HashSet<>();
        List<Permission> all = permRepo.findAll();
        for (Permission p : all) {
            for (String prefix : prefixes) {
                if (p.getName().startsWith(prefix)) { filtered.add(p); break; }
            }
        }
        return filtered;
    }

    private void assignPermsToRole(String roleName, Set<Permission> perms) {
        roleRepo.findFirstByName(roleName).ifPresent(role -> {
            role.setPermissions(perms);
            roleRepo.save(role);
        });
    }

    private Sucursal seedMainBranch() {
        return sucursalRepo.findAll().stream().filter(s -> "SEDE PRINCIPAL".equals(s.getNombreSucursal())).findFirst()
                .orElseGet(() -> {
                    Sucursal s = new Sucursal();
                    s.setNombreSucursal("SEDE PRINCIPAL");
                    s.setDireccion("SEDE PRINCIPAL");
                    s.setEstado(1);
                    return sucursalRepo.save(s);
                });
    }

    private void seedSuperAdmin(Sucursal branch) {
        DatosPersonales admin = userRepo.findByLogin("admin").orElseGet(() -> {
            DatosPersonales u = new DatosPersonales();
            u.setLogin("admin");
            u.setEmail("admin@sistema.com");
            u.setPasswd(encoder.encode("admin123"));
            u.setNombre("ADMIN");
            u.setApepat("SISTEMA");
            u.setActive(true);
            u.setSucursalActual(branch);
            return userRepo.save(u);
        });
        Role adminRole = roleRepo.findFirstByName("SUPER_ADMIN").orElseThrow();
        if (!ubrRepo.existsByUserAndBranchAndRole(admin, branch, adminRole)) {
            ubrRepo.save(UserBranchRole.builder().user(admin).branch(branch).role(adminRole).active(true).build());
        }
        if (!admin.getRoleNames().contains("SUPER_ADMIN")) {
            if (admin.getRoles() == null) admin.setRoles(new HashSet<>());
            admin.getRoles().add(adminRole);
            userRepo.save(admin);
        }
    }

    private MenuItem upsertMenuItem(String title, String icon, int order, AccesoMain access, MenuItem parent, MenuItem.TipoMenuItem tipo, MenuItem.TipoControl control, List<String> perms, String ruta) {
        MenuItem item = menuRepo.findByTituloAndParentAndAcceso(title, parent, access)
                .orElseGet(() -> menuRepo.findByTituloAndParentAndAcceso(title, parent, null).orElse(new MenuItem()));

        item.setTitulo(title);
        item.setAcceso(access);
        item.setParent(parent);
        item.setIcono(icon);
        item.setOrden(order);
        item.setTipo(tipo);
        item.setTipoControl(control);
        item.setRuta(ruta);
        item.setActivo(true);

        if (perms != null) {
            item.setPermisosRequeridosList(perms);
        } else {
            item.setPermisosRequeridos(null);
        }

        return menuRepo.save(item);
    }
}
