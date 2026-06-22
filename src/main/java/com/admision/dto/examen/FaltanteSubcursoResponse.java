package com.admision.dto.examen;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FaltanteSubcursoResponse {

    private ComponentePregunta componente;
    private String nombreComponente;

    private SubcursoPregunta subcurso;
    private String nombreSubcurso;

    private Integer requeridas;
    private Long disponibles;
    private Long faltantes;
}