package com.pe.finanzas.compra.service;

import com.pe.finanzas.compra.dto.CompraDTO;
import com.pe.finanzas.compra.entity.Compra;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import com.pe.finanzas.compra.entity.CronogramaPago;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface CompraService {

    Compra registrarCompra(CompraDTO dto);


    Page<CronogramaPago> listarDeudas(Map<String, Object> params, Pageable pageable);

    CronogramaPago registrarPagoCuota(Long idCronograma, String numeroOperacion, MultipartFile voucher);

    java.util.List<CronogramaPago> registrarPagosMasivos(java.util.List<Long> idsCronograma, String numeroOperacion, MultipartFile voucher);

    Optional<Compra> findById(Long id);

    Page<Compra> listBySucursal(
            Long idSucursal,
            String term,
            String searchType,
            String estado,
            String fechaDesde,
            String fechaHasta,
            Pageable pageable);

    void eliminarCompra(Long id);
}
