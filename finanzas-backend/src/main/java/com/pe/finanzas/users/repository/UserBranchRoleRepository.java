package com.pe.finanzas.users.repository;

import com.pe.finanzas.users.entity.UserBranchRole;
import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.sucursales.entity.Sucursal;
import com.pe.finanzas.roles.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserBranchRoleRepository extends JpaRepository<UserBranchRole, Long> {
    
    List<UserBranchRole> findByUser(DatosPersonales user);
    
    List<UserBranchRole> findByUserAndBranch(DatosPersonales user, Sucursal branch);
    
    Optional<UserBranchRole> findByUserAndBranchAndRole(DatosPersonales user, Sucursal branch, Role role);
    
    boolean existsByUserAndBranchAndRole(DatosPersonales user, Sucursal branch, Role role);
}
