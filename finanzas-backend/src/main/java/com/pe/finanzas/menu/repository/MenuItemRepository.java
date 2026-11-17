package com.pe.finanzas.menu.repository;

import com.pe.finanzas.menu.entity.MenuItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    @Query("SELECT m FROM MenuItem m WHERE m.parent IS NULL AND m.activo = true AND UPPER(m.acceso.nombre) = UPPER(:accesoNombre) ORDER BY m.orden")
    List<MenuItem> findRootItemsByAcceso(@Param("accesoNombre") String accesoNombre);

    @Query("SELECT m FROM MenuItem m WHERE m.parent IS NULL AND m.activo = true ORDER BY m.orden")
    List<MenuItem> findRootItems();


    @Query("SELECT m FROM MenuItem m WHERE m.parent.id = :parentId AND m.activo = true ORDER BY m.orden")
    List<MenuItem> findByParentId(@Param("parentId") Long parentId);

    List<MenuItem> findByActivoTrueOrderByOrdenAsc();

    long countByAccesoIsNull();

    java.util.Optional<MenuItem> findFirstByTituloAndAcceso(String titulo, com.pe.finanzas.accesos.entity.AccesoMain acceso);
    java.util.List<MenuItem> findByTituloAndAcceso(String titulo, com.pe.finanzas.accesos.entity.AccesoMain acceso);

    java.util.Optional<MenuItem> findFirstByTitulo(String titulo);
    java.util.List<MenuItem> findByTitulo(String titulo);

    java.util.Optional<MenuItem> findByTituloAndParentAndAcceso(String titulo, MenuItem parent, com.pe.finanzas.accesos.entity.AccesoMain acceso);
}

