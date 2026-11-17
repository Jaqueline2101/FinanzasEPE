package com.pe.finanzas.users.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.pe.finanzas.users.entity.DatosPersonales;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Repository
public interface DatosPersonalesRepository extends JpaRepository<DatosPersonales, Long> {

        Optional<DatosPersonales> findByLogin(String login);

        Optional<DatosPersonales> findByRuc(String ruc);

        Optional<DatosPersonales> findByEmail(String email);

        boolean existsByLogin(String login);

        boolean existsByEmail(String email);

        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "LEFT JOIN FETCH d.userBranchRoles ubr " +
                        "LEFT JOIN FETCH ubr.role ubrr " +
                        "LEFT JOIN FETCH ubrr.permissions " +
                        "WHERE d.login = :login")
        Optional<DatosPersonales> findByLoginWithRoles(@Param("login") String login);

        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "LEFT JOIN FETCH d.userBranchRoles ubr " +
                        "LEFT JOIN FETCH ubr.role ubrr " +
                        "LEFT JOIN FETCH ubrr.permissions " +
                        "WHERE d.email = :email")
        Optional<DatosPersonales> findByEmailWithRoles(@Param("email") String email);

        @Query("SELECT DISTINCT d FROM DatosPersonales d " +
                        "LEFT JOIN FETCH d.roles r " +
                        "LEFT JOIN FETCH r.permissions " +
                        "LEFT JOIN FETCH d.userBranchRoles ubr " +
                        "LEFT JOIN FETCH ubr.role ubrr " +
                        "LEFT JOIN FETCH ubrr.permissions " +
                        "WHERE d.id = :id")
        Optional<DatosPersonales> findByIdWithRoles(@Param("id") Long id);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO')")
        Page<DatosPersonales> findAllWithRoles(Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.active = true")
        Page<DatosPersonales> findAllActive(Pageable pageable);

        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r WHERE r.name = :roleName")
        Page<DatosPersonales> findByRoleName(@Param("roleName") String roleName, Pageable pageable);

        @Query("SELECT COUNT(d) FROM DatosPersonales d WHERE d.active = true")
        long countActiveUsers();

        @Query("SELECT COUNT(DISTINCT d) FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO')")
        long countTotalUsersWithRoles();

        @Query("SELECT COUNT(DISTINCT d) FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.active = :active")
        long countByActiveWithRoles(@Param("active") boolean active);

        Optional<DatosPersonales> findByNumdoc(String numdoc);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchByName(@Param("term") String term, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%'))")
        Page<DatosPersonales> searchByEmail(@Param("email") String email, Pageable pageable);

        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r WHERE d.active = true AND LOWER(r.name) LIKE LOWER(CONCAT('%', :roleName, '%'))")
        Page<DatosPersonales> findActiveByRoleName(@Param("roleName") String roleName, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND LOWER(d.login) LIKE LOWER(CONCAT('%', :login, '%'))")
        Page<DatosPersonales> searchByLogin(@Param("login") String login, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchByAnyTerm(@Param("term") String term, Pageable pageable);

        @Query("SELECT COUNT(d) FROM DatosPersonales d JOIN d.roles r WHERE r.id = :roleId")
        long countByRolesId(@Param("roleId") Long roleId);

        @Query("SELECT DISTINCT d FROM DatosPersonales d LEFT JOIN d.roles r WHERE (d.apepat IS NOT NULL AND d.apepat <> '') AND d.id NOT IN (SELECT d2.id FROM DatosPersonales d2 JOIN d2.roles r2 WHERE r2.name = 'PACIENTE')")
        List<DatosPersonales> findAllEmpleados();
        // ==========================================
        // MÉTODOS MULTI-TENANCY (FILTRADO POR SUCURSAL)
        // ==========================================

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.sucursalActual.id = :sucursalId")
        Page<DatosPersonales> findAllBySucursalActualId(@Param("sucursalId") Long sucursalId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.active = true AND d.sucursalActual.id = :sucursalId")
        Page<DatosPersonales> findAllActiveBySucursalActualId(@Param("sucursalId") Long sucursalId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.sucursalActual.id = :sucursalId AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchByNameAndSucursal(@Param("term") String term, @Param("sucursalId") Long sucursalId,
                        Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.sucursalActual.id = :sucursalId AND LOWER(d.email) LIKE LOWER(CONCAT('%', :email, '%'))")
        Page<DatosPersonales> searchByEmailAndSucursal(@Param("email") String email,
                        @Param("sucursalId") Long sucursalId,
                        Pageable pageable);

        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r WHERE d.sucursalActual.id = :sucursalId AND d.active = true AND LOWER(r.name) LIKE LOWER(CONCAT('%', :roleName, '%'))")
        Page<DatosPersonales> findActiveByRoleNameAndSucursal(@Param("roleName") String roleName,
                        @Param("sucursalId") Long sucursalId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.sucursalActual.id = :sucursalId AND LOWER(d.login) LIKE LOWER(CONCAT('%', :login, '%'))")
        Page<DatosPersonales> searchByLoginAndSucursal(@Param("login") String login,
                        @Param("sucursalId") Long sucursalId,
                        Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.sucursalActual.id = :sucursalId AND ("
                        +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.email) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchByAnyTermAndSucursal(@Param("term") String term,
                        @Param("sucursalId") Long sucursalId,
                        Pageable pageable);

        // MÉTODOS PARA BUSCAR PACIENTES (TODOS LOS USUARIOS ACTIVOS, SIN FILTRO DE ROL)
        @Query("SELECT DISTINCT d FROM DatosPersonales d WHERE d.active = true AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "d.numdoc LIKE CONCAT('%', :term, '%') OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchPacienteAll(@Param("term") String term, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d WHERE d.active = true AND (d.sucursalActual.id = :sucursalId OR d.sucursalActual IS NULL) AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "d.numdoc LIKE CONCAT('%', :term, '%') OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchPacienteBySucursal(@Param("term") String term, @Param("sucursalId") Long sucursalId, Pageable pageable);

        @Query("SELECT d FROM DatosPersonales d JOIN d.roles r WHERE r.name = :roleName AND d.sucursalActual.id = :sucursalId")
        Page<DatosPersonales> findByRoleNameAndSucursal(@Param("roleName") String roleName,
                        @Param("sucursalId") Long sucursalId, Pageable pageable);

        java.util.List<DatosPersonales> findTop10ByRucStartingWithOrNumdocStartingWithOrNombreContainingIgnoreCase(
                        String ruc, String numdoc, String nombre);

        List<DatosPersonales> findByTipoPacIn(List<String> tipos);

        @Query("SELECT DISTINCT d FROM DatosPersonales d WHERE d.active = true AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "d.numdoc LIKE CONCAT('%', :term, '%') OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchPersonasSinFiltroRol(@Param("term") String term, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d WHERE d.active = true AND (d.sucursalActual.id = :sucursalId OR d.sucursalActual IS NULL) AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "d.numdoc LIKE CONCAT('%', :term, '%') OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchPersonasSinFiltroRolAndSucursal(@Param("term") String term, @Param("sucursalId") Long sucursalId, Pageable pageable);

        @Query("SELECT DISTINCT d FROM DatosPersonales d JOIN d.roles r WHERE r.name NOT IN ('PACIENTE', 'USUARIO') AND d.active = true AND (" +
                        "LOWER(d.nombre) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apepat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(d.apemat) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "d.numdoc LIKE CONCAT('%', :term, '%') OR " +
                        "LOWER(d.login) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat)) LIKE LOWER(CONCAT('%', :term, '%')) OR " +
                        "LOWER(CONCAT(d.nombre, ' ', d.apepat, ' ', COALESCE(d.apemat, ''))) LIKE LOWER(CONCAT('%', :term, '%')))")
        Page<DatosPersonales> searchPersonasWithRolFilter(@Param("term") String term, Pageable pageable);
}
