package com.admision.dto;

import com.admision.enums.EstadoProceso;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ReporteTecnicoResponse {

    private Long procesoId;
    private String nombreProceso;
    private String modalidad;
    private EstadoProceso estadoProceso;

    private Integer totalArchivosCargados;
    private List<String> archivosCargados;

    private Integer totalPostulantes;
    private Integer puntajesCalculados;
    private Integer puntajesNoCalculados;

    private Integer totalIngresantes;
    private Integer totalNoIngresantes;
    private Integer totalPendientes;

    private Integer totalCarrerasDetectadas;
    private Integer totalCarrerasConVacantes;
    private Integer totalVacantesConfiguradas;

    private Integer registrosConCarreraPendiente;
    private Integer registrosConObservaciones;

    private List<String> observaciones;

    private String mensaje;
}