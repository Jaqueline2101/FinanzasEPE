package com.pe.articulos.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationDTO {
    private String userId;
    private String modulo;
    private String titulo;
    private Object contenido;
    private String tipo; // INFO|SUCCESS|ERROR
}
