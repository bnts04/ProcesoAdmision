package com.admision.dto.historial;

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
public class HistorialExamenResponse {

    private Long examenId;
    private String nombreExamen;
    private CodigoAreaExamen area;
    private String nombreArea;
    private List<String> temas;
    private Integer totalPreguntas;
    private LocalDateTime fechaGeneracion;
    private EstadoExamenGenerado estado;
    private boolean pdfExamenDisponible;
    private boolean pdfClaveDisponible;
}
