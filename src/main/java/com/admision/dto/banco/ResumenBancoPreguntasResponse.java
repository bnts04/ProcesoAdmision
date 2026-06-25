package com.admision.dto.banco;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenBancoPreguntasResponse {

    private Long totalPreguntasActivas;
    private List<ResumenComponenteBancoResponse> componentes;
}