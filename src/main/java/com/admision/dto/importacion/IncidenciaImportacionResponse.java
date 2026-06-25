package com.admision.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IncidenciaImportacionResponse {

    /**
     * Número real de fila dentro del Excel.
     * Por ejemplo: fila 2, fila 15, etc.
     */
    private Integer filaExcel;

    /**
     * Número de pregunta consignado en la primera columna del Excel.
     */
    private String numeroPregunta;

    /**
     * Tipo de incidencia:
     * ERROR, OMITIDA o DUPLICADA.
     */
    private String tipo;

    /**
     * Explicación concreta de lo sucedido.
     */
    private String motivo;
}