package com.admision.dto.banco;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaBancoResponse {

    private Long id;
    private String codigo;

    private ComponentePregunta componente;
    private String nombreComponente;

    private SubcursoPregunta subcurso;
    private String nombreSubcurso;

    private String enunciado;

    private String imagenUrl;
    private String observacion;

    private LetraAlternativa respuestaCorrecta;

    private EstadoPregunta estado;

    private LocalDateTime fechaRegistro;
    private LocalDateTime fechaActualizacion;

    private List<AlternativaPreguntaResponse> alternativas;
}