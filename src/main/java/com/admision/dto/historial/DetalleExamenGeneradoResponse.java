package com.admision.dto.historial;

import com.admision.dto.tema.TemaExamenResponse;
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
public class DetalleExamenGeneradoResponse {

    private Long examenId;
    private String nombreExamen;
    private CodigoAreaExamen area;
    private String nombreArea;
    private String descripcionArea;
    private Integer cantidadTemas;
    private String temaInicial;
    private Integer totalPreguntas;
    private EstadoExamenGenerado estado;
    private LocalDateTime fechaGeneracion;
    private List<TemaExamenResponse> temas;
    private List<PdfGeneradoHistorialResponse> pdfs;
}
