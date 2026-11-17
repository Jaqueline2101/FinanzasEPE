package com.pe.finanzas.system.service;

import com.pe.finanzas.system.entity.Configuracion;
import com.pe.finanzas.system.repository.ConfiguracionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class ConfiguracionService {

    private final ConfiguracionRepository configuracionRepository;

    /**
     * Obtiene el timeout global de sesión en minutos desde la configuración
     * 
     * @return timeout en minutos, o null si no está configurado
     */
    public Long getGlobalSessionTimeout() {
        try {
            return configuracionRepository.findByClave("SESSION_TIMEOUT_MINUTES")
                    .map(Configuracion::getValor)
                    .map(Long::parseLong)
                    .orElse(null);
        } catch (Exception e) {
            log.warn("Error al obtener SESSION_TIMEOUT_MINUTES de configuración, usando default", e);
            return null;
        }
    }

    /**
     * Obtiene un valor de configuración por clave
     * 
     * @param clave la clave de configuración
     * @return el valor como String, o null si no existe
     */
    public String getValor(String clave) {
        return configuracionRepository.findByClave(clave)
                .map(Configuracion::getValor)
                .orElse(null);
    }

    /**
     * Obtiene la configuración completa por clave
     */
    public Configuracion getConfig(String clave) {
        return configuracionRepository.findByClave(clave)
                .orElseThrow(() -> new RuntimeException("Configuración no encontrada para la clave: " + clave));
    }

    /**
     * Actualiza el valor de una configuración existente
     */
    public Configuracion updateConfig(String clave, String valor) {
        Configuracion config = getConfig(clave);
        config.setValor(valor);
        return configuracionRepository.save(config);
    }

    /**
     * Actualiza el timeout global de sesión
     */
    public Configuracion updateGlobalSessionTimeout(Long minutes) {
        return configuracionRepository.findByClave("SESSION_TIMEOUT_MINUTES")
                .map(config -> {
                    config.setValor(String.valueOf(minutes));
                    return configuracionRepository.save(config);
                })
                .orElseGet(() -> {
                    Configuracion config = Configuracion.builder()
                            .clave("SESSION_TIMEOUT_MINUTES")
                            .valor(String.valueOf(minutes))
                            .descripcion("Tiempo de expiración de sesión en minutos")
                            .tipoDato("INTEGER")
                            .build();
                    return configuracionRepository.save(config);
                });
    }

    /**
     * Guarda o actualiza una configuración
     * 
     * @param clave       la clave de configuración
     * @param valor       el valor a guardar
     * @param descripcion descripción de la configuración
     * @param tipoDato    tipo de dato (STRING, INTEGER, BOOLEAN, etc.)
     */
    public void guardarConfiguracion(String clave, String valor, String descripcion, String tipoDato) {
        Configuracion config = configuracionRepository.findByClave(clave)
                .orElse(Configuracion.builder()
                        .clave(clave)
                        .build());

        config.setValor(valor);
        config.setDescripcion(descripcion);
        config.setTipoDato(tipoDato);

        configuracionRepository.save(config);
    }
}
