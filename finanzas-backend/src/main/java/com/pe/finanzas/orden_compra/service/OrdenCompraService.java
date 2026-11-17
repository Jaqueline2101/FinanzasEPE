package com.pe.finanzas.orden_compra.service;

import com.pe.finanzas.orden_compra.dto.OrdenCompraRequest;
import com.pe.finanzas.orden_compra.dto.OrdenCompraResponse;

import java.util.List;

public interface OrdenCompraService {
    List<OrdenCompraResponse> listar();

    OrdenCompraResponse obtenerPorId(Long id);

    OrdenCompraResponse registrar(OrdenCompraRequest request);

    OrdenCompraResponse actualizar(Long id, OrdenCompraRequest request);

    void eliminar(Long id);

    OrdenCompraResponse aprobar(Long id, String usuario);

    OrdenCompraResponse anular(Long id, String usuario, String motivo);
}
