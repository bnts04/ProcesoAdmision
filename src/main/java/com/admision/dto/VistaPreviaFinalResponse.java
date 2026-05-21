package com.admision.dto;

import com.admision.enums.EstadoProceso;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class VistaPreviaFinalResponse {

    private Long procesoId;
    private String nombreProceso;
    private String modalidad;
    private EstadoProceso estadoProceso;

    private Boolean fuentesValidas;
    private Boolean dbfProcesados;
    private Boolean pdfGuiaDisponible;
    private Boolean pdfGuiaGlobalUtilizado;

    private Integer totalPostulantes;
    private Integer puntajesCalculados;
    private Integer puntajesNoCalculados;

    private Integer totalCarreras;
    private Integer totalVacantes;

    private Integer totalIngresantes;
    private Integer totalNoIngresantes;
    private Integer totalPendientes;

    private Integer nombresCompletadosDesdePdf;
    private Integer nombresPendientes;

    private Integer temasValidos;
    private Integer temasInvalidos;
    private Integer temasTomadosDesdeIdentifi;

    private Integer observacionesCriticas;

    private List<String> observaciones;

    private String mensaje;
}