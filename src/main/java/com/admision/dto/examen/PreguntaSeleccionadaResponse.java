package com.admision.dto.examen;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PreguntaSeleccionadaResponse {

    private Integer ordenBase;

    private Long preguntaId;
    private String codigoPregunta;

    private ComponentePregunta componente;
    private String nombreComponente;

    private SubcursoPregunta subcurso;
    private String nombreSubcurso;

    private String enunciado;
    private String imagenUrl;
}