package com.admision.dto.examen;

import com.admision.enums.CodigoAreaExamen;
import com.admision.enums.EstadoExamenGenerado;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExamenGeneradoResponse {

    private Long id;
    private String nombreExamen;

    private CodigoAreaExamen codigoArea;
    private String nombreArea;
    private String descripcionArea;

    private Integer cantidadTemas;
    private String temaInicial;
    private Integer totalPreguntas;

    private EstadoExamenGenerado estado;
    private LocalDateTime fechaGeneracion;

    private List<String> temasGenerados;
}