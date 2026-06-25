package com.admision.dto.tema;

import com.admision.enums.LetraAlternativa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleClaveTemaResponse {

    private Integer numeroPregunta;
    private LetraAlternativa respuestaCorrecta;
}