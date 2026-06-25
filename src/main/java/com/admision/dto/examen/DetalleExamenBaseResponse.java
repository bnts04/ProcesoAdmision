package com.admision.dto.examen;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DetalleExamenBaseResponse {

    private ExamenGeneradoResponse examen;
    private List<PreguntaSeleccionadaResponse> preguntasBase;
}