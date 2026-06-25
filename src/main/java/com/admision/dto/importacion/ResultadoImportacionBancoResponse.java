package com.admision.dto.importacion;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResultadoImportacionBancoResponse {

    private String mensaje;

    /**
     * Cantidad de filas con datos encontradas en la hoja.
     * No incluye la fila de encabezados.
     */
    private Integer totalFilasLeidas;

    /**
     * Preguntas registradas correctamente en PostgreSQL.
     */
    private Integer preguntasImportadas;

    /**
     * Preguntas que no se cargaron por error, duplicidad
     * o incumplimiento de alguna validación.
     */
    private Integer preguntasOmitidas;

    /**
     * Preguntas importadas que tenían una imagen válida
     * dentro del archivo ZIP.
     */
    private Integer preguntasConImagen;

    /**
     * Preguntas importadas sin imagen.
     */
    private Integer preguntasSinImagen;

    /**
     * Preguntas detectadas como duplicadas.
     */
    private Integer preguntasDuplicadas;

    /**
     * Filas que tuvieron errores de estructura, contenido,
     * clasificación, clave o imagen.
     */
    private Integer totalErrores;

    /**
     * Lista detallada de incidencias encontradas.
     */
    @Builder.Default
    private List<IncidenciaImportacionResponse> incidencias = new ArrayList<>();
}