package com.admision.dto.banco;

import com.admision.enums.SubcursoPregunta;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResumenSubcursoBancoResponse {

    private SubcursoPregunta subcurso;
    private String nombreSubcurso;
    private Long totalPreguntas;
}