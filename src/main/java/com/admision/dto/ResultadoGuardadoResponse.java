package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ResultadoGuardadoResponse {

    private Long procesoId;
    private Integer totalGuardados;
    private String mensaje;
}