package com.admision.dto.vistaprevia;

import com.admision.enums.CodigoAreaExamen;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaPreviaExamenResponse {

    private Long examenId;
    private String nombreExamen;
    private CodigoAreaExamen area;
    private String nombreArea;
    private String letraTema;
    private Integer totalPreguntas;
    private LocalDateTime fechaGeneracion;
    private List<VistaPreviaPreguntaResponse> preguntas;
}
