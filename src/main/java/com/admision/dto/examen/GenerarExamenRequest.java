package com.admision.dto.examen;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GenerarExamenRequest {

    private String nombreExamen;
    private String area;
    private Integer cantidadTemas;
    private String temaInicial;
}