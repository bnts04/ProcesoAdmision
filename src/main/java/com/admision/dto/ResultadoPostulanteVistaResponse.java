package com.admision.dto;

import com.admision.entity.ResultadoPostulante;
import com.admision.enums.CondicionPostulante;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@Builder
public class ResultadoPostulanteVistaResponse {

    private Long id;
    private Long procesoId;

    private String codigo;
    private String dni;
    private String apellidosNombres;
    private String facultad;
    private String carrera;

    private String litho;
    private String tema;
    private String secuencia;

    private Integer correctas;
    private Integer incorrectas;
    private Integer blancas;

    private BigDecimal puntajeBruto;
    private BigDecimal puntajeFinal;

    private Integer ome;
    private Integer omg;

    private CondicionPostulante condicion;
    private String observacion;

    public static ResultadoPostulanteVistaResponse fromEntity(ResultadoPostulante resultado, Long procesoId) {
        return ResultadoPostulanteVistaResponse.builder()
                .id(resultado.getId())
                .procesoId(procesoId)
                .codigo(resultado.getCodigo())
                .dni(resultado.getDni())
                .apellidosNombres(resultado.getApellidosNombres())
                .facultad(resultado.getFacultad())
                .carrera(resultado.getCarrera())
                .litho(resultado.getLitho())
                .tema(resultado.getTema())
                .secuencia(resultado.getSecuencia())
                .correctas(resultado.getCorrectas())
                .incorrectas(resultado.getIncorrectas())
                .blancas(resultado.getBlancas())
                .puntajeBruto(resultado.getPuntajeBruto())
                .puntajeFinal(resultado.getPuntajeFinal())
                .ome(resultado.getOme())
                .omg(resultado.getOmg())
                .condicion(resultado.getCondicion())
                .observacion(resultado.getObservacion())
                .build();
    }
}