package com.pe.finanzas.niveles.service.impl;

import com.pe.finanzas.niveles.entity.Nivel;
import com.pe.finanzas.niveles.repository.NivelRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class CodigoNivelGenerator {

    private final NivelRepository nivelRepository;

    public String generarCodigo(Long idNivelPadre, String codigoManual) {
        if (idNivelPadre == null) {
            return validarCodigoRaiz(codigoManual);
        }
        return generarCodigoHijo(idNivelPadre);
    }

    private String validarCodigoRaiz(String codigoManual) {
        if (codigoManual == null || codigoManual.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El código es obligatorio para niveles raíz. Debe proporcionar un código numérico.");
        }

        // Validar que solo contenga números
        if (!codigoManual.matches("^[0-9]+$")) {
            throw new IllegalArgumentException(
                    "El código debe contener solo números");
        }

        // Validar que no exista otro nivel raíz con el mismo código
        if (nivelRepository.existsByNumNivelAndNivelPadreIsNull(codigoManual)) {
            throw new IllegalArgumentException(
                    "Ya existe un nivel raíz con el código: " + codigoManual);
        }

        log.info("Código raíz validado: {}", codigoManual);
        return codigoManual;
    }

    /**
     * Genera el código automático para un nivel hijo
     */
    private String generarCodigoHijo(Long idNivelPadre) {
        // Obtener el nivel padre
        Nivel padre = nivelRepository.findById(idNivelPadre)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Nivel padre no encontrado con ID: " + idNivelPadre));

        String codigoPadre = padre.getNumNivel();

        if (codigoPadre == null || codigoPadre.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "El nivel padre no tiene un código válido");
        }

        // Obtener todos los hijos del padre para calcular el siguiente secuencial
        List<Nivel> hermanos = nivelRepository.findByNivelPadreIdNivelOrderByNumNivel(idNivelPadre);

        int siguienteSecuencial = calcularSiguienteSecuencial(hermanos, codigoPadre);

        // Generar código: codigoPadre + secuencial de 3 dígitos
        String codigoGenerado = String.format("%s%03d", codigoPadre, siguienteSecuencial);

        log.info("Código generado para hijo del nivel {}: {} -> {}",
                idNivelPadre, codigoPadre, codigoGenerado);

        return codigoGenerado;
    }

    /**
     * Calcula el siguiente número secuencial para un hijo
     */
    private int calcularSiguienteSecuencial(List<Nivel> hermanos, String codigoPadre) {
        if (hermanos.isEmpty()) {
            return 1; // Primer hijo
        }

        int maxSecuencial = 0;
        int longitudPadre = codigoPadre.length();

        for (Nivel hermano : hermanos) {
            String codigoHermano = hermano.getNumNivel();

            // Verificar que el código del hermano comience con el código del padre
            if (codigoHermano != null && codigoHermano.startsWith(codigoPadre)) {
                try {
                    // Extraer los últimos 3 dígitos (el secuencial)
                    String secuencialStr = codigoHermano.substring(longitudPadre);

                    // Si tiene exactamente 3 dígitos, es un hijo directo
                    if (secuencialStr.length() >= 3) {
                        int secuencial = Integer.parseInt(secuencialStr.substring(0, 3));
                        maxSecuencial = Math.max(maxSecuencial, secuencial);
                    }
                } catch (NumberFormatException | StringIndexOutOfBoundsException e) {
                    log.warn("Código inválido encontrado en hermano: {}", codigoHermano);
                }
            }
        }

        return maxSecuencial + 1;
    }

    /**
     * Valida que un código tenga el formato correcto según su nivel
     */
    public boolean validarFormatoCodigo(String codigo, Long idNivelPadre) {
        if (codigo == null || codigo.isEmpty()) {
            return false;
        }

        // Debe contener solo números
        if (!codigo.matches("^[0-9]+$")) {
            return false;
        }

        // Si es raíz, cualquier longitud numérica es válida
        if (idNivelPadre == null) {
            return true;
        }

        // Si es hijo, debe tener la longitud del padre + 3
        Nivel padre = nivelRepository.findById(idNivelPadre).orElse(null);
        if (padre == null || padre.getNumNivel() == null) {
            return false;
        }

        int longitudEsperada = padre.getNumNivel().length() + 3;
        return codigo.length() == longitudEsperada && codigo.startsWith(padre.getNumNivel());
    }
}
