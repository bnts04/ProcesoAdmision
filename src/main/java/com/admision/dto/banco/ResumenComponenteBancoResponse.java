package com.admision.dto.banco;

import com.admision.enums.ComponentePregunta;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenComponenteBancoResponse {

    private ComponentePregunta componente;
    private String nombreComponente;
    private Long totalPreguntas;
    private List<ResumenSubcursoBancoResponse> subcursos;
}