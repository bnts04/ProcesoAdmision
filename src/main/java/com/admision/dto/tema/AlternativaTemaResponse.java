package com.admision.dto.tema;

import com.admision.enums.LetraAlternativa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlternativaTemaResponse {

    private LetraAlternativa letra;
    private String texto;
    private Boolean esCorrecta;
}