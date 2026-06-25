package com.admision.dto.pdf;

import com.admision.enums.TipoExamenPdf;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PdfExamenResponse {

    private Long id;
    private Long examenId;
    private String nombreExamen;
    private String area;
    private String letraTema;
    private TipoExamenPdf tipo;
    private String nombreArchivo;
    private String urlVer;
    private String urlDescargar;
    private LocalDateTime fechaGeneracion;
    private String mensaje;
}
