package com.admision.dto.banco;

import com.admision.enums.LetraAlternativa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativaPreguntaResponse {

    private Long id;
    private LetraAlternativa letra;
    private String texto;
    private Boolean esCorrecta;
}