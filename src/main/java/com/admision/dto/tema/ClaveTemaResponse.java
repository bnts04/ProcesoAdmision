package com.admision.dto.tema;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ClaveTemaResponse {

    private Long examenId;
    private String nombreExamen;
    private String area;
    private String letraTema;
    private Integer totalPreguntas;

    private List<DetalleClaveTemaResponse> claves;
}