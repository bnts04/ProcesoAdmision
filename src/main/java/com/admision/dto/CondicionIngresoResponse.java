package com.admision.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class CondicionIngresoResponse {

    private Long procesoId;

    private Integer totalPostulantes;
    private Integer totalVacantesAplicadas;

    private Integer totalIngresantes;
    private Integer totalNoIngresantes;

    private Integer carrerasProcesadas;
    private Integer carrerasSinVacantes;

    private Integer ingresantesAdicionalesPorEmpate;

    private List<String> carrerasSinVacantesDetalle;

    private String mensaje;
}