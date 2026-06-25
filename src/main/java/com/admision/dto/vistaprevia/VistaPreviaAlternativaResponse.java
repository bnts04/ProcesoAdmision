package com.admision.dto.vistaprevia;

import com.admision.enums.LetraAlternativa;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VistaPreviaAlternativaResponse {

    private LetraAlternativa letra;
    private String texto;
}
