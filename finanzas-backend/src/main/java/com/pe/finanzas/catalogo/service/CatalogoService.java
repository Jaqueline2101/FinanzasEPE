package com.pe.finanzas.catalogo.service;

import com.pe.finanzas.catalogo.dto.request.CreateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.request.UpdateCatalogoRequest;
import com.pe.finanzas.catalogo.dto.response.CatalogoResponse;
import com.pe.finanzas.shared.dto.PageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface CatalogoService {

    CatalogoResponse createServicio(CreateCatalogoRequest request);

    CatalogoResponse getServicioById(Long id);

    CatalogoResponse getServicioByCodigo(String codigo);

    PageResponse<CatalogoResponse> getAllServicios(Pageable pageable);

    PageResponse<CatalogoResponse> getServiciosByEstado(Integer estado, Pageable pageable);

    PageResponse<CatalogoResponse> getServiciosByTipoAtencion(Long idTipoAtencion, Pageable pageable);

    PageResponse<CatalogoResponse> searchServicios(String searchTerm, Pageable pageable);

    PageResponse<CatalogoResponse> searchServiciosConContexto(String searchTerm, Long idConvenio, String tipoCliente,
            Pageable pageable);

    PageResponse<CatalogoResponse> searchServiciosByEstado(String searchTerm, Integer estado, Pageable pageable);

    com.pe.finanzas.catalogo.dto.response.SmartSearchResponse searchSmart(String searchTerm, Long idConvenio,
            String tipoCliente);

    CatalogoResponse updateServicio(Long id, UpdateCatalogoRequest request);

    void deleteServicio(Long id);

    List<CatalogoResponse> getAllServiciosList();

    List<com.pe.finanzas.catalogo.dto.response.DescuentoConfiguradoDto> getDescuentosPorPlan(Long idPlan);

    void aplicarDescuento(com.pe.finanzas.catalogo.dto.request.AplicarDescuentoDto request);
}
