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
public class FilaPreguntaImportacion {

    /*
     * Número real de fila dentro del Excel.
     * La primera pregunta está en la fila 2,
     * porque la fila 1 contiene los encabezados.
     */
    private Integer filaExcel;

    /*
     * Número referencial de la pregunta dentro
     * del archivo consolidado.
     */
    private String numeroPregunta;

    private String componente;
    private String subcurso;
    private String enunciado;

    /*
     * Nombre de la imagen dentro del ZIP.
     * Será null cuando la pregunta no tenga imagen.
     */
    private String nombreImagen;

    private String alternativaA;
    private String alternativaB;
    private String alternativaC;
    private String alternativaD;
    private String alternativaE;

    private String respuestaCorrecta;

    /*
     * Campo interno y opcional.
     */
    private String observacion;
}