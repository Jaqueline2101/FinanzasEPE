package com.pe.finanzas.productos.repository;

import com.pe.finanzas.productos.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    List<Producto> findByIdSucursal(Long idSucursal);

    List<Producto> findByIdCatalogo(Long idCatalogo);

    @org.springframework.data.jpa.repository.Query("SELECT p FROM Producto p WHERE p.idCatalogo = :idCatalogo AND p.stockReal > 0 ORDER BY p.fechaVencimiento ASC")
    List<Producto> findAvailableLotesOrderByVencimiento(@org.springframework.data.repository.query.Param("idCatalogo") Long idCatalogo);
}
