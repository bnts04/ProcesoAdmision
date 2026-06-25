package com.admision.dto.area;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DistribucionAreaExamenResponse {

    private AreaExamenResponse area;

    private Integer totalPreguntasRequeridas;
    private Long totalPreguntasDisponibles;
    private Boolean bancoSuficiente;

    private List<ResumenComponenteAreaResponse> resumenComponentes;
    private List<ConfiguracionAreaResponse> detalleSubcursos;
}