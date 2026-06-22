package com.admision.dto.tema;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemaExamenResponse {

    private Long id;
    private Long examenId;

    private String nombreExamen;
    private String letraTema;

    private Integer totalPreguntas;
    private LocalDateTime fechaGeneracion;
}