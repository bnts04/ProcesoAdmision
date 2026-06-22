package com.admision.dto.banco;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.EstadoPregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ActualizarPreguntaRequest {

    private ComponentePregunta componente;
    private SubcursoPregunta subcurso;

    private String observacion;
    private String enunciado;

    private String alternativaA;
    private String alternativaB;
    private String alternativaC;
    private String alternativaD;
    private String alternativaE;

    private LetraAlternativa respuestaCorrecta;

    private EstadoPregunta estado;
}