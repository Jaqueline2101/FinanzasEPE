package com.pe.finanzas.users.service.impl;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.pe.finanzas.users.entity.DatosPersonales;
import com.pe.finanzas.users.repository.DatosPersonalesRepository;
import com.pe.finanzas.roles.entity.Role;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements UserDetailsService {

    private final DatosPersonalesRepository datosPersonalesRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.debug("Cargando usuario financiero por login: {}", username);

        DatosPersonales user = datosPersonalesRepository.findByLogin(username)
                .orElseThrow(() -> {
                    log.error("Usuario no encontrado: {}", username);
                    return new UsernameNotFoundException("Usuario no encontrado: " + username);
                });

        // Initialize direct roles and their permissions
        user.getRoles().size();
        user.getRoles().forEach(r -> {
            if (r.getPermissions() != null) r.getPermissions().size();
            if (r.getParent() != null) {
                Role p = r.getParent();
                if (p.getPermissions() != null) p.getPermissions().size();
            }
        });
        
        // Initialize user branch roles and their permissions
        user.getUserBranchRoles().size();
        user.getUserBranchRoles().forEach(ubr -> {
            Role role = ubr.getRole();
            if (role.getPermissions() != null) role.getPermissions().size();
            if (role.getParent() != null) {
                Role p = role.getParent();
                if (p.getPermissions() != null) p.getPermissions().size();
            }
        });

        return user;
    }
}