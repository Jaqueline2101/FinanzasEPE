package com.pe.finanzas.kardex.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.pe.finanzas.kardex.entity.Kardex;
import java.util.List;

@Repository
public interface KardexRepository extends JpaRepository<Kardex, String> {
    List<Kardex> findByIdCatalogoOrderByFechaDesc(Long idCatalogo);
    List<Kardex> findByIdAlmArticuloOrderByFechaDesc(Long idAlmArticulo);
    
    @org.springframework.data.jpa.repository.EntityGraph(attributePaths = {"catalogo", "sucursal", "usuario"})
    Page<Kardex> findAllByOrderByFechaDesc(Pageable pageable);
}
