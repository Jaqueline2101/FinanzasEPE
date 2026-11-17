package com.pe.finanzas.productos.service;

import com.pe.finanzas.productos.entity.Producto;
import java.util.List;
import java.util.Optional;

public interface ProductoService {
    List<Producto> findAll();

    List<Producto> findBySucursal(Long idSucursal);

    Optional<Producto> findById(Long id);

    Producto save(Producto producto);

    Producto update(Long id, Producto producto);

    void delete(Long id);
}
