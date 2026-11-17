package com.pe.finanzas.kardex.controller;

import com.pe.finanzas.kardex.entity.Kardex;
import com.pe.finanzas.kardex.repository.KardexRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/kardex")
@RequiredArgsConstructor
public class KardexController {

    private final KardexRepository kardexRepository;

    @GetMapping("/todos")
    public Page<Kardex> getTodos(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("fecha").descending());
        return kardexRepository.findAllByOrderByFechaDesc(pageable);
    }

    @GetMapping
    public List<Kardex> getHistorial(
            @RequestParam(required = false) Long idCatalogo,
            @RequestParam(required = false) Long idAlmArticulo) {
        
        if (idAlmArticulo != null) {
            return kardexRepository.findByIdAlmArticuloOrderByFechaDesc(idAlmArticulo);
        }
        if (idCatalogo != null) {
            return kardexRepository.findByIdCatalogoOrderByFechaDesc(idCatalogo);
        }
        // This part is fallback, returning first 100 for safety if no params
        return kardexRepository.findAll(PageRequest.of(0, 100, Sort.by("fecha").descending())).getContent();
    }
}
