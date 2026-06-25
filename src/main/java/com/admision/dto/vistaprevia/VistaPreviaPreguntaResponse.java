package com.admision.dto.vistaprevia;

import com.admision.enums.ComponentePregunta;
import com.admision.enums.LetraAlternativa;
import com.admision.enums.SubcursoPregunta;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaPreviaPreguntaResponse {

    private Integer numeroPregunta;
    private String codigoPregunta;
    private ComponentePregunta componente;
    private String nombreComponente;
    private SubcursoPregunta subcurso;
    private String nombreSubcurso;
    private String enunciado;
    private String imagenUrl;
    private LetraAlternativa respuestaCorrectaFinal;
    private List<VistaPreviaAlternativaResponse> alternativas;
}
