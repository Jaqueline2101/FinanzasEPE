package com.pe.finanzas.users.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.pe.finanzas.permissions.entity.Permission;
import com.pe.finanzas.roles.entity.Role;
import com.pe.finanzas.sucursales.entity.Sucursal;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Entity
@Table(name = "datos_personales")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@com.fasterxml.jackson.annotation.JsonIdentityInfo(generator = com.fasterxml.jackson.annotation.ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class DatosPersonales implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(length = 200)
    private String nombre;

    @Column(name = "ver_nombre", length = 200)
    private String verNombre;

    @Column(name = "apemat", length = 300)
    private String apemat;

    @Column(name = "apepat", length = 300)
    private String apepat;

    @Column(length = 3)
    private String sexo;

    @Column(name = "nacfec")
    private LocalDate nacfec;

    @Column(length = 12)
    private String rhc;

    @Column(name = "tipodoc", length = 2)
    private String tipodoc;

    @Column(name = "numdoc", length = 15)
    private String numdoc;

    @Column(name = "ruc", length = 11)
    private String ruc;

    @Column(length = 200)
    private String direcc;

    @Column(name = "fon_local", length = 25)
    private String fonLocal;

    @Column(name = "fono1", length = 20)
    private String fono1;

    @Column(name = "login", length = 30)
    private String login;

    @Column(name = "passwd", length = 250)
    private String passwd;

    @Column(length = 40)
    private String email;

    @Column(name = "id_compania")
    private Long idCompania;

    @Column(name = "id_empresa")
    private Long idEmpresa;

    @Column(name = "tipo_pac", length = 20)
    private String tipoPac;

    @Column(name = "observacion", columnDefinition = "TEXT") // Cambia "observaciones" por "observacion"
    private String observaciones;

    // --- SECCIÓN VINCULACIÓN ---
    @Column(name = "fecha_fin")
    private LocalDate fechaFin;

    @Column(name = "renovacion")
    private Boolean renovacion;

    @Column(name = "cargo", length = 100)
    private String cargo;

    @Column(name = "funcion_principal", length = 255)
    private String funcionPrincipal;

    @Column(name = "area", length = 100)
    private String area;

    @Column(name = "tipo_contrato", length = 100)
    private String tipoContrato;

    @Column(name = "modalidad_trabajo", length = 100)
    private String modalidadTrabajo;

    @Column(name = "modalidad_pago", length = 100)
    private String modalidadPago;

    @Column(name = "jefe_inmediato", length = 100)
    private String jefeInmediato;

    @Column(name = "centro_costo", length = 100)
    private String centroCosto;

    @Column(name = "motivo_cese", columnDefinition = "TEXT")
    private String motivoCese;

    @Column(name = "estado_vinculo", length = 20)
    private String estadoVinculo;

    // --- SECCIÓN ECONÓMICOS ---
    @Column(name = "monto_acordado", precision = 12, scale = 2)
    private java.math.BigDecimal montoAcordado;

    @Column(name = "moneda", length = 10)
    private String moneda;

    @Column(name = "tipo_pago", length = 50)
    private String tipoPago;

    @Column(name = "frecuencia_pago", length = 50)
    private String frecuenciaPago;

    @Column(name = "retencion_aplica")
    private Boolean retencionAplica;

    @Column(name = "porcentaje_retencion", precision = 5, scale = 2)
    private java.math.BigDecimal porcentajeRetencion;

    @Column(name = "cuenta_contable_asociada", length = 50)
    private String cuentaContableAsociada;

    @Column(name = "observaciones_pago", columnDefinition = "TEXT")
    private String observacionesPago;

    // --- SECCIÓN ESPECÍFICOS TRABAJADOR ---
    @Column(name = "tipo_planilla", length = 50)
    private String tipoPlanilla;

    @Column(name = "afp_onp", length = 50)
    private String afpOnp;

    @Column(name = "numero_afp", length = 50)
    private String numeroAfp;

    @Column(name = "regimen_laboral", length = 100)
    private String regimenLaboral;

    @Column(name = "jornada_laboral", length = 100)
    private String jornadaLaboral;

    @Column(name = "sueldo_base", precision = 12, scale = 2)
    private java.math.BigDecimal sueldoBase;

    @Column(name = "bonificaciones", precision = 12, scale = 2)
    private java.math.BigDecimal bonificaciones;

    // --- SECCIÓN ESPECÍFICOS MÉDICO ---
    @Column(name = "especialidad", length = 100)
    private String especialidad;

    @Column(name = "numero_colegiatura", length = 50)
    private String numeroColegiatura;

    @Column(name = "rne", length = 50)
    private String rne;

    @Column(name = "turno", length = 50)
    private String turno;

    @Column(name = "tipo_atencion", length = 50)
    private String tipoAtencion;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login")
    private LocalDateTime lastLogin;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @Builder.Default
    private Set<UserBranchRole> userBranchRoles = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<UserPermission> directPermissions = new HashSet<>();

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "usuario_sucursales_asignadas", joinColumns = @JoinColumn(name = "usuario_id", referencedColumnName = "id"), inverseJoinColumns = @JoinColumn(name = "sucursal_id"))
    @Builder.Default
    @ToString.Exclude
    private Set<Sucursal> sucursalesAsignadas = new HashSet<>();

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sucursal_actual_id")
    @ToString.Exclude
    private Sucursal sucursalActual;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "punto_venta_actual_id")
    @ToString.Exclude
    private com.pe.finanzas.puntos.entity.Punto puntoVentaActual;

    @Column(name = "id_ocupac")
    private Long idOcupac;

    @Column(name = "nom_ocupacion", length = 200)
    private String nomOcupacion;

    @Column(name = "cent_labor", length = 200)
    private String centLabor;

    @Column(name = "fecha_ing")
    private LocalDate fechaIng;

    @Column(name = "estciv", length = 2)
    private String estciv;

    @Column(name = "use_direct_permissions", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean useDirectPermissions = false;

    @Column(name = "foto_perfil", columnDefinition = "TEXT")
    private String fotoPerfil;

    @Column(name = "must_change_password", nullable = false, columnDefinition = "boolean default false")
    @Builder.Default
    private Boolean mustChangePassword = false;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Override
    public String getPassword() {
        return passwd;
    }

    @Override
    public String getUsername() {
        return login;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return active;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return active;
    }

    public Set<String> getPermissionNames() {
        Set<String> permissions = new HashSet<>();
        
        // Add from direct roles
        if (roles != null && !roles.isEmpty()) {
            permissions.addAll(roles.stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .toList());
        }
        
        // Add from user branch roles
        if (userBranchRoles != null && !userBranchRoles.isEmpty()) {
            permissions.addAll(userBranchRoles.stream()
                .filter(ubr -> ubr.getActive() != null && ubr.getActive())
                .flatMap(ubr -> ubr.getRole().getPermissions().stream())
                .map(Permission::getName)
                .toList());
        }
        
        return permissions;
    }

    public Set<String> getRoleNames() {
        Set<String> rolesSet = new HashSet<>();
        
        // Add direct roles
        if (roles != null && !roles.isEmpty()) {
            rolesSet.addAll(roles.stream()
                .map(Role::getName)
                .toList());
        }
        
        // Add from user branch roles
        if (userBranchRoles != null && !userBranchRoles.isEmpty()) {
            rolesSet.addAll(userBranchRoles.stream()
                .filter(ubr -> ubr.getActive() != null && ubr.getActive())
                .map(ubr -> ubr.getRole().getName())
                .toList());
        }
        
        return rolesSet;
    }

    public boolean hasRole(String roleName) {
        return userBranchRoles.stream()
                .anyMatch(ubr -> ubr.getRole().getName().equalsIgnoreCase(roleName));
    }

    public String getFullName() {
        if (nombre == null || nombre.trim().isEmpty()) {
            return login != null ? login : String.valueOf(id);
        }

        StringBuilder sb = new StringBuilder(nombre.trim());
        if (apepat != null && !apepat.trim().isEmpty()) {
            sb.append(" ").append(apepat.trim());
        }
        if (apemat != null && !apemat.trim().isEmpty()) {
            sb.append(" ").append(apemat.trim());
        }

        return sb.toString();
    }

    public Collection<? extends GrantedAuthority> getAuthorities() {
        return getAuthoritiesByModulo(null);
    }

    public Collection<? extends GrantedAuthority> getAuthoritiesByModulo(String moduloNombre) {
        Set<GrantedAuthority> authorities = new HashSet<>();
        
        log.info("=== getAuthoritiesByModulo called (login: {}, id: {}, modulo: {}) ===", this.login, this.id, moduloNombre);
        log.info("  - direct roles count: {}", (this.roles != null ? this.roles.size() : 0));
        if (this.roles != null) {
            this.roles.forEach(r -> log.info("  - direct role: {}", r.getName()));
        }
        log.info("  - userBranchRoles count: {}", (this.userBranchRoles != null ? this.userBranchRoles.size() : 0));
        if (this.userBranchRoles != null) {
            this.userBranchRoles.forEach(ubr -> log.info("  - userBranchRole: role={}, active={}", ubr.getRole().getName(), ubr.getActive()));
        }
        
        // Add permissions from direct roles
        if (roles != null && !roles.isEmpty()) {
            log.info("  - adding permissions from direct roles");
            roles.forEach(role -> {
                log.info("    processing direct role: {}", role.getName());
                boolean matchesModulo = (moduloNombre == null) ||
                        (role.getAcceso() != null && moduloNombre.equalsIgnoreCase(role.getAcceso().getNombre()));

                if (matchesModulo) {
                    log.info("    adding role authority: ROLE_{}", role.getName());
                    authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                }

                if (role.getPermissions() != null) {
                    role.getPermissions().forEach(permission -> {
                        boolean permMatches = (moduloNombre == null) ||
                                (permission.getAcceso() != null
                                        && moduloNombre.equalsIgnoreCase(permission.getAcceso().getNombre()));

                        if (permMatches) {
                            log.info("    adding permission (direct role): {}", permission.getName());
                            authorities.add(new SimpleGrantedAuthority(permission.getName()));
                        }
                    });
                }
            });
        }

        // Add permissions from user branch roles
        if (userBranchRoles != null && !userBranchRoles.isEmpty()) {
            log.info("  - adding permissions from userBranchRoles");
            userBranchRoles.stream()
                .filter(ubr -> ubr.getActive() != null && ubr.getActive())
                .forEach(ubr -> {
                    Role role = ubr.getRole();
                    log.info("    processing userBranchRole: {}", role.getName());
                    if (role != null) {
                        boolean matchesModulo = (moduloNombre == null) ||
                                (role.getAcceso() != null && moduloNombre.equalsIgnoreCase(role.getAcceso().getNombre()));

                        if (matchesModulo) {
                            log.info("    adding role authority (userBranchRole): ROLE_{}", role.getName());
                            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));
                        }

                        if (role.getPermissions() != null) {
                            log.info("      role has {} permissions", role.getPermissions().size());
                            role.getPermissions().forEach(permission -> {
                                boolean permMatches = (moduloNombre == null) ||
                                        (permission.getAcceso() != null
                                                && moduloNombre.equalsIgnoreCase(permission.getAcceso().getNombre()));

                                if (permMatches) {
                                    log.info("    adding permission (userBranchRole): {}", permission.getName());
                                    authorities.add(new SimpleGrantedAuthority(permission.getName()));
                                }
                            });
                        }
                    }
                });
        }

        // Add direct permissions if enabled
        if (useDirectPermissions != null && useDirectPermissions && directPermissions != null) {
            log.info("  - adding direct permissions");
            directPermissions.forEach(up -> {
                boolean isActive = (up.getActive() != null && up.getActive());
                boolean isNotExpired = (up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()));

                if (isActive && isNotExpired && up.getPermission() != null) {
                    boolean permMatches = (moduloNombre == null) ||
                                (up.getPermission().getAcceso() != null
                                        && moduloNombre.equalsIgnoreCase(up.getPermission().getAcceso().getNombre()));

                    if (permMatches) {
                        log.info("    adding direct permission: {}", up.getPermission().getName());
                        authorities.add(new SimpleGrantedAuthority(up.getPermission().getName()));
                    }
                }
            });
        }
        
        log.info("=== Total authorities returned: {} ===", authorities.size());
        authorities.forEach(a -> log.info("  - authority: {}", a.getAuthority()));

        return authorities;
    }

    public Set<Permission> getEffectivePermissions() {
        Set<Permission> permissions = new HashSet<>();

        // Add permissions from direct roles
        if (roles != null && !roles.isEmpty()) {
            roles.forEach(role -> {
                permissions.addAll(getRecursivePermissions(role));
            });
        }

        // Add permissions from user branch roles
        if (userBranchRoles != null && !userBranchRoles.isEmpty()) {
            userBranchRoles.stream()
                .filter(ubr -> ubr.getActive() != null && ubr.getActive())
                .forEach(ubr -> {
                    Role role = ubr.getRole();
                    if (role != null) {
                        permissions.addAll(getRecursivePermissions(role));
                    }
                });
        }

        // Add direct permissions if enabled
        if (useDirectPermissions != null && useDirectPermissions && directPermissions != null) {
            directPermissions.stream()
                .filter(up -> up.getActive() != null && up.getActive())
                .filter(up -> up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()))
                .forEach(up -> {
                    if (up.getPermission() != null) {
                        permissions.add(up.getPermission());
                    }
                });
        }

        return permissions;
    }

    private Set<Permission> getRecursivePermissions(Role role) {
        Set<Permission> perms = new HashSet<>();
        if (role.getPermissions() != null) {
            perms.addAll(role.getPermissions());
        }
        if (role.getParent() != null) {
            perms.addAll(getRecursivePermissions(role.getParent()));
        }
        return perms;
    }

    public Set<String> getEffectivePermissionNames() {
        return getEffectivePermissions().stream()
                .map(Permission::getName)
                .collect(java.util.stream.Collectors.toSet());
    }

    public boolean hasPermission(String permissionName) {
        return getEffectivePermissionNames().contains(permissionName);
    }

    public long countActiveDirectPermissions() {
        return directPermissions.stream()
                .filter(up -> up.getActive())
                .filter(up -> up.getExpiresAt() == null || up.getExpiresAt().isAfter(LocalDateTime.now()))
                .count();
    }

    public String getPermissionMode() {
        return (useDirectPermissions != null && useDirectPermissions) ? "DIRECT" : "ROLE";
    }
}
