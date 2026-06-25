package com.admision.dto.historial;

import com.admision.enums.TipoExamenPdf;
import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfGeneradoHistorialResponse {

    private Long id;
    private String letraTema;
    private TipoExamenPdf tipo;
    private String nombreArchivo;
    private String urlVer;
    private String urlDescargar;
    private LocalDateTime fechaGeneracion;
}
